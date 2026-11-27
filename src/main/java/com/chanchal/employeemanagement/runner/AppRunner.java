package com.chanchal.employeemanagement.runner;

import com.chanchal.employeemanagement.car.Car;
import com.chanchal.employeemanagement.printer.Printer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {
//    private final Car car;
//
//    public AppRunner(Car car) {
//        this.car = car;
//        System.out.println("AppRunner bean created...");
//    }

    private final Printer printer;

    public AppRunner(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void run(String... args) {
        printer.print();
        System.out.println("Application started...");
    }
}
