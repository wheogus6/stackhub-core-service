package core.utill;

import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@UtilityClass
public class EncryptionUtil {

//    public Key getAESKey() {
//        String key = "ymkymkymkymk";
//        byte[] keyBytes = new byte[16];
//        byte[] b = key.getBytes(StandardCharsets.UTF_8);
//
//        int len = b.length;
//        if (len > keyBytes.length) {
//            len = keyBytes.length;
//        }
//        System.arraycopy(b, 0, keyBytes, 0, len);
//        Key keySpec = new SecretKeySpec(keyBytes, "AES");
//
//        return keySpec;
//    }
//
//    // 암호화
//    public String encryption(String str) throws Exception {
//        Key keySpec = getAESKey();
//        String iv = "0987654321654321";
//        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
//        byte[] encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
//        return new String(Base64.getEncoder().encode(encrypted));
//    }
//
//    // 복호화
//    public String decryption(String enStr) throws Exception {
//        Key keySpec = getAESKey();
//        String iv = "0987654321654321";
//        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
//        byte[] byteStr = Base64.getDecoder().decode(URLDecoder.decode(enStr, "UTF-8").getBytes(StandardCharsets.UTF_8));
//        return new String(c.doFinal(byteStr), "UTF-8");
//    }

    public Key getAESKey() {
        String key = "ymkymkymkymk";
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes(StandardCharsets.UTF_8);

        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        Key keySpec = new SecretKeySpec(keyBytes, "AES");

        return keySpec;
    }

    // 암호화
    public String encryption(String str) throws Exception {
        Key keySpec = getAESKey();
        String iv = "0987654321654321";
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
        byte[] encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(Base64.getEncoder().encodeToString(encrypted), "UTF-8");
    }

    // 복호화
    public String decryption(String enStr) throws Exception {
        Key keySpec = getAESKey();
        String iv = "0987654321654321";
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8)));
        byte[] byteStr = Base64.getDecoder().decode(URLDecoder.decode(enStr, "UTF-8"));
        return new String(c.doFinal(byteStr), StandardCharsets.UTF_8);
    }
}
