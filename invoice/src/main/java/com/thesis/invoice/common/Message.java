package com.thesis.invoice.common;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class Message {
    private String message;
    private HttpStatus status;
}
