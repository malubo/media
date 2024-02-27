package cz.malubo.media;

public class Utils {
    public static String removeLeadingZeroes(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (!sb.isEmpty() && sb.charAt(0) == '0') {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }
}
