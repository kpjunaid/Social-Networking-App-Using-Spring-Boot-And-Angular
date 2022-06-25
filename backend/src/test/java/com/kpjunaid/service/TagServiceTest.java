package com.kpjunaid.service;

import com.kpjunaid.entity.Tag;
import com.kpjunaid.exception.TagNotFoundException;
import com.kpjunaid.repository.TagRepository;
import com.kpjunaid.shared.MockResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
class TagServiceTest {
    @InjectMocks
    TagServiceImpl tagService;

    @Mock
    TagRepository tagRepository;
    private final Tag TAG_ONE = MockResource.getTagOne();
    private final Tag TAG_TWO = MockResource.getTagTwo();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnTag_whenTagIdIsGiven() {
        when(tagRepository.findById(TAG_ONE.getId())).thenReturn(Optional.of(TAG_ONE));

        Tag returnedTag = tagService.getTagById(TAG_ONE.getId());

        assertThat(returnedTag).isEqualTo(TAG_ONE);
    }

    @Test
    void shouldReturnTag_whenTagNameIsGiven() {
        when(tagRepository.findTagByName(TAG_ONE.getName())).thenReturn(Optional.of(TAG_ONE));

        Tag returnedTag = tagService.getTagByName(TAG_ONE.getName());

        assertThat(returnedTag).isEqualTo(TAG_ONE);
    }

    @Test
    void shouldCreateNewTag_whenTagNameIsGiven() {
        when(tagRepository.findTagByName(TAG_ONE.getName())).thenThrow(TagNotFoundException.class);
        when(tagRepository.save(any(Tag.class))).thenReturn(TAG_ONE);

        tagService.createNewTag(TAG_ONE.getName());

        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void shouldIncreaseTagUseCounter_whenTagNameIsGiven() {
        when(tagRepository.findTagByName(TAG_ONE.getName())).thenReturn(Optional.of(TAG_ONE));
        when(tagRepository.save(any(Tag.class))).thenReturn(TAG_ONE);

        Tag updatedTag = tagService.increaseTagUseCounter(TAG_ONE.getName());

        verify(tagRepository).save(any(Tag.class));
        assertThat(updatedTag.getTagUseCounter()).isEqualTo(2);
    }

    @Test
    void shouldDecreaseTagUseCounter_whenTagNameIsGiven() {
        when(tagRepository.findTagByName(TAG_ONE.getName())).thenReturn(Optional.of(TAG_ONE));
        when(tagRepository.save(any(Tag.class))).thenReturn(TAG_ONE);

        Tag updatedTag = tagService.decreaseTagUseCounter(TAG_ONE.getName());

        verify(tagRepository).save(any(Tag.class));
        assertThat(updatedTag.getTagUseCounter()).isEqualTo(0);
    }

    @Test
    void shouldReturnListOfTagsOrderedByTagUseCounter() {
        when(tagRepository.findAll(PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "tagUseCounter"))))
                .thenReturn(
                        new PageImpl<>(List.of(TAG_ONE, TAG_TWO), PageRequest.of(0, 10,
                            Sort.by(Sort.Direction.DESC, "tagUseCounter")), 2));

        List<Tag> returnedTagList = tagService.getTimelineTags();

        assertThat(returnedTagList.size()).isEqualTo(2);
    }
}