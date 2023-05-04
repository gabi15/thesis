package com.thesis.service.users.mappers;


import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.entities.ServiceUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "fingerprintCookie", target = "fingerprintCookie")
    UserDto toUserDto(ServiceUser user, String token, String fingerprintCookie);
}
