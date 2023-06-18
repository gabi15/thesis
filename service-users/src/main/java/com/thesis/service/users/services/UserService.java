package com.thesis.service.users.services;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.thesis.service.users.dto.CredentialsDto;
import com.thesis.service.users.dto.UserDto;
import com.thesis.service.users.dto.UserRegistrationRequest;
import com.thesis.service.users.entities.ServiceUser;
import com.thesis.service.users.exceptions.AppException;
import com.thesis.service.users.mappers.UserMapper;
import com.thesis.service.users.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    private transient String keyHMAC;
    // Random data generator
    private SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        keyHMAC = Base64.getEncoder().encodeToString(secretKey.getBytes());

    }

    public UserDto signIn(CredentialsDto credentialsDto) {
        ServiceUser user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
            String fingerprint = createUserFingerprint();
            String token = createToken(user, fingerprint);

            return userMapper.toUserDto(user, token, fingerprint);
        }

        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public DecodedJWT validateCookieToken(HttpServletRequest request, String token){
        //Retrieve the user fingerprint from the dedicated cookie
        String userFingerprint = null;
        if (request.getCookies() != null && request.getCookies().length > 0) {
            List<Cookie> cookies = Arrays.stream(request.getCookies()).toList();
            Optional<Cookie> cookie = cookies.stream().filter(c -> "__FakeSecure-Fgp"
                    .equals(c.getName())).findFirst();
            if (cookie.isPresent()) {
                userFingerprint = cookie.get().getValue();
            }
        }

        //Compute a SHA256 hash of the received fingerprint in cookie in order to compare
        //it to the fingerprint hash stored in the token
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e){
            System.out.println("dupa");
        }
        byte[] userFingerprintDigest=null;
        try {
            userFingerprintDigest = digest.digest(userFingerprint.getBytes("utf-8"));
        }
        catch(UnsupportedEncodingException e){
            System.out.println("dupa");
        }
        String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);

        //Create a verification context for the token
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(keyHMAC))
                .withIssuer("invoice_server")
                .withClaim("userFingerprint", userFingerprintHash)
                .build();

//Verify the token, if the verification fail then an exception is thrown
        DecodedJWT decodedToken = verifier.verify(token);
        return decodedToken;
    }

    public UserDto validateToken(String token) {

        String login = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        Optional<ServiceUser> userOptional = userRepository.findByLogin(login);

        if (userOptional.isEmpty()) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }

        ServiceUser user = userOptional.get();
        String fingerprintCookie = createUserFingerprint();
        return userMapper.toUserDto(user, createToken(user, fingerprintCookie), fingerprintCookie);
    }

    private String createUserFingerprint() {
        byte[] randomFgp = new byte[50];
        secureRandom.nextBytes(randomFgp);
        return DatatypeConverter.printHexBinary(randomFgp);
    }

    private String createToken(ServiceUser user, String userFingerprint) {
        String userLogin = user.getLogin();
//        Claims claims = Jwts.claims().setSubject(userLogin);
//
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + 3600000); // 1 hour


        //Compute a SHA256 hash of the fingerprint in order to store the
        //fingerprint hash (instead of the raw value) in the token
        //to prevent an XSS to be able to read the fingerprint and
        //set the expected cookie itself

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
        byte[] userFingerprintDigest;
        try {
            userFingerprintDigest = digest.digest(userFingerprint.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
        String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);

        //Create the token with a validity of 15 minutes and client context (fingerprint) information
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
                .withClaim("userFingerprint", userFingerprintHash)
                .withHeader(headerClaims)
                .sign(Algorithm.HMAC256(this.keyHMAC));

//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
        return token;
    }

    public void register(UserRegistrationRequest request) {
        ServiceUser user = ServiceUser.builder()
                .login(request.getLogin())
                .build();
        if (userRepository.existsServiceUserByLogin(request.getLogin())) {
            throw new AppException("User with login " + request.getLogin() + "already exists", HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
