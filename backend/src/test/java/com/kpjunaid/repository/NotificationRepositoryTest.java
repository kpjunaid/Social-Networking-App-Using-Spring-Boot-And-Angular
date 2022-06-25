package com.kpjunaid.repository;

import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Notification;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.shared.MockResourceRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {
    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    private final User USER_JOHN = MockResourceRepo.getMockUserJohn();
    private final User USER_JANE = MockResourceRepo.getMockUserJane();
    private final Post POST_ONE = MockResourceRepo.getPostOne();
    private final Comment COMMENT_ONE = MockResourceRepo.getCommentOne();
    private final Notification NOTIFICATION_ONE = MockResourceRepo.getNotificationOne();

    @BeforeEach
    void setUp() {
        User userJohn = userRepository.save(USER_JOHN);
        User userJane = userRepository.save(USER_JANE);

        POST_ONE.setAuthor(userJohn);
        POST_ONE.getLikeList().add(userJane);
        POST_ONE.setLikeCount(POST_ONE.getLikeCount()+1);
        Post postOne = postRepository.save(POST_ONE);

        COMMENT_ONE.setPost(postOne);
        COMMENT_ONE.setAuthor(userJane);
        Comment commentOne = commentRepository.save(COMMENT_ONE);

        NOTIFICATION_ONE.setSender(userJane);
        NOTIFICATION_ONE.setReceiver(userJohn);
        NOTIFICATION_ONE.setOwningPost(postOne);
        NOTIFICATION_ONE.setOwningComment(commentOne);
        NOTIFICATION_ONE.setType(NotificationType.POST_LIKE.name());
        notificationRepository.save(NOTIFICATION_ONE);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnOptionalOfNotification_whenReceiverOwningPostAndTypeIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post owningPost = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        Optional<Notification> returnedNotification = notificationRepository
                .findByReceiverAndOwningPostAndType(userJohn, owningPost, NotificationType.POST_LIKE.name());

        assertThat(returnedNotification.isPresent()).isTrue();
    }

    @Test
    void shouldReturnListOfNotification_whenReceiverIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        List<Notification> returnedNotificationList = notificationRepository
                .findNotificationsByReceiver(userJohn, PageRequest.of(0, 5));

        assertThat(returnedNotificationList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfUnseenNotification_whenReceiverIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        List<Notification> returnedNotificationList = notificationRepository
                .findNotificationsByReceiverAndIsSeenIsFalse(userJohn);

        assertThat(returnedNotificationList.get(0).getIsSeen()).isFalse();
    }

    @Test
    void shouldReturnListOfUnreadNotification_whenReceiverIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        List<Notification> returnedNotificationList = notificationRepository
                .findNotificationsByReceiverAndIsReadIsFalse(userJohn);

        assertThat(returnedNotificationList.get(0).getIsRead()).isFalse();
    }

    @Test
    void shouldDeleteNotification_whenOwningPostIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post owningPost = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        notificationRepository.deleteNotificationByOwningPost(owningPost);

        Optional<Notification> deletedNotification = notificationRepository
                .findByReceiverAndOwningPostAndType(userJohn, owningPost, NotificationType.POST_LIKE.name());

        assertThat(deletedNotification.isPresent()).isFalse();
    }

    @Test
    void shouldDeleteNotification_whenOwningCommentIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post owningPost = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);
        Comment owningComment = commentRepository.findByPost(owningPost, PageRequest.of(0, 5)).get(0);

        notificationRepository.deleteNotificationByOwningComment(owningComment);

        Optional<Notification> deletedNotification = notificationRepository
                .findByReceiverAndOwningPostAndType(userJohn, owningPost, NotificationType.POST_LIKE.name());

        assertThat(deletedNotification.isPresent()).isFalse();
    }
}