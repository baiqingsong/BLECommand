package com.dawn.blelibrary;

public class BLEUtil {
    /**
     * 字节数组转为字符串
     */
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for(byte b : byteArray){
            if ((b & 0xFF) < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & b));
        }
        return hexString.toString().toUpperCase();
    }

    /**
     * 字符串转为字节数组
     */
    public static byte[] toByteArray(String hexString) {
        if (hexString == null)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toUpperCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            // 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xFF);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xFF);
            byteArray[i] = (byte) (high << 4 | low & 0xFF);
            k += 2;
        }
        return byteArray;
    }
}
