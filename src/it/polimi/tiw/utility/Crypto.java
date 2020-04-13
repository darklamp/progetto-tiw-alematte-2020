package it.polimi.tiw.utility;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import org.apache.commons.lang3.RandomStringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Random;


public class Crypto {
    private static Argon2Advanced create(){
        return  Argon2Factory.createAdvanced(Argon2Factory.Argon2Types.ARGON2id);
    }
    public static String createSalt(){
        return RandomStringUtils.randomAlphanumeric(100);
    }
    public static String pwHash(String password, byte[] salt) throws SQLException {
        return create().hash(10,65536,2,password.toCharArray(), StandardCharsets.UTF_8,salt);
    }
}
