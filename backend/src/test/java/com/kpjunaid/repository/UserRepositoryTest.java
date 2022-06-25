package com.kpjunaid.repository;

import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
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
class UserRepositoryTest {
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

    @BeforeEach
    void setUp() {
        User userJohn = userRepository.save(USER_JOHN);
        User userJane = userRepository.save(USER_JANE);

        POST_ONE.setAuthor(userJohn);
        Post postOne = postRepository.save(POST_ONE);

        COMMENT_ONE.setAuthor(userJane);
        COMMENT_ONE.setPost(postOne);
        commentRepository.save(COMMENT_ONE);
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnOptionalOfUser_whenEmailIsGiven() {
        Optional<User> returnedUser = userRepository.findByEmail(USER_JOHN.getEmail());

        assertThat(returnedUser.isPresent()).isTrue();
    }

    @Test
    void shouldReturnListOfFollowingUsers_whenUserIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();

        userJohn.getFollowingUsers().add(userJane); // John is follower
        userJane.getFollowerUsers().add(userJohn); // Jane is followed
        User updatedJohn = userRepository.save(userJohn);
        User updatedJane = userRepository.save(userJane);

        List<User> johnFollowingUsers = userRepository.findUsersByFollowingUsers(
                updatedJane, // Jane is John's following user
                PageRequest.of(0, 5)
        );

        assertThat(johnFollowingUsers.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfFollowerUsers_whenUserIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();

        userJohn.getFollowingUsers().add(userJane); // John is follower
        userJane.getFollowerUsers().add(userJohn); // Jane is followed
        User updatedJohn = userRepository.save(userJohn);
        User updatedJane = userRepository.save(userJane);


        List<User> janeFollowerUsers = userRepository.findUsersByFollowerUsers(
                updatedJohn, // John is Jane's follower user
                PageRequest.of(0, 5)
        );

        assertThat(janeFollowerUsers.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfPostLikerUsers_whenPostIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post targetPost = postRepository.findAll().get(0);
        targetPost.getLikeList().add(userJohn);
        targetPost.setLikeCount(targetPost.getLikeCount()+1);
        Post updatedPost = postRepository.save(targetPost);

        List<User> postLikerUsers = userRepository.findUsersByLikedPosts(updatedPost, PageRequest.of(0, 5));

        assertThat(postLikerUsers.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfCommentLikerUsers_whenPostIsGiven() {
        Comment targetComment = commentRepository.findAll().get(0);
        targetComment.getLikeList().add(USER_JOHN);
        targetComment.setLikeCount(targetComment.getLikeCount()+1);
        Comment updatedComment = commentRepository.save(targetComment);

        List<User> commentLikerUsers = userRepository.findUsersByLikedComments(updatedComment, PageRequest.of(0, 5));

        assertThat(commentLikerUsers.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfUsers_whenSearchedWithName() {
        List<User> foundUsers = userRepository.findUsersByName("Doe", PageRequest.of(0, 5));

        assertThat(foundUsers.size()).isEqualTo(2);
    }
}