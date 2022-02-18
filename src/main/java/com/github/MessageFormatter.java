package com.github;

/**
 * @author EalenXie create on 2020/10/29 9:44
 * 例如 format("ABC--{}--EFG ", "D")    ABC--D--EFG
 */
public final class MessageFormatter {

    private static final String DELIMITER = "{}";

    private static final String ESCAPE_CHAR = "\\";

    private static final String EMPTY = "";

    private MessageFormatter() {

    }

    public static String format(String template, Object... args) {
        if (args == null || args.length <= 0) {
            return template;
        }
        if (template == null || template.isEmpty()) {
            return EMPTY;
        }
        if (template.contains(DELIMITER)) {
            StringBuilder buf = new StringBuilder(template);
            int start = 0;
            for (Object arg : args) {
                int result = escapeReplace(DELIMITER, ESCAPE_CHAR, buf, arg, start);
                start = result;
                if (result == -1) {
                    break;
                }
            }
            return buf.toString();
        }
        return template;
    }


    public static int escapeReplace(String delimiter, String escape, StringBuilder buf, Object arg, int start) {
        int esIndex = buf.indexOf(escape + delimiter, start);
        int deIndex = buf.indexOf(delimiter, start);
        if (esIndex == -1) {
            if (deIndex != -1) {
                int di = deIndex + delimiter.length();
                buf.replace(deIndex, di, arg.toString());
                return di;
            } else {
                return -1;
            }
        } else {
            if (esIndex > deIndex) {
                int di = deIndex + delimiter.length();
                buf.replace(deIndex, di, arg.toString());
                return di;
            } else {
                buf.replace(esIndex, esIndex + escape.length(), EMPTY);
                return escapeReplace(delimiter, escape, buf, arg, esIndex + (escape + delimiter).length());
            }
        }
    }


}
