package pers.summer502.j8zyeinkappstore.util;

public class StringUtils {
    private StringUtils() {
    }

    /**
     * 字符串的长度
     *
     * @param str 字符串
     * @return 长度
     */
    public static int byteLength(String str) {
        int b = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.codePointAt(i) <= 255) {
                b++;
            } else {
                // 字符编码大于255，说明是双字节字符
                b += 2;
            }
        }
        return b;
    }
}
