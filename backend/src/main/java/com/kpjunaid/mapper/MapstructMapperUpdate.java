package com.kpjunaid.mapper;

import com.kpjunaid.entity.User;
import com.kpjunaid.dto.UpdateUserInfoDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MapstructMapperUpdate {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "joinDate", ignore = true)
    @Mapping(target = "accountVerified", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "profilePhoto", ignore = true)
    @Mapping(target = "country", ignore = true)
    void updateUserFromUserUpdateDto(UpdateUserInfoDto updateUserInfoDto, @MappingTarget User user);
}
