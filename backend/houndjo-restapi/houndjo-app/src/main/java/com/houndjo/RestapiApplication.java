package com.houndjo;

import com.houndjo.util.EnvironmentFileLoader;
import java.io.FileNotFoundException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestapiApplication {

    static void main(String[] args) throws FileNotFoundException {
        EnvironmentFileLoader.load(); // The .env file should be loaded before the spring boot app starts running.
        SpringApplication.run(RestapiApplication.class, args);
    }
}
