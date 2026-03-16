package com.jingdianjichi.auth.infra.basic.utils;

import com.alibaba.druid.filter.config.ConfigTools;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 数据库加密util
 * <p>
 * 该工具类使用Druid提供的ConfigTools实现数据库密码的加密和解密功能。
 * 通过RSA非对称加密算法，生成公钥和私钥对，用于敏感信息的加密存储。
 * 主要用于数据库连接密码等敏感配置信息的加密处理，提高系统安全性。
 * </p>
 *
 * @author: ChickenWing
 * @date: 2023/10/1
 */
public class DruidEncryptUtil {

    // 定义静态字符串变量，用于存储RSA公钥
    private static String publicKey;

    // 定义静态字符串变量，用于存储RSA私钥
    private static String privateKey;

    // 静态代码块，在类加载时自动执行，用于生成RSA密钥对
    static {
        try {
            // 使用Druid的ConfigTools工具类生成512位长度的RSA密钥对
            String[] keyPair = ConfigTools.genKeyPair(512);
            // 获取生成的私钥并赋值给privateKey变量
            privateKey = keyPair[0];
            // 在控制台输出私钥，方便开发时记录和使用
            System.out.println("privateKey:" + privateKey);
            // 获取生成的公钥并赋值给publicKey变量
            publicKey = keyPair[1];
            // 在控制台输出公钥，方便开发时记录和使用
            System.out.println("publicKey:" + publicKey);
        } catch (NoSuchAlgorithmException e) {
            // 捕获无此算法异常，当系统不支持指定的加密算法时抛出
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // 捕获无此提供者异常，当系统不支持指定的加密提供者时抛出
            e.printStackTrace();
        }
    }

    /**
     * 加密方法
     * <p>
     * 使用私钥对明文进行RSA加密，返回加密后的字符串。
     * 注意：这里使用私钥加密，公钥解密，是一种特殊的用法。
     * </p>
     * 
     * @param plainText 需要加密的明文字符串，如数据库密码
     * @return 加密后的字符串
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static String encrypt(String plainText) throws Exception {
        // 使用ConfigTools工具类和私钥对明文进行加密
        String encrypt = ConfigTools.encrypt(privateKey, plainText);
        // 在控制台输出加密结果，方便开发时验证
        System.out.println("encrypt:" + encrypt);
        // 返回加密后的字符串
        return encrypt;
    }

    /**
     * 解密方法
     * <p>
     * 使用公钥对加密文本进行RSA解密，返回原始明文字符串。
     * 与encrypt方法相对应，完成加密数据的还原。
     * </p>
     * 
     * @param encryptText 需要解密的加密字符串
     * @return 解密后的原始明文字符串
     * @throws Exception 解密过程中可能抛出的异常
     */
    public static String decrypt(String encryptText) throws Exception {
        // 使用ConfigTools工具类和公钥对加密文本进行解密
        String decrypt = ConfigTools.decrypt(publicKey, encryptText);
        // 在控制台输出解密结果，方便开发时验证
        System.out.println("decrypt:" + decrypt);
        // 返回解密后的原始明文
        return decrypt;
    }

    /**
     * 主方法，用于测试加密和解密功能
     * <p>
     * 可以直接运行此方法进行加密功能的测试，
     * 示例中加密了字符串"2003"并输出结果。
     * </p>
     * 
     * @param args 命令行参数，本方法中未使用
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        // 调用encrypt方法加密字符串"2003"
        String encrypt = encrypt("2003");
        // 在控制台输出加密结果
        System.out.println("encrypt:" + encrypt);
    }
}
