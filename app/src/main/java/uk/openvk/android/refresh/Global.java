package uk.openvk.android.refresh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.TypefaceCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.models.OvkLink;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.ui.core.activities.WallPostActivity;

public class Global {
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
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

    public static void setColorTheme(Context ctx, String value, Window window) {
        switch (value) {
            case "blue":
                ctx.setTheme(R.style.ApplicationTheme);
                break;
            case "red":
                ctx.setTheme(R.style.ApplicationTheme_Color2);
                break;
            case "green":
                ctx.setTheme(R.style.ApplicationTheme_Color3);
                break;
            case "violet":
                ctx.setTheme(R.style.ApplicationTheme_Color4);
                break;
            case "orange":
                ctx.setTheme(R.style.ApplicationTheme_Color5);
                break;
            case "teal":
                ctx.setTheme(R.style.ApplicationTheme_Color6);
                break;
            case "ocean":
                ctx.setTheme(R.style.ApplicationTheme_Color7);
                break;
            case "vk5x":
                ctx.setTheme(R.style.ApplicationTheme_Color8);
                break;
            case "gray":
                ctx.setTheme(R.style.ApplicationTheme_Color9);
                break;
            case "monet":
                ctx.setTheme(R.style.ApplicationTheme);
                MonetCompat.setup(ctx);
                break;
        }
        WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setNavigationBarColor(MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorSurface, Color.BLACK));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        } else {
            window.setNavigationBarColor(Global.adjustAlpha(Color.BLACK, 0.5f));
        }
    }

    private static boolean checkDarkTheme(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("dark_theme", true);
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
        switch (value) {
            case "inter":
                family = ResourcesCompat.getFont(ctx, R.font.inter);
                break;
            case "open_sans":
                family = ResourcesCompat.getFont(ctx, R.font.open_sans);
                break;
            case "raleway":
                family = ResourcesCompat.getFont(ctx, R.font.raleway);
                break;
            case "roboto":
                family = ResourcesCompat.getFont(ctx, R.font.roboto);
                break;
            case "rubik":
                family = ResourcesCompat.getFont(ctx, R.font.rubik);
                break;
            default:
                if (weight < 700)
                    family = Typeface.DEFAULT;
                else
                    family = Typeface.DEFAULT_BOLD;
                break;
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

    @SuppressWarnings("deprecation")
    public static Spanned formatLinksAsHtml(String original_text) {
        String[] lines = original_text.split("\r\n|\r|\n");
        StringBuilder text_llines = new StringBuilder();
        Pattern pattern = Pattern.compile("\\[(.+?)\\]|" +
                "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{1,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
        Matcher matcher = pattern.matcher(original_text);
        boolean regexp_search = matcher.find();
        String text = original_text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
        text = text.replace("\r\n", "<br>").replace("\n", "<br>");
        int regexp_results = 0;
        while(regexp_search) {
            String block = matcher.group();
            if(block.startsWith("[") && block.endsWith("]")) {
                OvkLink link = new OvkLink();
                String[] markup = block.replace("[", "").replace("]", "").split("\\|");
                link.screen_name = markup[0];
                if (markup.length == 2) {
                    if (markup[0].startsWith("id")) {
                        link.url = String.format("openvk://profile/%s", markup[0]);
                        link.name = markup[1];
                    } else if (markup[0].startsWith("club")) {
                        link.url = String.format("openvk://group/%s", markup[0]);
                        link.name = markup[1];
                    }
                    link.name = markup[1];
                    if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                        text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                    }
                }
            } else if(block.startsWith("https://") || block.startsWith("http://")) {
                text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
            }
            regexp_results = regexp_results + 1;
            regexp_search = matcher.find();
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void openPostComments(Account account, WallPost post, Context ctx) {
        Intent intent = new Intent(ctx, WallPostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("counters", post.counters);
        ctx.startActivity(intent);
    }
}
