package com.kpjunaid.controller;

import com.kpjunaid.entity.Country;
import com.kpjunaid.repository.CountryRepository;
import com.kpjunaid.service.CountryService;
import com.kpjunaid.shared.WithMockAuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CountryControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CountryService countryService;

    private final String API_URL_PREFIX = "/api/v1";

    @BeforeEach
    void setUp() {
        countryRepository.save(Country.builder().name("Bangladesh").build());
    }

    @AfterEach
    void tearDown() {
        countryRepository.deleteAll();
    }

    @Test
    @WithMockAuthUser
    void getCountryList() throws Exception {
        mockMvc.perform(get(API_URL_PREFIX + "/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}