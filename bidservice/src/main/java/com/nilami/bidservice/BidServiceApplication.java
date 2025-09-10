package com.nilami.bidservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BidServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidServiceApplication.class, args);
	}

}
