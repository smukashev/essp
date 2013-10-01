package com.bsbnb.util.translit;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Class is written to include cyrillic data into latin filenames
 * Browsers mess up cyrillic data
 * @author Aidar.Myrzahanov
 */
public class Transliterator {

    private static final HashMap<String, String> TRANSLIT_MAP = new HashMap<String, String>() {

        {
            put("а", "a");
            put("б", "b");
            put("в", "v");
            put("г", "g");
            put("д", "d");
            put("е", "e");
            put("ж", "zh");
            put("з", "z");
            put("и", "i");
            put("й", "y");
            put("к", "k");
            put("л", "l");
            put("м", "m");
            put("н", "n");
            put("о", "o");
            put("п", "p");
            put("р", "r");
            put("с", "s");
            put("т", "t");
            put("у", "u");
            put("ф", "f");
            put("х", "h");
            put("ц", "ts");
            put("ч", "ch");
            put("ш", "sh");
            put("щ", "sch");
            put("ь", "");
            put("ъ", "");
            put("ы", "y");
            put("э", "e");
            put("ю", "yu");
            put("я", "ya");
            put("ё", "yo");
        }
    };
    
    public static final HashSet<Character> ALLOWED_CHARS = new HashSet<Character>() {
        {
            add('.');
            add('_');
            add('[');
            add(']');
            add('(');
            add(')');
        }
    };

    /*
     * Method replaces all cyrillic symbols in text with latin transliteration
     * Digits and non-transliterable letters will keep their original places
     * Spaces are replaced with underscore
     * Any other character is omitted.
     */
    public static String transliterate(String text) {
        char[] chars = text.toCharArray();
        StringBuilder result = new StringBuilder(chars.length);
        for (char ch : chars) {
            if (Character.isLetterOrDigit(ch)) {
                if (TRANSLIT_MAP.containsKey(String.valueOf(Character.toLowerCase(ch)))) {
                    String value = TRANSLIT_MAP.get(String.valueOf(Character.toLowerCase(ch)));
                    if (Character.isUpperCase(ch)) {
                        if (value.length() > 1) {
                            value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
                        } else {
                            value = value.toUpperCase();
                        }
                    } 
                    result.append(value);
                } else {
                    result.append(ch);
                }
            } else if (Character.isWhitespace(ch)) {
                result.append("_");
            } else if (ALLOWED_CHARS.contains(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
