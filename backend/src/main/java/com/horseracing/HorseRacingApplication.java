package com.horseracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi động hệ thống quản lý giải đua ngựa (Spring Boot + JWT).
 */
@SpringBootApplication
public class HorseRacingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HorseRacingApplication.class, args);
    }
}
