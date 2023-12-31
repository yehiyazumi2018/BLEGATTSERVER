//Yehiya 27.09.19
package com.example.blegattserver;
import android.text.TextUtils;

public final class OutputStringUtil {
    public static String transferForPrint(byte... bytes) {
        if (bytes == null)
            return null;
        return transferForPrint(new String(bytes));
    }

    public static String transferForPrint(String str) {
        if (TextUtils.isEmpty(str))
            return str;
        str = str.replace('\r', ' ');
        str = str.replace('\n', ' ');
        if (str.endsWith(">")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private static String toHexStr(byte b) {
        String str = Integer.toHexString(0xFF & b);
        if (str.length() == 1)
            str = "0" + str;
        return str.toUpperCase();
    }

    public static String toHexString(byte... bytes) {
        if (bytes == null)
            return null;
        StringBuilder sb = new StringBuilder();
        if (bytes.length < 20) {
            sb.append("[");
            for (int i = 0; i < bytes.length; i++) {
                sb.append(toHexStr(bytes[i])).append(",");
            }
            sb.append("]");
        } else {
            sb.append("[");
            for (int i = 0; i < 4; i++) {
                sb.append(toHexStr(bytes[i])).append(",");
            }
            sb.append("...");
            for (int i = bytes.length - 5; i < bytes.length; i++) {
                sb.append(toHexStr(bytes[i])).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append("]");
        }
        return sb.toString();
    }
}
