package com.gzrock.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import sun.misc.BASE64Encoder;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Date 2019/10/21 9:43
 * @Created by chp
 */
@Data
@Builder
//@AllArgsConstructor
//@NoArgsConstructor
@Accessors(chain = true)
public class TokenProccessor {

    private TokenProccessor(){};
    private static final TokenProccessor instance = new TokenProccessor();

    public static TokenProccessor getInstance() {
        return instance;
    }

    /**
     * 生成Token
     * @return
     */
    public static String makeToken() {

       // String base64 = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMzQifQ";
       // byte[] temp = Base64.getDecoder().decode(base64.getBytes());
//        new String(temp)
        String token = (System.currentTimeMillis() + new Random().nextInt(999999999)) + "";
       // try {
           /* MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] =  md.digest(token.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(md5);*/
            byte[] temp = Base64.getDecoder().decode(token.getBytes());
            return  new String(temp);
       /* } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;*/
    }

}
