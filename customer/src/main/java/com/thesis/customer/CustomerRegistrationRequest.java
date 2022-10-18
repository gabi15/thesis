package com.thesis.customer;

public record CustomerRegistrationRequest (
        String firstName,
        String lastName,
        String email)
{}
