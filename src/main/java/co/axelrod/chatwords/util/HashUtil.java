package co.axelrod.chatwords.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

@UtilityClass
public class HashUtil {
    private static final int HASH_LENGTH = 10;

    @SneakyThrows
    public static String getHash(String input) {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(input.getBytes());
        byte[] digest = messageDigest.digest();
        return DatatypeConverter.printHexBinary(digest).substring(0, HASH_LENGTH);
    }
}
