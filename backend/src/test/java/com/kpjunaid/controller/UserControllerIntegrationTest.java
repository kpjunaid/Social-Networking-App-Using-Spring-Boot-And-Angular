package com.kpjunaid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpjunaid.common.AppConstants;
import com.kpjunaid.common.UserPrincipal;
import com.kpjunaid.dto.*;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.repository.PostRepository;
import com.kpjunaid.repository.UserRepository;
import com.kpjunaid.service.JwtTokenService;
import com.kpjunaid.service.PostService;
import com.kpjunaid.service.UserService;
import com.kpjunaid.shared.MockResourceRepo;
import com.kpjunaid.shared.WithMockAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String API_URL_PREFIX = "/api/v1";
    private final User USER_JOHN = MockResourceRepo.getMockUserJohn();
    private final User USER_JANE = MockResourceRepo.getMockUserJane();
    private final Post POST_ONE = MockResourceRepo.getPostOne();
    private final Post POST_TWO = MockResourceRepo.getPostTwo();

    @BeforeEach
    void setUp() {
        USER_JOHN.setPassword(passwordEncoder.encode(USER_JOHN.getPassword()));
        userRepository.save(USER_JOHN);

        USER_JANE.setPassword(passwordEncoder.encode(USER_JANE.getPassword()));
        userRepository.save(USER_JANE);
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signup() throws Exception {
        SignupDto signupDto = SignupDto.builder()
                .email("mjunaidhira@gmail.com")
                .password("@P4ssword")
                .passwordRepeat("@P4ssword")
                .firstName("Junaid")
                .lastName("Khan Pathan")
                .build();
        String signupDtoJson = mapper.writeValueAsString(signupDto);

        mockMvc.perform(post(API_URL_PREFIX + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(signupDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("mjunaidhira@gmail.com"));
    }

    @Test
    void login() throws Exception {
        LoginDto loginDto = LoginDto.builder()
                .email(USER_JOHN.getEmail())
                .password("@P4ssword")
                .build();
        String loginDtoJson = mapper.writeValueAsString(loginDto);
        mockMvc.perform(post(API_URL_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(loginDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_JOHN.getEmail()))
                .andExpect(header().exists(AppConstants.TOKEN_HEADER));
    }

    @Test
    @WithMockAuthUser
    void showUserProfile() throws Exception {
        mockMvc.perform(get(API_URL_PREFIX + "/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_JOHN.getEmail()));
    }

    @Test
    @WithMockAuthUser
    void updateUserInfo() throws Exception {
        UpdateUserInfoDto updateUserInfoDto = UpdateUserInfoDto.builder()
                .firstName("John")
                .lastName("Doe")
                .intro("Updated intro for John Doe")
                .build();
        String updateUserInfoDtoJson = mapper.writeValueAsString(updateUserInfoDto);
        mockMvc.perform(post(API_URL_PREFIX + "/account/update/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(updateUserInfoDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intro").value("Updated intro for John Doe"));
    }

    @Test
    @WithMockAuthUser
    void updateUserEmail() throws Exception {
        UpdateEmailDto updateEmailDto = UpdateEmailDto.builder()
                .email("mjunaidhira@gmail.com")
                .password("@P4ssword")
                .build();
        String updateEmailDtoJson = mapper.writeValueAsString(updateEmailDto);
        mockMvc.perform(post(API_URL_PREFIX + "/account/update/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(updateEmailDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void updateUserPassword() throws Exception {
        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .password("@@P4ssword")
                .passwordRepeat("@@P4ssword")
                .oldPassword("@P4ssword")
                .build();
        String updatePasswordDtoJson = mapper.writeValueAsString(updatePasswordDto);
        mockMvc.perform(post(API_URL_PREFIX + "/account/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(updatePasswordDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void updateProfilePhoto() throws Exception {
        String fileContent = "some-file-content";
        MockMultipartFile profilePhoto = new MockMultipartFile(
                "profilePhoto",
                "photo.jpeg",
                "image/jpeg",
                fileContent.getBytes());
        mockMvc.perform(multipart(API_URL_PREFIX + "/account/update/profile-photo")
                        .file(profilePhoto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePhoto").isNotEmpty());
    }

    @Test
    @WithMockAuthUser
    void updateCoverPhoto() throws Exception {
        String fileContent = "some-file-content";
        MockMultipartFile coverPhoto = new MockMultipartFile(
                "coverPhoto",
                "photo.jpeg",
                "image/jpeg",
                fileContent.getBytes());
        mockMvc.perform(multipart(API_URL_PREFIX + "/account/update/cover-photo")
                        .file(coverPhoto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverPhoto").isNotEmpty());
    }

    @Test
    @WithMockAuthUser
    void deleteUserAccount() throws Exception {
        mockMvc.perform(post(API_URL_PREFIX + "/account/delete"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void followUser() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        mockMvc.perform(post(API_URL_PREFIX + "/account/follow/" + userJane.getId())
                        .param("userId", String.valueOf(userJane.getId())))
                .andExpect(status().isOk());

        User authUserJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        assertThat(authUserJohn.getFollowingCount()).isEqualTo(1);
    }

    @Test
    @WithMockAuthUser
    void unfollowUser() throws Exception {
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        userService.followUser(userJane.getId());

        mockMvc.perform(post(API_URL_PREFIX + "/account/unfollow/" + userJane.getId())
                        .param("userId", String.valueOf(userJane.getId())))
                .andExpect(status().isOk());

        User authUserJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        assertThat(authUserJohn.getFollowingCount()).isEqualTo(0);
    }

    @Test
    @WithMockAuthUser
    void getUserFollowingUsers() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        userService.followUser(userJane.getId());

        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}/following", userJohn.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void getUserFollowerUsers() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        User userJane = userRepository.findByEmail(USER_JANE.getEmail()).get();
        userService.followUser(userJane.getId());

        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}/follower", userJane.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void getUserById() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();

        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}", userJohn.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(userJohn.getEmail()));
    }

    @Test
    @WithMockAuthUser
    void getUserPosts() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();

        POST_ONE.setAuthor(userJohn);
        POST_TWO.setAuthor(userJohn);
        postRepository.save(POST_ONE);
        postRepository.save(POST_TWO);

        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}/posts", userJohn.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockAuthUser
    void searchUser() throws Exception {
        mockMvc.perform(get(API_URL_PREFIX + "/users/search")
                        .param("key", "Doe")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void verifyEmail() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        String jwtToken = jwtTokenService.generateToken(new UserPrincipal(userJohn));

        mockMvc.perform(post(API_URL_PREFIX + "/verify-email/{token}", jwtToken))
                .andExpect(status().isOk());

        User verifiedUserJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        assertThat(verifiedUserJohn.getEmailVerified()).isTrue();
    }

    @Test
    @Disabled
    void forgotPassword() throws Exception {
        mockMvc.perform(post(API_URL_PREFIX + "/forgot-password")
                        .param("email", USER_JOHN.getEmail()))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword() throws Exception {
        User userJohn = userRepository.findByEmail(USER_JOHN.getEmail()).get();
        String jwtToken = jwtTokenService.generateToken(new UserPrincipal(userJohn));
        ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
                .password("@P4ssword")
                .passwordRepeat("@P4ssword")
                .build();
        String resetPasswordDtoJson = mapper.writeValueAsString(resetPasswordDto);

        mockMvc.perform(post(API_URL_PREFIX + "/reset-password/{token}", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(resetPasswordDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}