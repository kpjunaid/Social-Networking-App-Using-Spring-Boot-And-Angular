package com.kpjunaid.service;

import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Notification;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.exception.NotificationNotFoundException;
import com.kpjunaid.repository.NotificationRepository;
import com.kpjunaid.shared.MockResource;
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
class NotificationServiceTest {
    @InjectMocks
    NotificationServiceImpl notificationService;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserService userService;

    private final User USER_JOHN = MockResource.getMockUserJohn();
    private final User USER_JANE = MockResource.getMockUserJane();
    private final Post POST_ONE = MockResource.getPostOne();
    private final Comment COMMENT_ONE = MockResource.getCommentOne();
    private final Notification NOTIFICATION_ONE = MockResource.getNotificationOne();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnNotification_whenNotificationIdIsGiven() {
        when(notificationRepository.findById(NOTIFICATION_ONE.getId())).thenReturn(Optional.of(NOTIFICATION_ONE));

        Notification returnedNotification = notificationService.getNotificationById(NOTIFICATION_ONE.getId());

        assertThat(returnedNotification).isNotNull();
        assertThat(returnedNotification).isEqualTo(NOTIFICATION_ONE);
    }

    @Test
    void shouldReturnNotification_whenReceiverOwningPostNotificationTypeAreGiven() {
        when(notificationRepository.findByReceiverAndOwningPostAndType(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name()))
                .thenReturn(Optional.of(NOTIFICATION_ONE));

        Notification returnedNotification = notificationService.getNotificationByReceiverAndOwningPostAndType(
                USER_JOHN,
                POST_ONE,
                NotificationType.POST_LIKE.name()
        );

        assertThat(returnedNotification).isNotNull();
        assertThat(returnedNotification).isEqualTo(NOTIFICATION_ONE);
    }

    @Test
    void shouldCreateAndSendNewNotification_whenAllArgumentsAreGiven() {
        when(notificationRepository.findByReceiverAndOwningPostAndType(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name()))
                .thenThrow(NotificationNotFoundException.class);

        notificationService.sendNotification(
                USER_JOHN,
                USER_JANE,
                POST_ONE,
                null,
                NotificationType.POST_LIKE.name()
        );

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldUpdateAndSendExistingNotification_whenRequiredArgumentsAreGiven() {
        when(notificationRepository.findByReceiverAndOwningPostAndType(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name()))
                .thenReturn(Optional.of(NOTIFICATION_ONE));

        notificationService.sendNotification(
                USER_JOHN,
                USER_JANE,
                POST_ONE,
                null,
                NotificationType.POST_LIKE.name()
        );

        verify(notificationRepository).save(any(Notification.class));
        assertThat(NOTIFICATION_ONE.getIsSeen()).isFalse();
        assertThat(NOTIFICATION_ONE.getIsRead()).isFalse();
    }

    @Test
    void shouldRemoveAnExistingNotification_whenReceiverOwningPostAndTypeAreGiven() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JANE);
        when(notificationRepository.findByReceiverAndOwningPostAndType(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name()))
                .thenReturn(Optional.of(NOTIFICATION_ONE));

        notificationService.removeNotification(
                USER_JOHN,
                POST_ONE,
                NotificationType.POST_LIKE.name()
        );

        verify(notificationRepository).save(any(Notification.class));
        assertThat(NOTIFICATION_ONE.getSender()).isNull();
    }

    @Test
    void shouldReturnListOfNotificationsForAuthUser() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(notificationRepository.findNotificationsByReceiver(
                USER_JOHN,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dateUpdated"))
        )).thenReturn(List.of(NOTIFICATION_ONE));

        List<Notification> returnedNotificationList = notificationService.getNotificationsForAuthUserPaginate(0, 5);

        assertThat(returnedNotificationList.size()).isEqualTo(1);
    }

    @Test
    void shouldMarkAllUnseenNotificationsAsSeen() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(notificationRepository.findNotificationsByReceiverAndIsSeenIsFalse(
                USER_JOHN)).thenReturn(List.of(NOTIFICATION_ONE));

        notificationService.markAllSeen();

        verify(notificationRepository, times(1)).save(any(Notification.class));
        assertThat(NOTIFICATION_ONE.getIsSeen()).isTrue();
        assertThat(NOTIFICATION_ONE.getIsSeen()).isTrue();
    }

    @Test
    void shouldMarkAllUnreadNotificationsAsRead() {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(notificationRepository.findNotificationsByReceiverAndIsReadIsFalse(
                USER_JOHN)).thenReturn(List.of(NOTIFICATION_ONE));

        notificationService.markAllRead();

        verify(notificationRepository, times(1)).save(any(Notification.class));
        assertThat(NOTIFICATION_ONE.getIsRead()).isTrue();
        assertThat(NOTIFICATION_ONE.getIsRead()).isTrue();
    }

    @Test
    void shouldDeleteNotification_whenReceiverOwningPostAndTypeAreGiven() {
        when(notificationRepository.findByReceiverAndOwningPostAndType(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name()))
                .thenReturn(Optional.of(NOTIFICATION_ONE));
        doNothing().when(notificationRepository).deleteById(NOTIFICATION_ONE.getId());

        notificationService.deleteNotification(USER_JOHN, POST_ONE, NotificationType.POST_LIKE.name());

        verify(notificationRepository).deleteById(NOTIFICATION_ONE.getId());
    }

    @Test
    void shouldDeleteNotification_whenOwningPostGiven() {
        doNothing().when(notificationRepository).deleteNotificationByOwningPost(POST_ONE);

        notificationService.deleteNotificationByOwningPost(POST_ONE);

        verify(notificationRepository).deleteNotificationByOwningPost(POST_ONE);
    }

    @Test
    void shouldDeleteNotification_whenOwningCommentGiven() {
        doNothing().when(notificationRepository).deleteNotificationByOwningComment(COMMENT_ONE);

        notificationService.deleteNotificationByOwningComment(COMMENT_ONE);

        verify(notificationRepository).deleteNotificationByOwningComment(COMMENT_ONE);
    }
}