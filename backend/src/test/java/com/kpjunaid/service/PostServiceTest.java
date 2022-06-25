package com.kpjunaid.service;

import com.kpjunaid.dto.TagDto;
import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.Tag;
import com.kpjunaid.entity.User;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.repository.PostRepository;
import com.kpjunaid.response.PostResponse;
import com.kpjunaid.shared.MockResource;
import com.kpjunaid.util.FileNamingUtil;
import com.kpjunaid.util.FileUploadUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataJpaTest
class PostServiceTest {
    @InjectMocks
    PostServiceImpl postService;

    @Mock
    PostRepository postRepository;

    @Mock
    UserService userService;

    @Mock
    CommentService commentService;

    @Mock
    TagService tagService;

    @Mock
    NotificationService notificationService;

    @Mock
    Environment environment;

    @Mock
    FileNamingUtil fileNamingUtil;

    @Mock
    FileUploadUtil fileUploadUtil;

    private final User USER_JOHN = MockResource.getMockUserJohn();
    private final User USER_JANE = MockResource.getMockUserJane();
    private final Post POST_ONE = MockResource.getPostOne();
    private final Post POST_ONE_SHARE = MockResource.getPostOneShare();
    private final Comment COMMENT_ONE = MockResource.getCommentOne();
    private final Tag TAG_ONE = MockResource.getTagOne();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnPost_whenPostIdIsGiven() {
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));

        Post returnedPost = postService.getPostById(POST_ONE.getId());

        assertThat(returnedPost).isNotNull();
        assertThat(returnedPost.getAuthor()).isEqualTo(USER_JOHN);
    }

    @Test
    void shouldReturnPostResponse_whenPostIdIsGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(postRepository.findById(1L)).thenReturn(Optional.of(POST_ONE));
        PostResponse returnedPostResponse = postService.getPostResponseById(POST_ONE.getId());

        assertThat(returnedPostResponse).isNotNull();
        assertThat(returnedPostResponse.getPost()).isEqualTo(POST_ONE);
        assertThat(returnedPostResponse.getLikedByAuthUser()).isFalse();
    }

    @Test
    void shouldReturnListOfTimelinePosts() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(postRepository.findPostsByAuthorIdIn(
                List.of(USER_JOHN.getId()),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCreated"))
        )).thenReturn(List.of(POST_ONE));

        List<PostResponse> returnedPostList = postService.getTimelinePostsPaginate(0, 5);

        assertThat(returnedPostList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfPostShares_whenSharedPostIsGiven() {
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(postRepository.findPostsBySharedPost(
                POST_ONE,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCreated"))
        )).thenReturn(List.of(POST_ONE_SHARE));
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);

        List<PostResponse> returnedPostShareList = postService.getPostSharesPaginate(POST_ONE, 0, 5);

        assertThat(returnedPostShareList.size()).isEqualTo(1);
        assertThat(returnedPostShareList.get(0).getPost().getSharedPost()).isEqualTo(POST_ONE);
    }

    @Test
    void shouldReturnListOfPosts_whenTagIsGiven() {
        POST_ONE.getPostTags().add(TAG_ONE);

        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(postRepository.findPostsByPostTags(
                TAG_ONE,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCreated"))
        )).thenReturn(List.of(POST_ONE));
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);

        List<PostResponse> returnedPostList = postService.getPostByTagPaginate(TAG_ONE, 0, 5);

        assertThat(returnedPostList.size()).isEqualTo(1);
        assertThat(returnedPostList.get(0).getPost().getPostTags().get(0)).isEqualTo(TAG_ONE);
    }

    @Test
    void shouldCreateNewPost_whenContentPostPhotoPostTagsAreGiven() throws IOException {
        String fileName = "photo-name.png";
        String fileContent = "some-file-content";
        String uploadProperty = "upload.post.images";
        String uploadDir = "upload-dir";
        String backendProperty = "app.root.backend";
        String backendUrl = "backend-url";

        POST_ONE.getPostTags().add(TAG_ONE);

        TagDto tagDto = TagDto.builder()
                .tagName("New Tag")
                .build();

        MultipartFile postPhoto = new MockMultipartFile(fileName, fileContent.getBytes());

        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(environment.getProperty(uploadProperty)).thenReturn(uploadDir);
        when(fileNamingUtil.nameFile(postPhoto)).thenReturn(fileName);
        when(environment.getProperty(backendProperty)).thenReturn(backendUrl);
        doNothing().when(fileUploadUtil).saveNewFile(uploadDir, fileName, postPhoto);
        when(tagService.getTagByName(anyString())).thenReturn(TAG_ONE);
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);

        Post returnedPost = postService.createNewPost("New Post", postPhoto, List.of(tagDto));

        verify(postRepository).save(any(Post.class));
        assertThat(returnedPost.getAuthor()).isEqualTo(USER_JOHN);
        assertThat(returnedPost.getPostTags().get(0).getName()).isEqualTo(TAG_ONE.getName());
    }

    @Test
    void shouldUpdatesPost_whenPostIdAndUpdatesAreGiven() throws IOException {
        String fileName = "photo-name.png";
        String fileContent = "some-file-content";
        String uploadProperty = "upload.post.images";
        String uploadDir = "upload-dir";
        String backendProperty = "app.root.backend";
        String backendUrl = "backend-url";

        POST_ONE.getPostTags().add(TAG_ONE);

        TagDto tagDto = TagDto.builder()
                .tagName(TAG_ONE.getName())
                .action("add")
                .build();

        MultipartFile postPhoto = new MockMultipartFile(fileName, fileContent.getBytes());

        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(environment.getProperty(uploadProperty)).thenReturn(uploadDir);
        when(fileNamingUtil.nameFile(postPhoto)).thenReturn(fileName);
        when(environment.getProperty(backendProperty)).thenReturn(backendUrl);
        doNothing().when(fileUploadUtil).saveNewFile(uploadDir, fileName, postPhoto);
        when(tagService.getTagByName(anyString())).thenReturn(TAG_ONE);
        when(tagService.increaseTagUseCounter(anyString())).thenReturn(TAG_ONE);
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);

        Post returnedPost = postService.updatePost(POST_ONE.getId(), "Updated Post", postPhoto, List.of(tagDto));

        verify(postRepository).save(any(Post.class));
        assertThat(returnedPost.getAuthor()).isEqualTo(USER_JOHN);
        assertThat(returnedPost.getContent()).isEqualTo("Updated Post");
    }

    @Test
    void shouldDeletePost_whenPostIdIsGiven() throws IOException {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        doNothing().when(postRepository).deleteById(POST_ONE.getId());

        postService.deletePost(POST_ONE.getId());

        verify(postRepository, times(1)).deleteById(POST_ONE.getId());
    }

    @Test
    void shouldDeletePostPhoto_whenPostIdIsGiven() throws IOException {
        String fileName = "photo-name.png";
        String uploadProperty = "upload.post.images";
        String uploadDir = "upload-dir";
        String backendProperty = "app.root.backend";
        String backendUrl = "backend-url";

        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(environment.getProperty(uploadProperty)).thenReturn(uploadDir);
        when(environment.getProperty(backendProperty)).thenReturn(backendUrl);
        doNothing().when(fileUploadUtil).deleteFile(uploadDir, fileName);
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);

        postService.deletePostPhoto(POST_ONE.getId());

        verify(postRepository).save(any(Post.class));
        assertThat(POST_ONE.getPostPhoto()).isNull();
    }

    @Test
    void shouldLikePost_whenPostIdIsGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);
        doNothing().when(notificationService)
                .sendNotification(USER_JOHN, USER_JANE, POST_ONE, null, NotificationType.POST_LIKE.name());

        postService.likePost(POST_ONE.getId());

        verify(postRepository).save(any(Post.class));
        assertThat(POST_ONE.getLikeCount()).isEqualTo(1);
        assertThat(POST_ONE.getLikeList().get(0)).isEqualTo(USER_JANE);
    }

    @Test
    void shouldUnlikePost_whenPostIdIsGiven() {
        POST_ONE.getLikeList().add(USER_JANE);
        POST_ONE.setLikeCount(POST_ONE.getLikeCount()+1);

        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);
        doNothing().when(notificationService)
                .removeNotification(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name());

        postService.unlikePost(POST_ONE.getId());

        verify(postRepository).save(any(Post.class));
        assertThat(POST_ONE.getLikeCount()).isEqualTo(0);
        assertThat(POST_ONE.getLikeList().size()).isEqualTo(0);
    }

    @Test
    void shouldCreatePostComment_whenPostIdAndContentIsGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(commentService.createNewComment(COMMENT_ONE.getContent(), POST_ONE)).thenAnswer((Answer) invocation -> {
            POST_ONE.getPostComments().add(COMMENT_ONE);
            return COMMENT_ONE;
        });

        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);
        doNothing().when(notificationService)
                .sendNotification(USER_JOHN, USER_JANE, POST_ONE, COMMENT_ONE, NotificationType.POST_COMMENT.name());

        postService.createPostComment(POST_ONE.getId(), COMMENT_ONE.getContent());

        verify(postRepository).save(any(Post.class));
        assertThat(POST_ONE.getCommentCount()).isEqualTo(1);
        assertThat(POST_ONE.getPostComments().size()).isEqualTo(1);
        assertThat(POST_ONE.getPostComments().get(0)).isEqualTo(COMMENT_ONE);
    }

    @Test
    void shouldUpdatePostComment_whenCommentIdPostIdAndContentAreGiven() {
        String updatedCommentContent = "Updated Comment";

        when(commentService.updateComment(COMMENT_ONE.getId(), updatedCommentContent)).thenReturn(COMMENT_ONE);

        postService.updatePostComment(COMMENT_ONE.getId(), POST_ONE.getId(), updatedCommentContent);

        verify(commentService, times(1)).updateComment(COMMENT_ONE.getId(), updatedCommentContent);
    }

    @Test
    void shouldDeletePostComment_whenCommentIdPostIdAreGiven() {
        POST_ONE.getPostComments().add(COMMENT_ONE);
        POST_ONE.setCommentCount(POST_ONE.getCommentCount()+1);

        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        doAnswer(invocation -> {
            POST_ONE.getPostComments().remove(COMMENT_ONE);
            return null;
        }).when(commentService).deleteComment(COMMENT_ONE.getId());
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);

        postService.deletePostComment(COMMENT_ONE.getId(), POST_ONE.getId());

        verify(postRepository).save(any(Post.class));
        assertThat(POST_ONE.getCommentCount()).isEqualTo(0);
        assertThat(POST_ONE.getPostComments().size()).isEqualTo(0);
    }

    @Test
    void shouldCreatePostShare_whenPostIdAndContentIsGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE.getId())).thenReturn(Optional.of(POST_ONE));
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE_SHARE, POST_ONE);
        doNothing().when(notificationService)
                .sendNotification(USER_JOHN, USER_JANE, POST_ONE, null, NotificationType.POST_SHARE.name());

        Post returnedPostShare = postService.createPostShare(POST_ONE_SHARE.getContent(), POST_ONE.getId());

        verify(postRepository, times(2)).save(any(Post.class));
        assertThat(returnedPostShare.getSharedPost()).isEqualTo(POST_ONE);
        assertThat(POST_ONE.getShareList().contains(returnedPostShare)).isTrue();
        assertThat(POST_ONE.getShareCount()).isEqualTo(1);
    }

    @Test
    void shouldUpdatePostShare_whenPostShareIdAndContentIsGiven() {
        String updatedPostShareContent = "Updated Post Share";

        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE_SHARE.getId())).thenReturn(Optional.of(POST_ONE_SHARE));
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE_SHARE);

        Post returnedPostShare = postService.updatePostShare(updatedPostShareContent, POST_ONE_SHARE.getId());

        verify(postRepository).save(any(Post.class));
        assertThat(returnedPostShare.getSharedPost()).isEqualTo(POST_ONE);
        assertThat(returnedPostShare.getContent()).isEqualTo(updatedPostShareContent);
    }

    @Test
    void shouldDeletePostShare_whenPostShareIdIsGiven() {
        POST_ONE.getShareList().add(POST_ONE_SHARE);
        POST_ONE.setShareCount(POST_ONE.getShareCount()+1);
        POST_ONE_SHARE.setSharedPost(POST_ONE);

        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(postRepository.findById(POST_ONE_SHARE.getId())).thenReturn(Optional.of(POST_ONE_SHARE));
        when(postRepository.save(any(Post.class))).thenReturn(POST_ONE);
        doNothing().when(notificationService)
                .removeNotification(USER_JANE, POST_ONE_SHARE, NotificationType.POST_SHARE.name());

        postService.deletePostShare(POST_ONE_SHARE.getId());

        verify(postRepository).save(any(Post.class));
        verify(postRepository).deleteById(POST_ONE_SHARE.getId());
        assertThat(POST_ONE.getShareList().contains(POST_ONE_SHARE)).isFalse();
        assertThat(POST_ONE.getShareCount()).isEqualTo(0);
    }
}