package tsubaki.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String generateMD5(String input) {
        try {
            // 创建 MD5 消息摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 更新摘要
            md.update(input.getBytes());
            // 计算哈希值
            byte[] digest = md.digest();

            // 将字节数组转换为 16 进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String input = "Hello, World!";
        String md5Hash = generateMD5(input);
        System.out.println("Input: " + input);
        System.out.println("MD5 Hash: " + md5Hash);
    }
}