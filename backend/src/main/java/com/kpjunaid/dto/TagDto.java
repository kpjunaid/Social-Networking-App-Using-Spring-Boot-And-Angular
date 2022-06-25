package com.kpjunaid.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {
    @NotEmpty
    private String tagName;

    @NotEmpty
    private String action;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagDto tagDto = (TagDto) o;
        return Objects.equals(tagName, tagDto.tagName) && Objects.equals(action, tagDto.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, action);
    }
}
