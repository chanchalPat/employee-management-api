package com.chanchal.employeemanagement.engine;

import org.springframework.stereotype.Component;

@Component
public class Engine {
    public Engine() {
        System.out.println("Engine bean created...");
    }

//    public void start() {
//        System.out.println("Engine started...");
//    }

//    public interface Engine {
//
//        void start();
//    }
}
