package uk.openvk.android.refresh;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.TypefaceCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;

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
        } else if(value.equals("monet")) {
            ctx.setTheme(R.style.ApplicationTheme_NoActionBar);
            MonetCompat.setup(ctx);
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

    public static boolean checkMonet(Context ctx) {
        String value = PreferenceManager.getDefaultSharedPreferences(ctx).getString("theme_color", "blue");
        if(value.equals("monet")) {
            return true;
        } else {
            return false;
        }
    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getIntFromColor(float Red, float Green, float Blue){
        int R = Math.round(255 * Red);
        int G = Math.round(255 * Green);
        int B = Math.round(255 * Blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    public static Typeface getFlexibleTypeface(Context ctx, int weight) {
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        String value = global_prefs.getString("interface_font", "system");
        Typeface family = null;
        if(value.equals("inter")) {
            family = ResourcesCompat.getFont(ctx, R.font.inter);
        } else if(value.equals("open_sans")) {
            family = ResourcesCompat.getFont(ctx, R.font.open_sans);
        } else if(value.equals("raleway")) {
            family = ResourcesCompat.getFont(ctx, R.font.raleway);
        } else if(value.equals("roboto")) {
            family = ResourcesCompat.getFont(ctx, R.font.roboto);
        } else if(value.equals("rubik")) {
            family = ResourcesCompat.getFont(ctx, R.font.rubik);
        } else {
            if(weight < 700)
                family = Typeface.DEFAULT;
            else
                family = Typeface.DEFAULT_BOLD;
        }
        boolean italic = false;
        if(weight >= 800) {
            weight = 800;
        } else if(weight >= 700) {
            weight = 700;
        } else if(weight >= 600) {
            weight = 600;
        } else if(weight >= 500) {
            weight = 500;
        } else if(weight >= 400) {
            weight = 400;
        } else if(weight >= 300) {
            weight = 300;
        } else if(weight >= 200) {
            weight = 200;
        } else {
            weight = 400;
        }
        Typeface typeface = TypefaceCompat.create(ctx, family, weight, italic);
        if(value.equals("open_sans") && weight >= 500) {
            return TypefaceCompat.create(ctx, family, 700, italic);
        }
        return TypefaceCompat.create(ctx, family, weight, italic);
    }
}
