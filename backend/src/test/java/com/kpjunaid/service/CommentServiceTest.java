package com.kpjunaid.service;

import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.Tag;
import com.kpjunaid.entity.User;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.repository.CommentRepository;
import com.kpjunaid.response.CommentResponse;
import com.kpjunaid.shared.MockResource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
class CommentServiceTest {
    @InjectMocks
    CommentServiceImpl commentService;

    @Mock
    CommentRepository commentRepository;

    @Mock
    UserService userService;

    @Mock
    NotificationService notificationService;

    private final User USER_JOHN = MockResource.getMockUserJohn();
    private final User USER_JANE = MockResource.getMockUserJane();
    private final Post POST_ONE = MockResource.getPostOne();
    private final Comment COMMENT_ONE = MockResource.getCommentOne();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnComment_whenCommentIdIsGiven() {
        POST_ONE.getPostComments().add(COMMENT_ONE);
        POST_ONE.setCommentCount(POST_ONE.getCommentCount()+1);

        when(commentRepository.findById(COMMENT_ONE.getId())).thenReturn(Optional.of(COMMENT_ONE));

        Comment returnedComment = commentService.getCommentById(COMMENT_ONE.getId());

        assertThat(returnedComment).isNotNull();
        assertThat(returnedComment.getPost()).isEqualTo(POST_ONE);
    }

    @Test
    void shouldCreateNewComment_whenContentAndPostAreGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(commentRepository.save(any(Comment.class))).thenReturn(COMMENT_ONE);

        Comment returnedComment = commentService.createNewComment("New Comment", POST_ONE);

        verify(commentRepository).save(any(Comment.class));
        assertThat(returnedComment.getPost()).isEqualTo(POST_ONE);
        assertThat(returnedComment.getAuthor()).isEqualTo(USER_JOHN);
    }

    @Test
    void shouldUpdateComment_whenContentAndCommentIdAreGiven() {
        String updatedCommentContent = "Updated Comment";

        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(commentRepository.findById(COMMENT_ONE.getId())).thenReturn(Optional.of(COMMENT_ONE));
        when(commentRepository.save(any(Comment.class))).thenReturn(COMMENT_ONE);

        Comment returnedComment = commentService.updateComment(COMMENT_ONE.getId(), updatedCommentContent);

        verify(commentRepository).save(any(Comment.class));
        assertThat(returnedComment.getContent()).isEqualTo(updatedCommentContent);
    }

    @Test
    void shouldDeleteComment_whenCommentIdIsGiven() {
        POST_ONE.getPostComments().add(COMMENT_ONE);
        POST_ONE.setCommentCount(POST_ONE.getCommentCount()+1);

        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(commentRepository.findById(COMMENT_ONE.getId())).thenReturn(Optional.of(COMMENT_ONE));
        doNothing().when(commentRepository).deleteById(COMMENT_ONE.getId());
        doNothing().when(notificationService).deleteNotificationByOwningComment(COMMENT_ONE);

        commentService.deleteComment(COMMENT_ONE.getId());

        verify(commentRepository).deleteById(COMMENT_ONE.getId());
    }

    @Test
    void shouldLikePostComment_whenCommentIdIsGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(commentRepository.findById(COMMENT_ONE.getId())).thenReturn(Optional.of(COMMENT_ONE));
        when(commentRepository.save(any(Comment.class))).thenReturn(COMMENT_ONE);
        doNothing().when(notificationService)
                .sendNotification(USER_JOHN, USER_JANE, POST_ONE, COMMENT_ONE, NotificationType.COMMENT_LIKE.name());

        Comment returnedComment = commentService.likeComment(COMMENT_ONE.getId());

        verify(commentRepository).save(any(Comment.class));
        assertThat(returnedComment.getLikeCount()).isEqualTo(1);
        assertThat(returnedComment.getLikeList().contains(USER_JANE)).isTrue();
    }

    @Test
    void shouldUnlikePostComment_whenCommentIdIsGiven() {
        COMMENT_ONE.getLikeList().add(USER_JANE);
        COMMENT_ONE.setLikeCount(COMMENT_ONE.getLikeCount()+1);

        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(commentRepository.findById(COMMENT_ONE.getId())).thenReturn(Optional.of(COMMENT_ONE));
        when(commentRepository.save(any(Comment.class))).thenReturn(COMMENT_ONE);
        doNothing().when(notificationService)
                .removeNotification(USER_JOHN, POST_ONE, NotificationType.COMMENT_LIKE.name());

        Comment returnedComment = commentService.unlikeComment(COMMENT_ONE.getId());

        verify(commentRepository).save(any(Comment.class));
        assertThat(returnedComment.getLikeCount()).isEqualTo(0);
        assertThat(returnedComment.getLikeList().contains(USER_JOHN)).isFalse();
    }

    @Test
    void shouldReturnListOfPostComments_whenPostIdIsGiven() {
        POST_ONE.getPostComments().add(COMMENT_ONE);
        POST_ONE.setCommentCount(POST_ONE.getCommentCount()+1);

        COMMENT_ONE.getLikeList().add(USER_JOHN);
        COMMENT_ONE.setLikeCount(COMMENT_ONE.getLikeCount()+1);

        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(commentRepository.findByPost(
                POST_ONE,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateCreated"))
        )).thenReturn(List.of(COMMENT_ONE));

        List<CommentResponse> returnedCommentList = commentService.getPostCommentsPaginate(POST_ONE, 0, 5);

        assertThat(returnedCommentList.size()).isEqualTo(1);
        Assertions.assertThat(returnedCommentList.get(0).getComment()).isEqualTo(COMMENT_ONE);
    }
}