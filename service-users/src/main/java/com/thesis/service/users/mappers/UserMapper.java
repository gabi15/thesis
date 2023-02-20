package com.thesis.service.users.mappers;


import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.entities.BookstoreUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "token", target = "token")
    UserDto toUserDto(BookstoreUser user, String token);
}
