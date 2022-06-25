package com.kpjunaid.service;

import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.exception.CommentNotFoundException;
import com.kpjunaid.response.CommentResponse;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.exception.InvalidOperationException;
import com.kpjunaid.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
    }

    @Override
    public Comment createNewComment(String content, Post post) {
        User authUser = userService.getAuthenticatedUser();
        Comment newComment = new Comment();
        newComment.setContent(content);
        newComment.setAuthor(authUser);
        newComment.setPost(post);
        newComment.setLikeCount(0);
        newComment.setDateCreated(new Date());
        newComment.setDateLastModified(new Date());
        return commentRepository.save(newComment);
    }

    @Override
    public Comment updateComment(Long commentId, String content) {
        User authUser = userService.getAuthenticatedUser();
        Comment targetComment = getCommentById(commentId);
        if (targetComment.getAuthor().equals(authUser)) {
            targetComment.setContent(content);
            targetComment.setDateLastModified(new Date());
            return commentRepository.save(targetComment);
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public void deleteComment(Long commentId) {
        User authUser = userService.getAuthenticatedUser();
        Comment targetComment = getCommentById(commentId);
        if (targetComment.getAuthor().equals(authUser)) {
            commentRepository.deleteById(commentId);
            notificationService.deleteNotificationByOwningComment(targetComment);
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public Comment likeComment(Long commentId) {
        User authUser = userService.getAuthenticatedUser();
        Comment targetComment = getCommentById(commentId);
        if (!targetComment.getLikeList().contains(authUser)) {
            targetComment.setLikeCount(targetComment.getLikeCount()+1);
            targetComment.getLikeList().add(authUser);
            targetComment.setDateLastModified(new Date());
            Comment updatedComment = commentRepository.save(targetComment);

            if (!targetComment.getAuthor().equals(authUser)) {
                notificationService.sendNotification(
                        targetComment.getAuthor(),
                        authUser,
                        targetComment.getPost(),
                        targetComment,
                        NotificationType.COMMENT_LIKE.name()
                );
            }

            return updatedComment;
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public Comment unlikeComment(Long commentId) {
        User authUser = userService.getAuthenticatedUser();
        Comment targetComment = getCommentById(commentId);
        if (targetComment.getLikeList().contains(authUser)) {
            targetComment.setLikeCount(targetComment.getLikeCount() - 1);
            targetComment.getLikeList().remove(authUser);
            targetComment.setDateLastModified(new Date());
            Comment updatedComment = commentRepository.save(targetComment);

            if (!targetComment.getAuthor().equals(authUser)) {
                notificationService.removeNotification(
                        targetComment.getPost().getAuthor(),
                        targetComment.getPost(),
                        NotificationType.COMMENT_LIKE.name()
                );
            }

            return updatedComment;
        } else {
            throw new InvalidOperationException();
        }
    }

    @Override
    public List<CommentResponse> getPostCommentsPaginate(Post post, Integer page, Integer size) {
        User authUser = userService.getAuthenticatedUser();
        List<Comment> foundCommentList = commentRepository.findByPost(
                post,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreated"))
        );

        List<CommentResponse> commentResponseList = new ArrayList<>();
        foundCommentList.forEach(comment -> {
            CommentResponse newCommentResponse = CommentResponse.builder()
                    .comment(comment)
                    .likedByAuthUser(comment.getLikeList().contains(authUser))
                    .build();
            commentResponseList.add(newCommentResponse);
        });

        return commentResponseList;
    }
}
