package com.kpjunaid.controller;

import com.kpjunaid.shared.MockResource;
import com.kpjunaid.shared.WithMockAuthUser;
import com.kpjunaid.dto.TagDto;
import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.Tag;
import com.kpjunaid.entity.User;
import com.kpjunaid.response.CommentResponse;
import com.kpjunaid.response.PostResponse;
import com.kpjunaid.service.CommentService;
import com.kpjunaid.service.PostService;
import com.kpjunaid.service.TagService;
import com.kpjunaid.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;

    @MockBean
    CommentService commentService;

    @MockBean
    UserService userService;

    @MockBean
    TagService tagService;

    private final User USER_JOHN = MockResource.getMockUserJohn();
    private final User USER_JANE = MockResource.getMockUserJane();
    private final Post POST_ONE = MockResource.getPostOne();
    private final Post POST_TWO = MockResource.getPostTwo();
    private final Post POST_ONE_SHARE = MockResource.getPostOneShare();
    private final Comment COMMENT_ONE = MockResource.getCommentOne();
    private final Tag TAG_ONE = MockResource.getTagOne();
    private final String API_URL_PREFIX = "/api/v1";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @WithMockAuthUser
    void shouldCreateNewPost_whenContentPhotoAndTagsAreGiven() throws Exception {
        String postTagsString = "[{\"tagName\":\"TagOne\",\"action\":\"add\"}]";
        String fileContent = "some-file-content";
        MockMultipartFile postPhoto = new MockMultipartFile("postPhoto", fileContent.getBytes());
        List<TagDto> postTags = List.of(
                new TagDto("TagOne", "add")
        );

        when(postService.createNewPost(POST_ONE.getContent(), postPhoto, postTags)).thenReturn(POST_ONE);

        mockMvc.perform(multipart(API_URL_PREFIX + "/posts/create")
                        .file(postPhoto)
                        .param("content", POST_ONE.getContent())
                        .param("postTags", postTagsString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(POST_ONE.getContent()));
    }

    @Test
    @WithMockAuthUser
    void shouldUpdatePost_whenPostIdContentPhotoAndTagsAreGiven() throws Exception {
        String postTagsString = "[{\"tagName\":\"TagOne\",\"action\":\"add\"}]";
        String fileContent = "some-file-content";
        MockMultipartFile postPhoto = new MockMultipartFile("postPhoto", fileContent.getBytes());
        List<TagDto> postTags = List.of(
                new TagDto("TagOne", "add")
        );

        when(postService.updatePost(POST_ONE.getId(), POST_ONE.getContent(), postPhoto, postTags)).thenReturn(POST_ONE);

        mockMvc.perform(multipart(API_URL_PREFIX + "/posts/{postId}/update", POST_ONE.getId())
                        .file(postPhoto)
                        .param("content", POST_ONE.getContent())
                        .param("postTags", postTagsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(POST_ONE.getContent()));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenPostDeleted() throws Exception {
        doNothing().when(postService).deletePost(POST_ONE.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/delete", POST_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenPostPhotoDeleted() throws Exception {
        doNothing().when(postService).deletePostPhoto(POST_ONE.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/photo/delete", POST_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnPost_whenPostIdIsGiven() throws Exception {
        when(postService.getPostResponseById(POST_ONE.getId()))
                .thenReturn(new PostResponse(POST_ONE, false));

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}", POST_ONE.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.post.content").value(POST_ONE.getContent()));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfPostLikes_whenPostIdIsGiven() throws Exception {
        when(postService.getPostById(POST_ONE.getId())).thenReturn(POST_ONE);
        when(userService.getLikesByPostPaginate(POST_ONE, 0, 5))
                .thenReturn(List.of(USER_JOHN, USER_JANE));

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}/likes", POST_ONE.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfPostShares_whenPostIdIsGiven() throws Exception {
        when(postService.getPostById(POST_ONE.getId())).thenReturn(POST_ONE);
        when(postService.getPostSharesPaginate(POST_ONE, 0, 5))
                .thenReturn(List.of(new PostResponse(POST_ONE_SHARE, false)));

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}/shares", POST_ONE.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfPostComments_whenPostIdIsGiven() throws Exception {
        when(postService.getPostById(POST_ONE.getId())).thenReturn(POST_ONE);
        when(commentService.getPostCommentsPaginate(POST_ONE, 0, 5))
                .thenReturn(List.of(new CommentResponse(COMMENT_ONE, false)));

        mockMvc.perform(get(API_URL_PREFIX + "/posts/{postId}/comments", POST_ONE.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenPostLiked() throws Exception {
        doNothing().when(postService).likePost(POST_ONE.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/like", POST_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenPostUnliked() throws Exception {
        doNothing().when(postService).unlikePost(POST_ONE.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/unlike", POST_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldCreateNewPostComment_whenContentAndPostIdAreGiven() throws Exception {
        when(postService.createPostComment(POST_ONE.getId(), COMMENT_ONE.getContent())).thenReturn(COMMENT_ONE);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/comments/create", POST_ONE.getId())
                        .param("content", COMMENT_ONE.getContent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment.content").value(COMMENT_ONE.getContent()));
    }

    @Test
    @WithMockAuthUser
    void shouldUpdatePostComment_whenContentCommentIdAndPostIdAreGiven() throws Exception {
        when(postService.updatePostComment(COMMENT_ONE.getId(), POST_ONE.getId(), COMMENT_ONE.getContent()))
                .thenReturn(COMMENT_ONE);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/comments/{commentId}/update", POST_ONE.getId(), COMMENT_ONE.getId())
                        .param("content", COMMENT_ONE.getContent()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(COMMENT_ONE.getContent()));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenCommentDeleted() throws Exception {
        doNothing().when(postService).deletePostComment(COMMENT_ONE.getId(), POST_ONE.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/comments/{commentId}/delete", POST_ONE.getId(), COMMENT_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenCommentLiked() throws Exception {
        when(commentService.likeComment(COMMENT_ONE.getId())).thenReturn(COMMENT_ONE);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/comments/{commentId}/like", COMMENT_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenCommentUnliked() throws Exception {
        when(commentService.unlikeComment(COMMENT_ONE.getId())).thenReturn(COMMENT_ONE);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/comments/{commentId}/unlike", COMMENT_ONE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfCommentLikes_whenCommentIdIsGiven() throws Exception {
        when(commentService.getCommentById(COMMENT_ONE.getId())).thenReturn(COMMENT_ONE);
        when(userService.getLikesByCommentPaginate(COMMENT_ONE, 0, 5))
                .thenReturn(List.of(USER_JOHN, USER_JANE));

        mockMvc.perform(get(API_URL_PREFIX + "/posts/comments/{commentId}/likes", COMMENT_ONE.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockAuthUser
    void shouldCreateNewPostShare_whenContentAndPostIdAreGiven() throws Exception {
        when(postService.createPostShare(POST_ONE_SHARE.getContent(), POST_ONE.getId())).thenReturn(POST_ONE_SHARE);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/share/create", POST_ONE.getId())
                        .param("content", POST_ONE_SHARE.getContent()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldUpdatePostShare_whenContentAndPostIdAreGiven() throws Exception {
        when(postService.updatePostShare(POST_ONE_SHARE.getContent(), POST_ONE.getId())).thenReturn(POST_ONE_SHARE);

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postId}/share/update", POST_ONE.getId())
                        .param("content", POST_ONE_SHARE.getContent()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenPostShareDeleted() throws Exception {
        doNothing().when(postService).deletePostShare(POST_ONE_SHARE.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/posts/{postShareId}/share/delete", POST_ONE_SHARE.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfPosts_whenTagNameIsGiven() throws Exception {
        when(tagService.getTagByName(TAG_ONE.getName())).thenReturn(TAG_ONE);
        when(postService.getPostByTagPaginate(TAG_ONE, 0, 5))
                .thenReturn(List.of(
                        new PostResponse(POST_ONE, false),
                        new PostResponse(POST_TWO, false)
                ));

        mockMvc.perform(get(API_URL_PREFIX + "/posts/tags/{tagName}", TAG_ONE.getName())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}