package com.dhl.cmdb.api;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.jasypt.salt.RandomSaltGenerator;
import org.springframework.stereotype.Component;

/**
 * @author WSY
 */
@Component
public class JasyptUtil {

    //uat环境使用环境变量
    private static final String SECRET_KEY = "8HNNRI5ICOBZFnq";

    //生产环境使用环境变量
    //private static final String SECRET_KEY = "sgk7M2JrucrRrEnh";

    /**
     * Jasypt生成加密结果
     *
     * 用于生成加密串密钥  生产环境需要使用生产环境的密码来生产加密串
     *
     * @param text 加密值
     */
    public static String encrypt(String text) {
        return getEncryptor().encrypt(text);
    }

    /**
     * Jasypt解密
     *
     * @param encryptedText 待解密的密文
     */
    public static String decrypt(String encryptedText) {
        return PropertyValueEncryptionUtils.decrypt(encryptedText, getEncryptor());
    }

    public static StandardPBEStringEncryptor getEncryptor() {
        String secret = System.getenv("JASYPT_ENCRYPTOR_PASSWORD_DEV");
        System.out.println("系统环境变量:::" + secret);
        if(StringUtils.isBlank(secret)) {
            secret = SECRET_KEY;
            System.out.println("本地配置密钥:::" + secret);
        }
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setPassword(secret);
        config.setKeyObtentionIterations("10");
        config.setPoolSize("4");
        config.setProviderName("SunJCE");
        config.setSaltGenerator(new RandomSaltGenerator());
        config.setStringOutputType("base64");
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }

    //执行该函数生成密码加密串
    public static void main(String[] args) {
        // 本地配置
        String readDSPassword = "Zaixin@net0704#epl";
        String writeDSPassword = "Zaixin@net0704#epl";
        String sqlServerPassword = "zaixin123";
        //UAT配置
        //String readDSPassword = "Donuser!123456789";
        //String writeDSPassword = "Donuser!123456789";
        //String sqlServerPassword = "DHL12345!";

        // 生产配置
        //String readDSPassword = "Donuser!123456789";
        //String writeDSPassword = "Donuser!123456789";
        //String sqlServerPassword = "JellyFish11223344";

        //MYSQL-数据库writeDS加密密码::Zaixin@net0704#epl
        String writeDSEncrypt = "ENC(" + encrypt("w5CCL35f335YyN9i6noY6DLU3") + ")";
        System.out.println("MYSQL-数据库writeDS加密::" + writeDSEncrypt);
//        String writeDSDecrypt = decrypt(writeDSEncrypt);
        String writeDSDecrypt = decrypt(writeDSEncrypt);
        System.out.println("MYSQL-数据库writeDS解密::" + writeDSDecrypt);

        //MYSQL-数据库readDS加密密码::Zaixin@net0704#epl
//        String readDSEncrypt = "ENC(" + encrypt(readDSPassword) + ")";
//        System.out.println("MYSQL-数据库readDS加密::" + readDSEncrypt);
//        String readDSDecrypt = decrypt(readDSEncrypt);
//        System.out.println("MYSQL-数据库readDS解密::" + readDSDecrypt);

//        //SQL-SERVER-数据库加密密码::Zaixin@net0704#epl
//        String sqlServerEncrypt = "ENC(" + encrypt(sqlServerPassword) + ")";
//        System.out.println("SQL-SERVER-数据库加密::" + sqlServerEncrypt);
//        String sqlServerDecrypt = decrypt(sqlServerEncrypt);
//        System.out.println("SQL-SERVER-数据库解密::" + sqlServerDecrypt);
    }

}
