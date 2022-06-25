package com.kpjunaid.controller;

import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.Tag;
import com.kpjunaid.entity.User;
import com.kpjunaid.repository.PostRepository;
import com.kpjunaid.repository.TagRepository;
import com.kpjunaid.repository.UserRepository;
import com.kpjunaid.service.UserService;
import com.kpjunaid.shared.MockResourceRepo;
import com.kpjunaid.shared.WithMockAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TimelineControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

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
        tagRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockAuthUser
    void getTimelinePosts() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();

        userService.followUser(userJane.getId());

        mockMvc.perform(get(API_URL_PREFIX + "/")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockAuthUser
    void getTimelineTags() throws Exception {
        tagRepository.save(Tag.builder().name("TagOne").tagUseCounter(1).build());
        tagRepository.save(Tag.builder().name("TagTwo").tagUseCounter(3).build());

        mockMvc.perform(get(API_URL_PREFIX + "/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].name").value("TagTwo"));
    }
}