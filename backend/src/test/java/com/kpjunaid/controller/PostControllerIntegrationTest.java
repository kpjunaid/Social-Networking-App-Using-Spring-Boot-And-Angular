package com.kpjunaid.controller;

import com.kpjunaid.dto.TagDto;
import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Notification;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.exception.CommentNotFoundException;
import com.kpjunaid.exception.PostNotFoundException;
import com.kpjunaid.repository.*;
import com.kpjunaid.service.*;
import com.kpjunaid.shared.MockResourceRepo;
import com.kpjunaid.shared.WithMockAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    PostService postService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    TagService tagService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    NotificationRepository notificationRepository;

    private final String API_URL_PREFIX = "/api/v1";
    private final User USER_JOHN = MockResourceRepo.getMockUserJohn();
    private final User USER_JANE = MockResourceRepo.getMockUserJane();
    private final Post POST_ONE = MockResourceRepo.getPostOne();
    private final Post POST_TWO = MockResourceRepo.getPostTwo();

    @BeforeEach
    void setUp() {
        USER_JOHN.setPassword(passwordEncoder.encode(USER_JOHN.getPassword()));
        User userJohn = userRepository.save(USER_JOHN);

        USER_JANE.setPassword(passwordEncoder.encode(USER_JANE.getPassword()));
        User userJane = userRepository.save(USER_JANE);

        POST_ONE.setAuthor(userJohn);
        postRepository.save(POST_ONE);

        POST_TWO.setAuthor(userJane);
        postRepository.save(POST_TWO);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        postRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockAuthUser
    void createNewPost() throws Exception {
        String postTagsString = "[{\"tagName\":\"TagOne\",\"action\":\"add\"}]";
        String fileContent = "some-file-content";
        MockMultipartFile postPhoto = new MockMultipartFile(
                "postPhoto",
                "photo.jpeg",
                "image/jpeg",
                fileContent.getBytes());

        mockMvc.perform(multipart(API_URL_PREFIX + "/posts/create")
                        .file(postPhoto)
                        .param("content", "New Post Content")
                        .param("postTags", postTagsString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("New Post Content"))
                .andExpect(jsonPath("$.postTags", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void updatePost() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);
        String postTagsString = "[{\"tagName\":\"TagOne\",\"action\":\"add\"}]";
        String fileContent = "some-file-content";
        MockMultipartFile postPhoto = new MockMultipartFile(
                "postPhoto",
                "photo.jpeg",
                "image/jpeg",
                fileContent.getBytes());

        mockMvc.perform(multipart(API_URL_PREFIX + "/posts/{postId}/update", postOne.getId())
                        .file(postPhoto)
                        .param("content", "Updated Post One")
                        .param("postTags", postTagsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Post One"))
                .andExpect(jsonPath("$.postTags", hasSize(1)))
                .andExpect(jsonPath("$.postPhoto").isNotEmpty());
    }

    @Test
    @WithMockAuthUser
    void deletePost() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/delete", postOne.getId()))
                .andExpect(status().isOk());

        assertThatThrownBy(() -> {
            postService.getPostById(postOne.getId());
        }).isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @WithMockAuthUser
    void deletePostPhoto() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);
        String fileContent = "some-file-content";
        MockMultipartFile postPhoto = new MockMultipartFile(
                "postPhoto",
                "photo.jpeg",
                "image/jpeg",
                fileContent.getBytes());
        Post postOneWithPhoto = postService.updatePost(postOne.getId(), postOne.getContent(), postPhoto, null);

        assertThat(postOneWithPhoto.getPostPhoto()).isNotNull();

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/photo/delete", postOne.getId()))
                .andExpect(status().isOk());

        Post updatedPostOne = postService.getPostById(postOne.getId());
        assertThat(updatedPostOne.getPostPhoto()).isNull();
    }

    @Test
    @WithMockAuthUser
    void getPostById() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}", postOne.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.content").value(postOne.getContent()));
    }

    @Test
    @WithMockAuthUser
    void getPostLikes() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        postService.likePost(postOne.getId());

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}/likes", postOne.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void getPostShares() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        postService.createPostShare("Post One Share", postOne.getId());

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}/shares", postOne.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void getPostComments() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post postOne = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        postService.createPostComment(postOne.getId(), "Post One Comment");

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}/comments", postOne.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void likePost() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/like", postTwo.getId()))
                .andExpect(status().isOk());

        Post postTwoLiked = postService.getPostById(postTwo.getId());
        assertThat(postTwoLiked.getLikeCount()).isEqualTo(1);

        Notification notification = notificationService.getNotificationByReceiverAndOwningPostAndType(
                userJane,
                postTwo,
                NotificationType.POST_LIKE.name()
        );
        assertThat(notification).isNotNull();
    }

    @Test
    @WithMockAuthUser
    void unlikePost() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        postService.likePost(postTwo.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/unlike", postTwo.getId()))
                .andExpect(status().isOk());


        Notification notification = notificationService.getNotificationByReceiverAndOwningPostAndType(
                userJane,
                postTwo,
                NotificationType.POST_LIKE.name()
        );
        assertThat(notification).isNotNull();
        assertThat(notification.getSender()).isNull();
    }

    @Test
    @WithMockAuthUser
    void createPostComment() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/comments/create", postTwo.getId())
                        .param("content", "Post One Comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment.content").value("Post One Comment"));

        Notification notification = notificationService.getNotificationByReceiverAndOwningPostAndType(
                userJane,
                postTwo,
                NotificationType.POST_COMMENT.name()
        );
        assertThat(notification).isNotNull();
    }

    @Test
    @WithMockAuthUser
    void updatePostComment() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Comment commentOne = postService.createPostComment(postTwo.getId(), "Post One Comment");

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/comments/{commentId}/update", postTwo.getId(), commentOne.getId())
                        .param("content", "Updated Post One Comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Post One Comment"));
    }

    @Test
    @WithMockAuthUser
    void deletePostComment() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Comment commentOne = postService.createPostComment(postTwo.getId(), "Post One Comment");

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/comments/{commentId}/delete", postTwo.getId(), commentOne.getId()))
                .andExpect(status().isOk());

        assertThatThrownBy(() -> {
            commentService.getCommentById(commentOne.getId());
        }).isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @WithMockAuthUser
    void likePostComment() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Comment commentOne = postService.createPostComment(postTwo.getId(), "Post One Comment");

        mockMvc.perform(post(API_URL_PREFIX + "/posts/comments/{commentId}/like", commentOne.getId()))
                .andExpect(status().isOk());

        Comment commentOneLiked = commentService.getCommentById(commentOne.getId());
        assertThat(commentOneLiked.getLikeCount()).isEqualTo(1);
    }

    @Test
    @WithMockAuthUser
    void unlikePostComment() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Comment commentOne = postService.createPostComment(postTwo.getId(), "Post One Comment");
        commentService.likeComment(commentOne.getId());

        Comment commentOneLiked = commentService.getCommentById(commentOne.getId());
        assertThat(commentOneLiked.getLikeCount()).isEqualTo(1);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/comments/{commentId}/unlike", commentOne.getId()))
                .andExpect(status().isOk());

        Comment commentOneUnliked = commentService.getCommentById(commentOne.getId());
        assertThat(commentOneUnliked.getLikeCount()).isEqualTo(0);
    }

    @Test
    @WithMockAuthUser
    void getCommentLikeList() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Comment commentOne = postService.createPostComment(postTwo.getId(), "Post One Comment");
        commentService.likeComment(commentOne.getId());

        mockMvc.perform(get(API_URL_PREFIX + "/posts/comments/{commentId}/likes", commentOne.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void createPostShare() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/share/create", postTwo.getId())
                        .param("content", "Post Two Share"))
                .andExpect(status().isOk());

        Post postTwoShared = postService.getPostById(postTwo.getId());
        assertThat(postTwoShared.getShareCount()).isEqualTo(1);

        Notification notification = notificationService.getNotificationByReceiverAndOwningPostAndType(
                userJane,
                postTwo,
                NotificationType.POST_SHARE.name()
        );
        assertThat(notification).isNotNull();
    }

    @Test
    @WithMockAuthUser
    void updatePostShare() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Post postTwoShare = postService.createPostShare("Post Two Share", postTwo.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/share/update", postTwoShare.getId())
                        .param("content", "Updated post Two Share"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated post Two Share"));
    }

    @Test
    @WithMockAuthUser
    void deletePostShare() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        Post postTwoShare = postService.createPostShare("Post Two Share", postTwo.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/share/delete", postTwoShare.getId()))
                .andExpect(status().isOk());

        Post postTwoUpdated = postService.getPostById(postTwo.getId());
        assertThat(postTwoUpdated.getShareCount()).isEqualTo(0);

        assertThatThrownBy(() -> {
            postService.getPostById(postTwoShare.getId());
        }).isInstanceOf(PostNotFoundException.class);

        Notification notification = notificationService.getNotificationByReceiverAndOwningPostAndType(
                userJane,
                postTwo,
                NotificationType.POST_SHARE.name()
        );
        assertThat(notification.getSender()).isNull();
    }

    @Test
    @WithMockAuthUser
    void getPostsByTag() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        Post postTwo = postRepository.findPostsByAuthor(userJane, PageRequest.of(0, 5)).get(0);

        List<TagDto> postTagsToAdd = List.of(new TagDto("TagOne", "add"));
        postService.updatePost(postTwo.getId(), postTwo.getContent(), null, postTagsToAdd);

        mockMvc.perform(get(API_URL_PREFIX + "/posts/tags/{tagName}", "TagOne")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}