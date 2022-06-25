package com.kpjunaid.controller;

import com.kpjunaid.shared.MockResource;
import com.kpjunaid.shared.WithMockAuthUser;
import com.kpjunaid.entity.Country;
import com.kpjunaid.service.CountryService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CountryControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CountryService countryService;

    private final Country COUNTRY_BANGLADESH  = MockResource.getCountryBangladesh();
    private final String API_URL_PREFIX = "/api/v1";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @WithMockAuthUser
    void shouldReturnListOfCountries() throws Exception {
        when(countryService.getCountryList()).thenReturn(List.of(COUNTRY_BANGLADESH));

        mockMvc.perform(get(API_URL_PREFIX + "/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}