package edu.cit.sala.patupi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PatupiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatupiApplication.class, args);
    }

}