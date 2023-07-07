package com.thesis.service.users.services;

import com.thesis.service.users.dto.CredentialsDto;
import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.dto.UserRegistrationRequest;
import com.thesis.service.users.entities.ServiceUser;
import com.thesis.service.users.exceptions.AppException;
import com.thesis.service.users.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;


@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void testSignInCorrectCredentials() {
        ServiceUser user = ServiceUser.builder()
                .login("username")
                .build();
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        char [] passwordChar = "password".toCharArray();
        CredentialsDto credentialsDto = new CredentialsDto("username", passwordChar);
        UserDto result = userService.signIn(credentialsDto);
        assert (result.getLogin().equals("username"));
    }

    @Test
    void testSignInIncorrectCredentials() {
        ServiceUser user = ServiceUser.builder()
                .login("username")
                .build();
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        char [] passwordChar = "wrong_password".toCharArray();
        CredentialsDto credentialsDto = new CredentialsDto("username", passwordChar);
        Exception exception = Assertions.assertThrows(AppException.class, () -> {
            userService.signIn(credentialsDto);
        });

        String expectedMessage = "Invalid password";
        String actualMessage = exception.getMessage();
        assert (expectedMessage.equals(actualMessage));

    }

    @Test
    void testRegister() {
        String username = "username";
        String password = "password";
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(username, password);
        userService.register(userRegistrationRequest);
        Optional<ServiceUser> serviceUser = userRepository.findByLogin("username");
        assert(serviceUser.isPresent());
        assert (serviceUser.get().getLogin().equals(username));
        assert !(serviceUser.get().getPassword().equals(password));
    }

    @Test
    void testRegisterUserAlreadyExists() {
        ServiceUser user = ServiceUser.builder()
                .login("username")
                .build();
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        String username = "username";
        String password = "password";
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest(username, password);
        Exception exception = Assertions.assertThrows(AppException.class, () -> {
            userService.register(userRegistrationRequest);
        });

        String expectedMessage = "User with login username already exists";
        String actualMessage = exception.getMessage();
        assert (expectedMessage.equals(actualMessage));

    }

}
