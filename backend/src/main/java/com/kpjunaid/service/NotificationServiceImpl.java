package com.kpjunaid.service;

import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.exception.NotificationNotFoundException;
import com.kpjunaid.repository.NotificationRepository;
import com.kpjunaid.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Override
    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId).orElseThrow(NotificationNotFoundException::new);
    }

    @Override
    public Notification getNotificationByReceiverAndOwningPostAndType(User receiver, Post owningPost, String type) {
        return notificationRepository.findByReceiverAndOwningPostAndType(receiver, owningPost, type)
                .orElseThrow(NotificationNotFoundException::new);
    }

    @Override
    public void sendNotification(User receiver, User sender, Post owningPost, Comment owningComment, String type) {
        try {
            Notification targetNotification = getNotificationByReceiverAndOwningPostAndType(receiver, owningPost, type);
            targetNotification.setSender(sender);
            targetNotification.setIsSeen(false);
            targetNotification.setIsRead(false);
            targetNotification.setDateUpdated(new Date());
            targetNotification.setDateLastModified(new Date());
            notificationRepository.save(targetNotification);
        } catch (NotificationNotFoundException e) {
            Notification newNotification = new Notification();
            newNotification.setType(type);
            newNotification.setReceiver(receiver);
            newNotification.setSender(sender);
            newNotification.setOwningPost(owningPost);
            newNotification.setOwningComment(owningComment);
            newNotification.setIsSeen(false);
            newNotification.setIsRead(false);
            newNotification.setDateCreated(new Date());
            newNotification.setDateUpdated(new Date());
            newNotification.setDateLastModified(new Date());
            notificationRepository.save(newNotification);
        }
    }

    @Override
    public void removeNotification(User receiver, Post owningPost, String type) {
        User authUser = userService.getAuthenticatedUser();
        Notification targetNotification = getNotificationByReceiverAndOwningPostAndType(receiver, owningPost, type);
        if (targetNotification.getSender() != null && targetNotification.getSender().equals(authUser)) {
            targetNotification.setSender(null);
            targetNotification.setDateLastModified(new Date());
            notificationRepository.save(targetNotification);
        }
    }

    @Override
    public List<Notification> getNotificationsForAuthUserPaginate(Integer page, Integer size) {
        User authUser = userService.getAuthenticatedUser();
        return notificationRepository.findNotificationsByReceiver(
                authUser,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateUpdated"))
        );
    }

    @Override
    public void markAllSeen() {
        User authUser = userService.getAuthenticatedUser();
        notificationRepository.findNotificationsByReceiverAndIsSeenIsFalse(authUser)
                .forEach(notification -> {
                    if (notification.getReceiver().equals(authUser)) {
                        notification.setIsSeen(true);
                        notification.setDateLastModified(new Date());
                        notificationRepository.save(notification);
                    }
                });
    }

    @Override
    public void markAllRead() {
        User authUser = userService.getAuthenticatedUser();
        notificationRepository.findNotificationsByReceiverAndIsReadIsFalse(authUser)
                .forEach(notification -> {
                    if (notification.getReceiver().equals(authUser)) {
                        notification.setIsSeen(true);
                        notification.setIsRead(true);
                        notification.setDateLastModified(new Date());
                        notificationRepository.save(notification);
                    }
                });
    }

    @Override
    public void deleteNotification(User receiver, Post owningPost, String type) {
        Notification targetNotification = getNotificationByReceiverAndOwningPostAndType(receiver, owningPost, type);
        notificationRepository.deleteById(targetNotification.getId());
    }

    @Override
    public void deleteNotificationByOwningPost(Post owningPost) {
        notificationRepository.deleteNotificationByOwningPost(owningPost);
    }

    @Override
    public void deleteNotificationByOwningComment(Comment owningComment) {
        notificationRepository.deleteNotificationByOwningComment(owningComment);
    }
}
