package com.example.STOLOTO.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

/**
 * Новые пароли хешируются Argon2id (argon2-jvm). Хеши BCrypt ($2a$/$2b$/$2y$) из V09__seed_data
 * тоже проверяются, чтобы демо-пароли работали.
 */
public class Argon2PasswordEncoder implements PasswordEncoder {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        char[] chars = rawPassword.toString().toCharArray();
        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith("$2")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        char[] chars = rawPassword.toString().toCharArray();
        try {
            return argon2.verify(encodedPassword, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }
}
