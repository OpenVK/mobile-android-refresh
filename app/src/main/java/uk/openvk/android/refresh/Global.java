package uk.openvk.android.refresh;

import android.content.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Global {
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String generateSHA256Hash(String text) {
        MessageDigest digest=null;
        String hash = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes());
            hash = bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hash;
    }

    public static void setColorTheme(Context ctx, String value) {
        if(value.equals("blue")) {
            ctx.setTheme(R.style.ApplicationTheme_NoActionBar);
        } else if(value.equals("red")) {
            ctx.setTheme(R.style.ApplicationTheme_Color2_NoActionBar);
        } else if(value.equals("green")) {
            ctx.setTheme(R.style.ApplicationTheme_Color3_NoActionBar);
        } else if(value.equals("violet")) {
            ctx.setTheme(R.style.ApplicationTheme_Color4_NoActionBar);
        } else if(value.equals("orange")) {
            ctx.setTheme(R.style.ApplicationTheme_Color5_NoActionBar);
        } else if(value.equals("teal")) {
            ctx.setTheme(R.style.ApplicationTheme_Color6_NoActionBar);
        } else if(value.equals("vk5x")) {
            ctx.setTheme(R.style.ApplicationTheme_Color7_NoActionBar);
        } else if(value.equals("gray")) {
            ctx.setTheme(R.style.ApplicationTheme_Color8_NoActionBar);
        }
    }
}
