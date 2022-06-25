package com.kpjunaid.controller;

import com.kpjunaid.entity.Country;
import com.kpjunaid.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @GetMapping("/countries")
    public ResponseEntity<?> getCountryList() {
        List<Country> countryList = countryService.getCountryList();
        return new ResponseEntity<>(countryList, HttpStatus.OK);
    }
}
