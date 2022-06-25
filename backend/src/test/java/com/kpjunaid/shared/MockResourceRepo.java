package com.kpjunaid.shared;

import com.kpjunaid.entity.*;
import com.kpjunaid.enumeration.NotificationType;
import com.kpjunaid.enumeration.Role;

import java.util.ArrayList;

public class MockResourceRepo {
    public static User getMockUserJohn() {
        return  User.builder()
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
                .content("Post One")
                .postPhoto(null)
                .author(null)
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
                .content("Post Two")
                .postPhoto(null)
                .author(null)
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
                .content("Post One Share")
                .postPhoto(null)
                .author(null)
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

    public static Post getPostTwoShare() {
        return Post.builder()
                .content("Post Two Share")
                .postPhoto(null)
                .author(null)
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
                .content("Comment One")
                .author(null)
                .post(null)
                .likeCount(0)
                .likeList(new ArrayList<>())
                .build();
    }

    public static Tag getTagOne() {
        return Tag.builder()
                .name("TagOne")
                .tagUseCounter(0)
                .taggedPosts(new ArrayList<>())
                .build();
    }

    public static Tag getTagTwo() {
        return Tag.builder()
                .name("TagTwo")
                .tagUseCounter(0)
                .taggedPosts(new ArrayList<>())
                .build();
    }

    public static Notification getNotificationOne() {
        return Notification.builder()
                .receiver(null)
                .sender(null)
                .owningPost(null)
                .owningComment(null)
                .isSeen(false)
                .isRead(false)
                .type(null)
                .build();
    }

    public static Country getCountryBangladesh() {
        return Country.builder()
                .name("Bangladesh")
                .build();
    }
}
