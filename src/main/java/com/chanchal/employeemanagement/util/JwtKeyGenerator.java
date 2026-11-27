package com.chanchal.employeemanagement.util;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Create a cryptographically secure 32-byte (256-bit) array
        byte[] randomBytes = new byte[32];

        // Populate the array with true random numbers
        new SecureRandom().nextBytes(randomBytes);

        // Encode to a standard Base64 string for text-based environment properties
        String base64Key = Base64.getEncoder().encodeToString(randomBytes);

        System.out.println("\n=== YOUR SECURE JWT SECRET KEY ===");
        System.out.println(base64Key);
        System.out.println("==================================\n");
    }
}

