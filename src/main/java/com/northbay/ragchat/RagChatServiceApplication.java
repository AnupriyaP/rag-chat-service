package com.northbay.ragchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

//@EnableEncryptableProperties
@SpringBootApplication
public class RagChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagChatServiceApplication.class, args);
    }


}
