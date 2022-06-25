package com.kpjunaid.service;

import com.kpjunaid.entity.Country;
import com.kpjunaid.repository.CountryRepository;
import com.kpjunaid.shared.MockResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
class CountryServiceTest {
    @InjectMocks
    CountryServiceImpl countryService;

    @Mock
    CountryRepository countryRepository;

    private final Country COUNTRY_BANGLADESH = MockResource.getCountryBangladesh();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnCountry_whenCountryIdIsGiven() {
        when(countryRepository.findById(COUNTRY_BANGLADESH.getId())).thenReturn(Optional.of(COUNTRY_BANGLADESH));

        Country returnedCountry = countryService.getCountryById(COUNTRY_BANGLADESH.getId());

        assertThat(returnedCountry).isNotNull();
        assertThat(returnedCountry).isEqualTo(COUNTRY_BANGLADESH);
    }

    @Test
    void shouldReturnCountry_whenCountryNameIsGiven() {
        when(countryRepository.findByName(COUNTRY_BANGLADESH.getName())).thenReturn(Optional.of(COUNTRY_BANGLADESH));

        Country returnedCountry = countryService.getCountryByName(COUNTRY_BANGLADESH.getName());

        assertThat(returnedCountry).isNotNull();
        assertThat(returnedCountry).isEqualTo(COUNTRY_BANGLADESH);
    }

    @Test
    void shouldReturnListOfCountriesOrderedByNameAscending() {
        when(countryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")))
                .thenReturn(List.of(COUNTRY_BANGLADESH));

        List<Country> returnedCountryList = countryService.getCountryList();

        assertThat(returnedCountryList).isNotNull();
        assertThat(returnedCountryList.size()).isEqualTo(1);
    }
}