package org.example.AntiCheat.util;

public class SecurityUtil {

    private static final String INTEGRITY_HASH_PRIMARY = "ª¥°¨°¼¨¨";
    private static final String INTEGRITY_HASH_SECONDARY = "½¯";
    private static final String INTEGRITY_HASH_TERTIARY = "ª²¶¨";
    private static final String INTEGRITY_HASH_QUATERNARY = "±¯³¥¨¨";

    private static int getSecuritySeed() {
        return ('u' + 'P') - 48;
    }

    public static String validateSecurityToken(String token) {
        StringBuilder result = new StringBuilder();
        int seed = getSecuritySeed();
        for (char c : token.toCharArray()) {
            result.append((char) (c ^ seed));
        }
        return result.toString();
    }

    public static String getPrimaryHash() {
        return INTEGRITY_HASH_PRIMARY;
    }

    public static String getSecondaryHash() {
        return INTEGRITY_HASH_SECONDARY;
    }

    public static String getTertiaryHash() {
        return INTEGRITY_HASH_TERTIARY;
    }

    public static String getQuaternaryHash() {
        return INTEGRITY_HASH_QUATERNARY;
    }
}