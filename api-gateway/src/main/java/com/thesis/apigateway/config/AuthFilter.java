package com.thesis.apigateway.config;

import com.thesis.apigateway.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Missing authorization information");
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            log.info("bearer");
            log.info(authHeader);
            log.info("cookies");
            MultiValueMap<String, HttpCookie> AllCookies = exchange.getRequest().getCookies();
            HttpCookie secureFgpCookie = AllCookies.getFirst("__FakeSecure-Fgp");
            assert secureFgpCookie != null;
            String cookieValue = secureFgpCookie.getValue();

            log.info(String.valueOf(exchange.getRequest().getCookies()));

            MultiValueMap<String, String> cookiesToPass = new LinkedMultiValueMap<String, String>();
            cookiesToPass.add("__FakeSecure-Fgp", cookieValue);
            log.info("cookies to pass");
            log.info(String.valueOf(cookiesToPass));

            String[] parts = authHeader.split(" ");

            if (parts.length != 2 || !"Bearer".equals(parts[0])) {
                throw new RuntimeException("Incorrect authorization structure");
            }

            return webClientBuilder.build()
                    .post()
                    .uri("http://service-users/users/validateCookieToken?token=" + parts[1])
                    .cookies(cookies -> cookies.addAll(cookiesToPass))
                    .retrieve().bodyToMono(UserDto.class)
                    .map(userDto -> {
                        exchange.getRequest()
                                .mutate()
                                .header("X-auth-user-id", String.valueOf(userDto.getId()));
                        return exchange;
                    }).flatMap(chain::filter);
        };
    }

    public static class Config {
        // empty class as I don't need any particular configuration
    }
}
