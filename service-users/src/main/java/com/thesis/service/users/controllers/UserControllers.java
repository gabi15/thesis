package com.thesis.service.users.controllers;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.thesis.service.users.dto.CredentialsDto;
import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.dto.UserRegistrationRequest;
import com.thesis.service.users.dto.UserWithoutId;
import com.thesis.service.users.entities.ServiceUser;
import com.thesis.service.users.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/users")
public class UserControllers {

    private final UserService userService;

    @PostMapping("/signIn")
    public ResponseEntity<Object> signIn(@RequestBody CredentialsDto credentialsDto) {
        log.info("Trying to login {}", credentialsDto.getLogin());

        //Add the fingerprint in a hardened cookie - Add cookie manually because
        //SameSite attribute is not supported by javax.servlet.http.Cookie class
        UserDto user = userService.signIn(credentialsDto);
        String fingerprintCookie = "__FakeSecure-Fgp=" + user.getFingerprintCookie()
                + ";Path=/; SameSite=Strict; HttpOnly; Secure";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Set-Cookie", fingerprintCookie);
        return ResponseEntity.ok().headers(responseHeaders).body(user);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<UserDto> signIn(@RequestParam String token) {
        log.info("Trying to validate token {}", token);
        return ResponseEntity.ok(userService.validateToken(token));
    }

    @PostMapping("/validateCookieToken")
    public ResponseEntity<DecodedJWT> validateCookieToken(HttpServletRequest request, @RequestParam String token) {
        log.info("Trying to validate token {}", token);
        return ResponseEntity.ok(userService.validateCookieToken(request, token));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationRequest request){
        userService.register(request);
        return new ResponseEntity<String>("created new user", HttpStatus.CREATED);
    }

    @PostMapping("/hello")
    public ResponseEntity<String> hello(@RequestBody UserRegistrationRequest request)
    {
        return new ResponseEntity<String>("created new hello", HttpStatus.CREATED);
    }
}
