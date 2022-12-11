package com.thesis.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
public record CustomerController(CustomerService customerService) {

    @PostMapping( path="/register")
    public ResponseEntity<String> register(@RequestBody CustomerRegistrationRequest customerRegistrationRequest){

        log.info("new customer registration {}", customerRegistrationRequest);
        try{customerService.register(customerRegistrationRequest);}
        catch(IllegalStateException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully registered new user", HttpStatus.CREATED);

    }
    @GetMapping(path="/all")
    public ResponseEntity<List<Customer>> getAllCustomers(){
        return ResponseEntity.ok().body(customerService.getAllCustomers());
    }
}
