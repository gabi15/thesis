//package com.thesis.apigateway;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SpringCloudGatewayRouting {
//
//    @Bean
//    public RouteLocator configureRoute(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("customerId", r->r.path("/api/v1/customers/**").uri("lb://CUSTOMER")) //static routing
//                //.route("orderId", r->r.path("/order/**").uri("lb://ORDER-SERVICE")) //dynamic routing
//                .build();
//    }
//}
