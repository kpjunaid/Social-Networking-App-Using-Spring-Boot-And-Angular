package com.kpjunaid.service;

import com.kpjunaid.common.AppConstants;
import com.kpjunaid.common.UserPrincipal;
import com.kpjunaid.dto.*;
import com.kpjunaid.entity.Comment;
import com.kpjunaid.entity.Country;
import com.kpjunaid.entity.Post;
import com.kpjunaid.entity.User;
import com.kpjunaid.exception.UserNotFoundException;
import com.kpjunaid.mapper.MapstructMapperUpdate;
import com.kpjunaid.repository.UserRepository;
import com.kpjunaid.response.UserResponse;
import com.kpjunaid.shared.MockResource;
import com.kpjunaid.util.FileNamingUtil;
import com.kpjunaid.util.FileUploadUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DataJpaTest
class UserServiceTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    CountryService countryService;

    @Mock
    EmailService emailService;

    @Mock
    JwtTokenService jwtTokenService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    FileNamingUtil fileNamingUtil;

    @Mock
    FileUploadUtil fileUploadUtil;

    @Mock
    Environment environment;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Spy
    MapstructMapperUpdate mapstructMapperUpdate = Mappers.getMapper(MapstructMapperUpdate.class);

    private final User USER_JOHN = MockResource.getMockUserJohn();
    private final User USER_JANE = MockResource.getMockUserJane();
    private final Post POST_ONE = MockResource.getPostOne();
    private final Comment COMMENT_ONE = MockResource.getCommentOne();
    private final Country COUNTRY_BANGLADESH = MockResource.getCountryBangladesh();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnUser_whenUserIdIsGiven() {
        when(userRepository.findById(USER_JOHN.getId())).thenReturn(Optional.of(USER_JOHN));
        User returnedUser = userService.getUserById(USER_JOHN.getId());

        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser).isEqualTo(USER_JOHN);
    }

    @Test
    void shouldReturnUser_whenUserEmailIsGiven() {
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        User returnedUser = userService.getUserByEmail(USER_JOHN.getEmail());

        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser).isEqualTo(USER_JOHN);
    }

    @Test
    void shouldReturnListOfFollowerUsers_whenUserIsGiven() {
        USER_JOHN.getFollowerUsers().add(USER_JANE);
        USER_JANE.getFollowingUsers().add(USER_JOHN);

        when(userRepository.findById(USER_JOHN.getId())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findUsersByFollowingUsers(
                USER_JOHN,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "firstName", "lastName")))
        ).thenReturn(List.of(USER_JANE));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());

        List<UserResponse> returnedFollowerList = userService.getFollowerUsersPaginate(USER_JOHN.getId(), 0, 5);

        assertThat(returnedFollowerList).isNotNull();
        assertThat(returnedFollowerList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnListOfFollowingUsers_whenUserIsGiven() {
        USER_JOHN.getFollowingUsers().add(USER_JANE);
        USER_JANE.getFollowerUsers().add(USER_JOHN);

        when(userRepository.findById(USER_JOHN.getId())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findUsersByFollowerUsers(
                USER_JOHN,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "firstName", "lastName")))
        ).thenReturn(List.of(USER_JANE));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));

        List<UserResponse> returnedFollowerList = userService.getFollowingUsersPaginate(USER_JOHN.getId(), 0, 5);

        assertThat(returnedFollowerList).isNotNull();
        assertThat(returnedFollowerList.size()).isEqualTo(1);
    }

    @Test
    void shouldCreateNewUser_whenSignupDtoIsGiven() {
        String verifyEmailText = "Verify Email Text";

        SignupDto signupDto = SignupDto.builder()
                .email(USER_JOHN.getEmail())
                .password(USER_JANE.getPassword())
                .passwordRepeat(USER_JOHN.getPassword())
                .firstName(USER_JOHN.getFirstName())
                .lastName(USER_JOHN.getLastName())
                .build();

        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenThrow(UserNotFoundException.class);
        when(emailService.buildEmailVerifyMail(anyString())).thenReturn(verifyEmailText);
        doNothing().when(emailService).send(signupDto.getEmail(), AppConstants.VERIFY_EMAIL, verifyEmailText);
        when(userRepository.save(any(User.class))).thenReturn(USER_JOHN);

        User savedUser = userService.createNewUser(signupDto);

        verify(userRepository).save(any(User.class));
        assertThat(savedUser).isNotNull();
    }

    @Test
    void shouldUpdateUserInfo_whenUpdateUserInfoDtoIsGiven() {
        UpdateUserInfoDto updateUserInfoDto = UpdateUserInfoDto.builder()
                .intro("New User Intro")
                .countryName(COUNTRY_BANGLADESH.getName())
                .build();

        USER_JOHN.setCountry(COUNTRY_BANGLADESH);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(countryService.getCountryByName(updateUserInfoDto.getCountryName())).thenReturn(COUNTRY_BANGLADESH);
        when(userRepository.save(any(User.class))).thenReturn(USER_JOHN);

        User updatedUser = userService.updateUserInfo(updateUserInfoDto);

        verify(userRepository).save(any(User.class));
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCountry().getName()).isEqualTo(COUNTRY_BANGLADESH.getName());
    }

    @Test
    void shouldUpdateEmail_whenUpdateEmailDtoIsGiven() {
        String verifyEmailText = "Verify Email Text";

        UpdateEmailDto updateEmailDto = UpdateEmailDto.builder()
                .email("updatedjohndoe@dom.com")
                .password(USER_JOHN.getPassword())
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findByEmail(updateEmailDto.getEmail())).thenThrow(UserNotFoundException.class);
        when(passwordEncoder.matches(updateEmailDto.getPassword(), USER_JOHN.getPassword())).thenReturn(true);
        when(emailService.buildEmailVerifyMail(anyString())).thenReturn(verifyEmailText);
        doNothing().when(emailService).send(updateEmailDto.getEmail(), AppConstants.VERIFY_EMAIL, verifyEmailText);
        when(userRepository.save(any(User.class))).thenReturn(USER_JOHN);

        User updatedUser = userService.updateEmail(updateEmailDto);

        verify(userRepository).save(any(User.class));
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(updateEmailDto.getEmail());
    }

    @Test
    void shouldUpdatePassword_whenUpdatePasswordDtoIsGiven() {
        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .password("@@P4ssword")
                .passwordRepeat("@@P4ssword")
                .oldPassword(USER_JOHN.getPassword())
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(passwordEncoder.matches(updatePasswordDto.getOldPassword(), USER_JOHN.getPassword())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(USER_JOHN);

        User updatedUser = userService.updatePassword(updatePasswordDto);

        verify(userRepository).save(any(User.class));
        assertThat(updatedUser).isNotNull();
    }

    @Test
    void shouldVerifyEmail_whenVerificationTokenIsGiven() {
        String jwtToken = "jwt-token";

        when(jwtTokenService.getSubjectFromToken(jwtToken)).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.save(any(User.class))).thenReturn(USER_JOHN);

        User updatedUser = userService.verifyEmail(jwtToken);

        verify(userRepository).save(any(User.class));
        assertThat(updatedUser.getEmailVerified()).isTrue();
    }

    @Test
    void shouldChangeProfilePhoto_whenProfilePhotoIsGiven() throws IOException {
        String fileName = "photo-name.png";
        String fileContent = "some-file-content";
        String uploadProperty = "upload.user.images";
        String uploadDir = "upload-dir";
        String backendProperty = "app.root.backend";
        String backendUrl = "backend-url";

        MultipartFile profilePhoto = new MockMultipartFile(fileName, fileContent.getBytes());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(environment.getProperty(uploadProperty)).thenReturn(uploadDir);
        when(fileNamingUtil.nameFile(profilePhoto)).thenReturn(fileName);
        when(environment.getProperty(backendProperty)).thenReturn(backendUrl);
        doNothing().when(fileUploadUtil).saveNewFile(uploadDir, fileName, profilePhoto);
        when(userRepository.save(any((User.class)))).thenReturn(USER_JOHN);

        User updatedUser = userService.updateProfilePhoto(profilePhoto);

        verify(userRepository).save(any(User.class));
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getProfilePhoto()).isNotNull();
        assertThat(updatedUser.getProfilePhoto())
                .isEqualTo(backendUrl + File.separator + uploadDir + File.separator + fileName);
    }

    @Test
    void shouldChangeCoverPhoto_whenCoverPhotoIsGiven() throws IOException {
        String fileName = "photo-name.png";
        String fileContent = "some-file-content";
        String uploadProperty = "upload.user.images";
        String uploadDir = "upload-dir";
        String backendProperty = "app.root.backend";
        String backendUrl = "backend-url";

        MultipartFile coverPhoto = new MockMultipartFile(fileName, fileContent.getBytes());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(environment.getProperty(uploadProperty)).thenReturn(uploadDir);
        when(fileNamingUtil.nameFile(coverPhoto)).thenReturn(fileName);
        when(environment.getProperty(backendProperty)).thenReturn(backendUrl);
        doNothing().when(fileUploadUtil).saveNewFile(uploadDir, fileName, coverPhoto);
        when(userRepository.save(any((User.class)))).thenReturn(USER_JOHN);

        User updatedUser = userService.updateCoverPhoto(coverPhoto);

        verify(userRepository).save(any(User.class));
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getCoverPhoto()).isNotNull();
        assertThat(updatedUser.getCoverPhoto())
                .isEqualTo(backendUrl + File.separator + uploadDir + File.separator + fileName);
    }

    @Test
    void shouldDeleteUserAccount() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));

        doNothing().when(userRepository).deleteByEmail(USER_JOHN.getEmail());

        userService.deleteUserAccount();

        verify(userRepository, times(1)).deleteByEmail(USER_JOHN.getEmail());
    }

    @Test
    void shouldSendForgotPasswordEmail_whenValidEmailIsGiven() {
        String jwtToken = "jwt-token";
        String resetPasswordEmail = "Reset Password Email Text";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(jwtTokenService.generateToken(any(UserPrincipal.class))).thenReturn(jwtToken);
        when(emailService.buildResetPasswordMail(jwtToken)).thenReturn(resetPasswordEmail);
        doNothing().when(emailService)
                .send(USER_JOHN.getEmail(), AppConstants.RESET_PASSWORD, resetPasswordEmail);

        userService.forgotPassword(USER_JOHN.getEmail());

        verify(emailService).send(anyString(), anyString(), anyString());
    }

    @Test
    void shouldChangePassword_whenValidTokenAndResetPasswordDtoIsGiven() {
        String jwtToken = "jwt-token";
        ResetPasswordDto resetPasswordDto = ResetPasswordDto.builder()
                .password("@@P4ssword")
                .passwordRepeat("@@P4ssword")
                .build();

        when(jwtTokenService.getSubjectFromToken(jwtToken)).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(passwordEncoder.encode(resetPasswordDto.getPassword())).thenReturn(resetPasswordDto.getPasswordRepeat());
        when(userRepository.save(any(User.class))).thenReturn(USER_JOHN);

        userService.resetPassword(jwtToken, resetPasswordDto);

        verify(userRepository).save(any(User.class));
        assertThat(USER_JOHN.getPassword()).isEqualTo(resetPasswordDto.getPassword());
    }

    @Test
    void shouldFollowUser_whenUserIdIsGiven() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findById(USER_JANE.getId())).thenReturn(Optional.of(USER_JANE));
        when(userRepository.save(USER_JOHN)).thenReturn(USER_JOHN);
        when(userRepository.save(USER_JANE)).thenReturn(USER_JANE);

        userService.followUser(USER_JANE.getId());

        verify(userRepository, times(2)).save(any(User.class));
        assertThat(USER_JOHN.getFollowingUsers().size()).isEqualTo(1);
        assertThat(USER_JANE.getFollowerUsers().size()).isEqualTo(1);
    }

    @Test
    void shouldUnfollowUser_whenUserIdIsGiven() {
        USER_JOHN.getFollowingUsers().add(USER_JANE);
        USER_JOHN.setFollowingCount(USER_JOHN.getFollowingCount()+1);
        USER_JANE.getFollowerUsers().add(USER_JOHN);
        USER_JANE.setFollowerCount(USER_JANE.getFollowerCount()+1);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findById(USER_JANE.getId())).thenReturn(Optional.of(USER_JANE));
        when(userRepository.save(USER_JOHN)).thenReturn(USER_JOHN);
        when(userRepository.save(USER_JANE)).thenReturn(USER_JANE);

        userService.unfollowUser(USER_JANE.getId());

        verify(userRepository, times(2)).save(any(User.class));
        assertThat(USER_JOHN.getFollowingUsers().size()).isEqualTo(0);
        assertThat(USER_JANE.getFollowerUsers().size()).isEqualTo(0);
    }

    @Test
    void shouldReturnListUsers_whenSearchedWithName() {
        String key = "Doe";

        USER_JOHN.getFollowingUsers().add(USER_JANE);
        USER_JANE.getFollowerUsers().add(USER_JOHN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER_JOHN.getEmail());
        when(userRepository.findByEmail(USER_JOHN.getEmail())).thenReturn(Optional.of(USER_JOHN));
        when(userRepository.findUsersByName(
                key,
                PageRequest.of(0, 5)
        )).thenReturn(List.of(USER_JOHN, USER_JANE));

        List<UserResponse> returnedResultList = userService.getUserSearchResult(key, 0, 5);

        assertThat(returnedResultList.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnListOfPostLikerUsers_whenPostIsGiven() {
        POST_ONE.getLikeList().add(USER_JOHN);
        POST_ONE.setLikeCount(POST_ONE.getLikeCount()+1);
        POST_ONE.getLikeList().add(USER_JANE);
        POST_ONE.setLikeCount(POST_ONE.getLikeCount()+1);

        when(userRepository.findUsersByLikedPosts(
                POST_ONE,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "firstName", "lastName")))
        ).thenReturn(List.of(USER_JOHN, USER_JANE));

        List<User> returnedUserList = userService.getLikesByPostPaginate(POST_ONE, 0, 5);

        assertThat(returnedUserList.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnListOfCommentLikerUsers_whenCommentIsGiven() {
        COMMENT_ONE.getLikeList().add(USER_JOHN);
        COMMENT_ONE.setLikeCount(COMMENT_ONE.getLikeCount()+1);
        COMMENT_ONE.getLikeList().add(USER_JANE);
        COMMENT_ONE.setLikeCount(COMMENT_ONE.getLikeCount()+1);

        when(userRepository.findUsersByLikedComments(
                COMMENT_ONE,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "firstName", "lastName")))
        ).thenReturn(List.of(USER_JOHN, USER_JANE));

        List<User> returnedUserList = userService.getLikesByCommentPaginate(COMMENT_ONE, 0, 5);

        assertThat(returnedUserList.size()).isEqualTo(2);
    }
}
