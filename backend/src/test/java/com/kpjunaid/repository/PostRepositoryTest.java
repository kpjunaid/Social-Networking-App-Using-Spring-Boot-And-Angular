package com.kpjunaid.repository;

import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.Tag;
import com.kpjunaid.entity.User;
import com.kpjunaid.shared.MockResourceRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {
    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TagRepository tagRepository;

    private final User USER_JOHN = MockResourceRepo.getMockUserJohn();
    private final User USER_JANE = MockResourceRepo.getMockUserJane();
    private final Post POST_ONE = MockResourceRepo.getPostOne();
    private final Post POST_TWO = MockResourceRepo.getPostTwo();
    private final Post POST_ONE_SHARE = MockResourceRepo.getPostOneShare();
    private final Post POST_TWO_SHARE = MockResourceRepo.getPostTwoShare();
    private final Tag TAG_ONE = MockResourceRepo.getTagOne();

    @BeforeEach
    void setUp() {
        User userJohn = userRepository.save(USER_JOHN);
        User userJane = userRepository.save(USER_JANE);

        Tag tagOne = tagRepository.save(TAG_ONE);

        POST_ONE.setAuthor(userJohn);
        POST_ONE.getPostTags().add(tagOne);
        Post postOne = postRepository.save(POST_ONE);

        tagOne.setTagUseCounter(tagOne.getTagUseCounter()+1);
        tagRepository.save(tagOne);

        POST_ONE_SHARE.setAuthor(userJane);
        POST_ONE_SHARE.setSharedPost(postOne);
        postRepository.save(POST_ONE_SHARE);
        postOne.setShareCount(postOne.getShareCount()+1);
        postRepository.save(postOne);

        POST_TWO.setAuthor(userJohn);
        Post postTwo = postRepository.save(POST_TWO);

        POST_TWO_SHARE.setAuthor(userJane);
        POST_TWO_SHARE.setSharedPost(postTwo);
        postRepository.save(POST_TWO_SHARE);
        postTwo.setShareCount(postTwo.getShareCount()+1);
        postRepository.save(postTwo);
    }

    @AfterEach
    void tearDown() {
        tagRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnListOfPosts_whenUserIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        List<Post> foundPosts = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5));

        assertThat(foundPosts.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnListOfPosts_whenFollowingAndOwnUserIdIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        List<Post> foundPosts = postRepository.findPostsByAuthorIdIn(List.of(userJohn.getId()), PageRequest.of(0, 5));

        assertThat(foundPosts.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnListOfPostShares_whenSharedPostIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Post sharedPost = postRepository.findPostsByAuthor(userJohn, PageRequest.of(0, 5)).get(0);

        List<Post> foundPostShares = postRepository.findPostsBySharedPost(sharedPost,  PageRequest.of(0, 5));

        assertThat(foundPostShares.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfPosts_whenTagIsGiven() {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        Tag targetTag = tagRepository.findAll().get(0);

        List<Post> foundPosts = postRepository.findPostsByPostTags(targetTag,  PageRequest.of(0, 5));

        assertThat(foundPosts.size()).isEqualTo(1);
    }
}