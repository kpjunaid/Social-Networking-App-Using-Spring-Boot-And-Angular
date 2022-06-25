package com.kpjunaid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpjunaid.common.AppConstants;
import com.kpjunaid.common.UserPrincipal;
import com.kpjunaid.shared.MockResource;
import com.kpjunaid.shared.WithMockAuthUser;
import com.kpjunaid.dto.*;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.response.PostResponse;
import com.kpjunaid.response.UserResponse;
import com.kpjunaid.service.JwtTokenService;
import com.kpjunaid.service.PostService;
import com.kpjunaid.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    PostService postService;

    @MockBean
    JwtTokenService jwtTokenService;

    @MockBean
    AuthenticationManager authenticationManager;

    private final User USER_JOHN = MockResource.getMockUserJohn();
    private final User USER_JANE = MockResource.getMockUserJane();
    private final Post POST_ONE = MockResource.getPostOne();
    private final Post POST_TWO = MockResource.getPostTwo();
    private final String API_URL_PREFIX = "/api/v1";
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldSignupNewUser_whenValidSignupDtoIsGiven() throws Exception {
        SignupDto signupDto = SignupDto.builder()
                .email("johndoe@dom.com")
                .password("@P4ssword")
                .passwordRepeat("@P4ssword")
                .firstName("John")
                .lastName("Doe")
                .build();
        String signupDtoJson = mapper.writeValueAsString(signupDto);
        when(userService.createNewUser(any(SignupDto.class))).thenReturn(USER_JOHN);

        mockMvc.perform(post(API_URL_PREFIX + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(signupDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(USER_JOHN.getEmail()));
    }

    @Test
    void shouldLoginUser_whenValidLoginDtoIsGiven() throws Exception {
        String jwtToken = "jwt-token";
        LoginDto loginDto = LoginDto.builder()
                .email("johndoe@dom.com")
                .password("@P4ssword")
                .build();
        String loginDtoJson = mapper.writeValueAsString(loginDto);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserByEmail(USER_JOHN.getEmail())).thenReturn(USER_JOHN);
        when(jwtTokenService.generateToken(any(UserPrincipal.class))).thenReturn(jwtToken);

        mockMvc.perform(post(API_URL_PREFIX + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(loginDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_JOHN.getEmail()))
                .andExpect(header().string(AppConstants.TOKEN_HEADER, jwtToken));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnUserProfile_whenUserIdIsGiven() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(USER_JOHN);
        mockMvc.perform(get(API_URL_PREFIX + "/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_JOHN.getEmail()));
    }

    @Test
    @WithMockAuthUser
    void shouldUpdateUserInfo_whenValidUpdateUserInfoDtoIsGiven() throws Exception {
        UpdateUserInfoDto updateUserInfoDto = UpdateUserInfoDto.builder()
                .firstName("John")
                .lastName("Doe")
                .intro("Updated intro for John Doe")
                .build();
        String updateUserInfoDtoJson = mapper.writeValueAsString(updateUserInfoDto);
        when(userService.updateUserInfo(any(UpdateUserInfoDto.class))).thenReturn(USER_JOHN);
        mockMvc.perform(post(API_URL_PREFIX + "/account/update/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(updateUserInfoDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_JOHN.getEmail()));
    }

    @Test
    @WithMockAuthUser
    void shouldUpdateUserEmail_whenValidUpdateUserEmailDtoIsGiven() throws Exception {
        UpdateEmailDto updateEmailDto = UpdateEmailDto.builder()
                .email("updatedjohndoe@dom.com")
                .password(USER_JOHN.getPassword())
                .build();
        String updateEmailDtoJson = mapper.writeValueAsString(updateEmailDto);
        when(userService.updateEmail(any(UpdateEmailDto.class))).thenReturn(USER_JOHN);
        mockMvc.perform(post(API_URL_PREFIX + "/account/update/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(updateEmailDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldUpdateUserPassword_whenValidUpdateUserPasswordDtoIsGiven() throws Exception {
        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .password("@@P4ssword")
                .passwordRepeat("@@P4ssword")
                .oldPassword("@P4ssword")
                .build();
        String updatePasswordDtoJson = mapper.writeValueAsString(updatePasswordDto);
        when(userService.updatePassword(any(UpdatePasswordDto.class))).thenReturn(USER_JOHN);
        mockMvc.perform(post(API_URL_PREFIX + "/account/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(updatePasswordDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldUpdateProfilePhoto_whenProfilePhotoIsGiven() throws Exception {
        String fileContent = "some-file-content";
        MockMultipartFile profilePhoto = new MockMultipartFile("profilePhoto", fileContent.getBytes());
        when(userService.updateProfilePhoto(any(MultipartFile.class))).thenReturn(USER_JOHN);
        mockMvc.perform(multipart(API_URL_PREFIX + "/account/update/profile-photo")
                        .file(profilePhoto))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldUpdateCoverPhoto_whenProfilePhotoIsGiven() throws Exception {
        String fileContent = "some-file-content";
        MockMultipartFile coverPhoto = new MockMultipartFile("coverPhoto", fileContent.getBytes());
        when(userService.updateCoverPhoto(any(MultipartFile.class))).thenReturn(USER_JOHN);
        mockMvc.perform(multipart(API_URL_PREFIX + "/account/update/cover-photo")
                        .file(coverPhoto))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockAuthUser
    void shouldDeleteUserAccount() throws Exception {
        doNothing().when(userService).deleteUserAccount();
        mockMvc.perform(post(API_URL_PREFIX + "/account/delete"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenUserFollowsSomeone() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).followUser(userId);
        mockMvc.perform(post(API_URL_PREFIX + "/account/follow/" + userId)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenUserUnfollowsSomeone() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).unfollowUser(userId);
        mockMvc.perform(post(API_URL_PREFIX + "/account/unfollow/" + userId)
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldFollowingList_whenUserIdIsGiven() throws Exception {
        USER_JOHN.getFollowingUsers().add(USER_JANE);
        USER_JOHN.setFollowingCount(USER_JOHN.getFollowingCount()+1);

        when(userService.getFollowingUsersPaginate(USER_JOHN.getId(), 0, 5))
                .thenReturn(
                        List.of(new UserResponse(USER_JANE, true))
                );
        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}/following", USER_JOHN.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void shouldFollowerList_whenUserIdIsGiven() throws Exception {
        USER_JOHN.getFollowerUsers().add(USER_JANE);
        USER_JOHN.setFollowerCount(USER_JOHN.getFollowerCount()+1);

        when(userService.getFollowerUsersPaginate(USER_JOHN.getId(), 0, 5))
                .thenReturn(
                        List.of(new UserResponse(USER_JANE, true))
                );
        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}/follower", USER_JOHN.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnUserAccount_whenUserIdIsGiven() throws Exception {
        when(userService.getAuthenticatedUser()).thenReturn(USER_JOHN);
        when(userService.getUserById(USER_JOHN.getId())).thenReturn(USER_JOHN);

        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}", USER_JOHN.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(USER_JOHN.getEmail()));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnUserPostList_whenUserIdIsGiven() throws Exception {
        when(userService.getUserById(USER_JOHN.getId())).thenReturn(USER_JOHN);
        when(postService.getPostsByUserPaginate(USER_JOHN, 0, 5))
                .thenReturn(
                        List.of(
                                new PostResponse(POST_ONE, false),
                                new PostResponse(POST_TWO, false)
                        )
                );

        mockMvc.perform(get(API_URL_PREFIX + "/users/{userId}/posts", USER_JOHN.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfUsers_whenSearchedWithKey() throws Exception {
        when(userService.getUserSearchResult("Doe", 0, 5))
                .thenReturn(
                        List.of(
                                new UserResponse(USER_JOHN, false),
                                new UserResponse(USER_JOHN, false)
                        )
                );

        mockMvc.perform(get(API_URL_PREFIX + "/users/search")
                        .param("key", "Doe")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnOK_whenVerifyEmailWithToken() throws Exception {
        String jwtToken = "jwt-token";
        when(userService.verifyEmail(jwtToken)).thenReturn(USER_JOHN);

        mockMvc.perform(post(API_URL_PREFIX + "/verify-email/{token}", jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOK_whenForgotPasswordEmailIsSent() throws Exception {
        doNothing().when(userService).forgotPassword(USER_JOHN.getEmail());

        mockMvc.perform(post(API_URL_PREFIX + "/forgot-password")
                        .param("email", USER_JOHN.getEmail()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOK_whenResetPasswordWithTokenAndResetPasswordDto() throws Exception {
        String jwtToken = "jwt-token";
        ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
                .password("@P4ssword")
                .passwordRepeat("@P4ssword")
                .build();
        String resetPasswordDtoJson = mapper.writeValueAsString(resetPasswordDto);
        when(userService.resetPassword(jwtToken, resetPasswordDto)).thenReturn(USER_JOHN);

        mockMvc.perform(post(API_URL_PREFIX + "/reset-password/{token}", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(resetPasswordDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}