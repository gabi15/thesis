package com.thesis.service.users.services;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.thesis.service.users.dto.CredentialsDto;
import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.dto.UserRegistrationRequest;
import com.thesis.service.users.entities.ServiceUser;
import com.thesis.service.users.exceptions.AppException;
import com.thesis.service.users.mappers.UserMapper;
import com.thesis.service.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.CharBuffer;
import java.util.*;

import com.auth0.jwt.JWT;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    public UserDto signIn(CredentialsDto credentialsDto) {
        ServiceUser user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
            String token = createToken(user);

            return userMapper.toUserDto(user, token);
        }

        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public ServiceUser validateToken(String token) {

        //decode the token
        DecodedJWT decodedToken;
        try {
            decodedToken= JWT.decode(token);
        } catch (JWTDecodeException e) {
            throw new AppException(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        String login = decodedToken.getSubject();
        Optional<ServiceUser> userOptional = userRepository.findByLogin(login);

        if (userOptional.isEmpty()) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }

        ServiceUser user = userOptional.get();
        return user;
    }

    private String createToken(ServiceUser user) {
        String userLogin = user.getLogin();

        Calendar c = Calendar.getInstance();
        Date nowDate = c.getTime();
        c.add(Calendar.MINUTE, 15);
        Date expirationDate = c.getTime();
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("typ", "JWT");
        String token = JWT.create().withSubject(userLogin)
                .withExpiresAt(expirationDate)
                .withIssuer("invoice_server")
                .withIssuedAt(nowDate)
                .withNotBefore(nowDate)
                .withHeader(headerClaims)
                .sign(Algorithm.HMAC256(secretKey));

        return token;
    }

    public void register(UserRegistrationRequest request) {
        ServiceUser user = ServiceUser.builder()
                .login(request.getLogin())
                .build();
        if (userRepository.existsServiceUserByLogin(request.getLogin())) {
            throw new AppException("User with login " + request.getLogin() + " already exists", HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
