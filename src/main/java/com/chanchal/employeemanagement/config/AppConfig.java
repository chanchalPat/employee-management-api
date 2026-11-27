package com.chanchal.employeemanagement.config;

import com.chanchal.employeemanagement.printer.Printer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Bean
    public Printer printer() {
        return new Printer();
    }
}
