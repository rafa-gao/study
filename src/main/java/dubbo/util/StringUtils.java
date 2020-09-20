package dubbo.util;

/**
 * @author rafa gao
 */


public class StringUtils {

    public static String camelToSplitName(String camelName, String spilt) {
        if (isEmpty(camelName)) {
            return camelName;
        }
        StringBuilder stringBuilder = new StringBuilder(camelName.length());
        for (int i = 0; i < camelName.length(); i++) {
            char c = camelName.charAt(i);
            // 小写字母
            if (c >= 'A' && c <= 'Z') {
                if (i != 0) {
                    stringBuilder.append(spilt);
                }
                stringBuilder.append(Character.toLowerCase(c));
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
