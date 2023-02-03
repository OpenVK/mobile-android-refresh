package uk.openvk.android.refresh;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

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

    public static void setAvatarShape(Context ctx, ShapeableImageView imageView) {
        try {
            SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            if (global_prefs.contains("avatars_shape")) {
                if (global_prefs.getString("avatars_shape", "circle").equals("circle")) {
                    float size = (float)((double)imageView.getLayoutParams().height / 2);
                    imageView.setShapeAppearanceModel(new ShapeAppearanceModel().toBuilder()
                            .setTopLeftCorner(CornerFamily.ROUNDED, size)
                            .setTopRightCorner(CornerFamily.ROUNDED, size)
                            .setBottomRightCornerSize(size)
                            .setBottomLeftCornerSize(size).build());
                } else if (global_prefs.getString("avatars_shape", "circle").equals("round32px")) {
                    float size = 32;
                    imageView.setShapeAppearanceModel(new ShapeAppearanceModel().toBuilder()
                            .setTopLeftCorner(CornerFamily.ROUNDED, size)
                            .setTopRightCorner(CornerFamily.ROUNDED, size)
                            .setBottomRightCornerSize(size)
                            .setBottomLeftCornerSize(size).build());
                } else if (global_prefs.getString("avatars_shape", "circle").equals("round16px")) {
                    float size = 16;
                    imageView.setShapeAppearanceModel(new ShapeAppearanceModel().toBuilder()
                            .setTopLeftCorner(CornerFamily.ROUNDED, size)
                            .setTopRightCorner(CornerFamily.ROUNDED, size)
                            .setBottomRightCornerSize(size)
                            .setBottomLeftCornerSize(size).build());
                } else {
                    imageView.setShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(0).toBuilder().build());
                }
            } else {
                imageView.setShapeAppearanceModel(new ShapeAppearanceModel().toBuilder()
                        .setTopLeftCorner(CornerFamily.ROUNDED, (float)(imageView.getLayoutParams().height / 2))
                        .setTopRightCorner(CornerFamily.ROUNDED, (float)(imageView.getLayoutParams().height / 2))
                        .setBottomRightCornerSize((float)(imageView.getLayoutParams().height / 2))
                        .setBottomLeftCornerSize((float)(imageView.getLayoutParams().height / 2)).build());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setInterfaceFont(AppCompatActivity activity) {
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String value = global_prefs.getString("interface_font", "system");
        if(value.equals("inter")) {
            activity.getTheme().applyStyle(R.style.ApplicationFont_Inter, true);
        } else if(value.equals("open_sans")) {
            activity.getTheme().applyStyle(R.style.ApplicationFont_OpenSans, true);
        } else if(value.equals("raleway")) {
            activity.getTheme().applyStyle(R.style.ApplicationFont_Raleway, true);
        } else if(value.equals("roboto")) {
            activity.getTheme().applyStyle(R.style.ApplicationFont_Roboto, true);
        } else if(value.equals("rubik")) {
            activity.getTheme().applyStyle(R.style.ApplicationFont_Rubik, true);
        }
    }
}
