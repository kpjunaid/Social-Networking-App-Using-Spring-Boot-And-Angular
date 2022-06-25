package com.kpjunaid.common;

public class AppConstants {

    public static final Integer FOLLOWER_PER_PAGE = 20;
    public static final Integer FOLLOWING_PER_PAGE = 20;
    public static final Integer POST_PER_PAGE = 20;
    public static final Integer CONTENT_PER_PAGE = 20;
    public static final Integer MAX_POST_TAGS = 5;
    public static final Long JWT_EXPIRATION_2Wk = 14 * 86400000L; // 2 Weeks
    public static final Long JWT_EXPIRATION_1d = 86400000L; // 1 Day
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Jwt-Token";
    public static final String OPTIONS_HTTP_METHOD = "options";
    public static final String AUTHORITIES = "authorities";
    public static final String TOKEN_UNVERIFIABLE = "Token cannot be verified.";
    public static final String INVALID_TOKEN = "Token is not valid.";
    public static final String FORBIDDEN = "You need to be logged in to access this resource.";
    public static final String INVALID_OPERATION = "You cannot perform this operation.";
    public static final String ACCESS_DENIED = "You don't have permission to access this resource.";
    public static final String ACCOUNT_LOCKED = "Your account has been locked.";
    public static final String METHOD_NOT_ALLOWED = "This operation is not allowed. Only %s operations are allowed.";
    public static final String NOT_FOUND_ERROR = "404 Not Found.";
    public static final String INTERNAL_SERVER_ERROR = "An error occurred while processing your request.";
    public static final String INCORRECT_CREDENTIALS = "Incorrect username or password.";
    public static final String ACCOUNT_DISABLED = "Your account has been disabled.";
    public static final String FILE_PROCESSING_ERROR = "There was an error while processing your file.";
    public static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission to perform this action.";
    public static final String VERIFY_EMAIL = "Verify your email";
    public static final String RESET_PASSWORD = "Reset your password";
    public static final String[] PUBLIC_URLS = {
            "/api/v1/signup",
            "/api/v1/login",
            "/api/v1/verify-email/**",
            "/api/v1/forgot-password",
            "/api/v1/reset-password/**",
            "/images/**",
            "/uploads/**"
    };
    public static final String EMAIL_NOT_FOUND = "Email address does not exist.";
    public static final String EMAIL_EXISTS = "User exists with this email address.";
    public static final String SAME_EMAIL = "You cannot update with your existing email.";
    public static final String USER_NOT_FOUND = "No user found.";
    public static final String POST_NOT_FOUND = "No post found.";
    public static final String TAG_NOT_FOUND = "No post found.";
    public static final String TAG_EXISTS = "Tag already exists.";
    public static final String COUNTRY_NOT_FOUND = "No post found.";
    public static final String COUNTRY_EXISTS = "Country already exists.";
    public static final String EMPTY_POST = "Post does not contain any content";
    public static final String SHARE_NOT_FOUND = "No share found.";
    public static final String SHARE_EXISTS = "You have already shared this post.";
    public static final String DUPLICATE_SHARE = "You have already shared this post.";
    public static final String NOTIFICATION_NOT_FOUND = "No notification found.";

    public static final String EMAIL_PATTERN = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{6,32}$";
}
