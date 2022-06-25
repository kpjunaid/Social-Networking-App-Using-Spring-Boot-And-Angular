package com.kpjunaid.shared;

import com.kpjunaid.entity.*;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.enumeration.Role;

import java.util.ArrayList;

public class MockResource {
    public static User getMockUserJohn() {
        return  User.builder()
                .id(1L)
                .email("johndoe@dom.com")
                .password("@P4ssword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER.name())
                .followerCount(0)
                .followingCount(0)
                .followerUsers(new ArrayList<>())
                .followingUsers(new ArrayList<>())
                .emailVerified(false)
                .accountVerified(false)
                .enabled(true)
                .gender("Male")
                .build();
    }

    public static User getMockUserJane() {
        return  User.builder()
                .id(2L)
                .email("janedoe@dom.com")
                .password("@P4ssword")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.ROLE_USER.name())
                .followerCount(0)
                .followingCount(0)
                .followerUsers(new ArrayList<>())
                .followingUsers(new ArrayList<>())
                .emailVerified(false)
                .accountVerified(false)
                .enabled(true)
                .gender("Female")
                .build();
    }

    public static Post getPostOne() {
        return Post.builder()
                .id(1L)
                .content("Post One")
                .postPhoto(null)
                .author(getMockUserJohn())
                .isTypeShare(false)
                .sharedPost(null)
                .postTags(new ArrayList<>())
                .likeCount(0)
                .shareCount(0)
                .commentCount(0)
                .likeList(new ArrayList<>())
                .postComments(new ArrayList<>())
                .shareList(new ArrayList<>())
                .build();
    }

    public static Post getPostTwo() {
        return Post.builder()
                .id(2L)
                .content("Post Two")
                .postPhoto(null)
                .author(getMockUserJohn())
                .isTypeShare(false)
                .sharedPost(null)
                .postTags(new ArrayList<>())
                .likeCount(0)
                .shareCount(0)
                .commentCount(0)
                .likeList(new ArrayList<>())
                .postComments(new ArrayList<>())
                .shareList(new ArrayList<>())
                .build();
    }

    public static Post getPostOneShare() {
        return Post.builder()
                .id(3L)
                .content("Post One Share")
                .postPhoto(null)
                .author(getMockUserJane())
                .isTypeShare(true)
                .sharedPost(getPostOne())
                .postTags(new ArrayList<>())
                .likeCount(0)
                .shareCount(0)
                .commentCount(0)
                .likeList(new ArrayList<>())
                .postComments(new ArrayList<>())
                .shareList(new ArrayList<>())
                .build();
    }

    public static Post getPostTwoShare() {
        return Post.builder()
                .id(4L)
                .content("Post Two Share")
                .postPhoto(null)
                .author(getMockUserJane())
                .isTypeShare(true)
                .sharedPost(null)
                .postTags(new ArrayList<>())
                .likeCount(0)
                .shareCount(0)
                .commentCount(0)
                .likeList(new ArrayList<>())
                .postComments(new ArrayList<>())
                .shareList(new ArrayList<>())
                .build();
    }

    public static Comment getCommentOne() {
        return Comment.builder()
                .id(1L)
                .content("Comment One")
                .author(getMockUserJohn())
                .post(getPostOne())
                .likeCount(0)
                .likeList(new ArrayList<>())
                .build();
    }

    public static Tag getTagOne() {
        return Tag.builder()
                .id(1L)
                .name("TagOne")
                .tagUseCounter(1)
                .taggedPosts(new ArrayList<>())
                .build();
    }

    public static Tag getTagTwo() {
        return Tag.builder()
                .id(2L)
                .name("TagTwo")
                .tagUseCounter(1)
                .taggedPosts(new ArrayList<>())
                .build();
    }

    public static Notification getNotificationOne() {
        return Notification.builder()
                .id(1L)
                .receiver(getMockUserJohn())
                .sender(getMockUserJane())
                .owningPost(getPostOne())
                .owningComment(null)
                .isSeen(false)
                .isRead(false)
                .type(NotificationType.POST_LIKE.name())
                .build();
    }

    public static Country getCountryBangladesh() {
        return Country.builder()
                .id(1L)
                .name("Bangladesh")
                .build();
    }
}
