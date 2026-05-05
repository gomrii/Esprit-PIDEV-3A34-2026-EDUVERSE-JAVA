package com.elearning.util;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    public static String genererMotDePasseFort() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Garantir au moins un caractère de chaque type
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Remplir le reste pour atteindre 12 caractères
        String allCharacters = LOWER + UPPER + DIGITS + SPECIAL;
        for (int i = 4; i < 12; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Mélanger les caractères pour ne pas avoir un pattern prévisible
        List<Character> chars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars, random);

        StringBuilder finalPassword = new StringBuilder();
        for (char c : chars) {
            finalPassword.append(c);
        }

        return finalPassword.toString();
    }
}
