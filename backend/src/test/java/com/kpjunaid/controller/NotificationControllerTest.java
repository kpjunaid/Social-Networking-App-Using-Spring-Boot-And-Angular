package com.kpjunaid.controller;

import com.kpjunaid.shared.MockResource;
import com.kpjunaid.shared.WithMockAuthUser;
import com.kpjunaid.entity.Notification;
import com.kpjunaid.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationService notificationService;

    private final Notification NOTIFICATION_ONE = MockResource.getNotificationOne();
    private final String API_URL_PREFIX = "/api/v1";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfNotifications() throws Exception {
        when(notificationService.getNotificationsForAuthUserPaginate(0, 5))
                .thenReturn(List.of(NOTIFICATION_ONE));

        mockMvc.perform(get(API_URL_PREFIX + "/notifications")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenNotificationsAreMarkedSeen() throws Exception {
        doNothing().when(notificationService).markAllSeen();

        mockMvc.perform(post(API_URL_PREFIX + "/notifications/mark-seen"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser
    void shouldReturnOK_whenNotificationsAreMarkedRead() throws Exception {
        doNothing().when(notificationService).markAllRead();

        mockMvc.perform(post(API_URL_PREFIX + "/notifications/mark-read"))
                .andExpect(status().isOk());
    }
}