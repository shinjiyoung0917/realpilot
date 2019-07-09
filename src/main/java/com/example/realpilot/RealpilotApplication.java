package com.example.realpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@Configuration + @EnableAutoConfiguration + @ComponentScan을 디폴트 속성으로 함께 사용하는 것과 같음
public class RealpilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealpilotApplication.class, args);
    }

}
