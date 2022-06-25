package com.kpjunaid.service;

import com.kpjunaid.entity.Country;

import java.util.List;

public interface CountryService {
    Country getCountryById(Long id);
    Country getCountryByName(String name);
    List<Country> getCountryList();
}
