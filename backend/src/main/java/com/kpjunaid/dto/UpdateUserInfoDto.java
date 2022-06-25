package com.kpjunaid.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoDto {
    @NotEmpty
    @Size(max = 64)
    private String firstName;

    @NotEmpty
    @Size(max = 64)
    private String lastName;

    @Size(max = 100)
    private String intro;

    @Size(max = 16)
    private String gender;

    @Size(max = 64)
    private String hometown;

    @Size(max = 64)
    private String currentCity;

    @Size(max = 128)
    private String eduInstitution;

    @Size(max = 128)
    private String workplace;

    @Size(max = 64)
    private String countryName;

    @Past
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthDate;
}
