package com.thesis.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
public record CustomerController(CustomerService customerService) {

//    private final CustomerService customerService;
//    @Autowired
//    public CustomerController(CustomerService customerService){
//        this.customerService = customerService;
//    }

    @PostMapping
    public void register(@RequestBody CustomerRegistrationRequest customerRegistrationRequest){

        log.info("new customer registration {}", customerRegistrationRequest);
        customerService.register(customerRegistrationRequest);
    }
}
