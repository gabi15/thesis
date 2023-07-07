package com.thesis.service.users.controllers;

import com.thesis.service.users.dto.CredentialsDto;
import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.dto.UserRegistrationRequest;
import com.thesis.service.users.entities.ServiceUser;
import com.thesis.service.users.exceptions.AppException;
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
        UserDto user;
        try {
            user = userService.signIn(credentialsDto);
        }
        catch(AppException e){
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        String fingerprintCookie = "__FakeSecure-Fgp=" + user.getFingerprintCookie()
                + ";Path=/; SameSite=Strict; HttpOnly; Secure";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Set-Cookie", fingerprintCookie);
        return ResponseEntity.ok().headers(responseHeaders).body(user);
    }

    @PostMapping("/validateCookieToken")
    public ResponseEntity<Object> validateCookieToken(HttpServletRequest request, @RequestParam String token) {
        log.info("Trying to validate token {}", token);
        ServiceUser user;
        try{
            user = userService.validateCookieToken(request, token);
        }
        catch (AppException e){
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationRequest request){
        try{
            userService.register(request);
        }
        catch (AppException e){
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return new ResponseEntity<String>("created new user", HttpStatus.CREATED);
    }

    @PostMapping("/hello")
    public ResponseEntity<String> hello(@RequestBody UserRegistrationRequest request)
    {
        return new ResponseEntity<String>("created new hello", HttpStatus.CREATED);
    }
}
