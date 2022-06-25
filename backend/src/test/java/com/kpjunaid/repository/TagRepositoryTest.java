package com.kpjunaid.repository;

import com.kpjunaid.entity.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {
    @Autowired
    TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        Tag newTag = Tag.builder()
                .name("New Tag")
                .tagUseCounter(0)
                .build();
        tagRepository.save(newTag);
    }

    @AfterEach
    void tearDown() {
        tagRepository.deleteAll();
    }

    @Test
    void shouldReturnOptionalOfTag_whenNameIsGiven() {
        String name = "New Tag";
        Optional<Tag> returnedTag = tagRepository.findTagByName(name);

        assertThat(returnedTag.isPresent()).isTrue();
    }
}