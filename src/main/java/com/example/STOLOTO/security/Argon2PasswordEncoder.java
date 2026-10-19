package com.example.STOLOTO.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

public class Argon2PasswordEncoder implements PasswordEncoder {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);

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
        char[] chars = rawPassword.toString().toCharArray();
        try {
            return argon2.verify(encodedPassword, chars);
        } finally {
            argon2.wipeArray(chars);
        }
    }
}
