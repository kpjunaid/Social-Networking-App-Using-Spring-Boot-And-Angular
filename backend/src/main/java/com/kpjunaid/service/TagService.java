package com.kpjunaid.service;

import com.kpjunaid.entity.Tag;

import java.util.List;

public interface TagService {
    Tag getTagById(Long id);
    Tag getTagByName(String name);
    Tag createNewTag(String name);
    Tag increaseTagUseCounter(String name);
    Tag decreaseTagUseCounter(String name);
    List<Tag> getTimelineTags();
}
