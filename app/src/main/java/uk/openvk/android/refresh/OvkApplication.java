package uk.openvk.android.refresh;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Locale;

import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.longpoll_api.LongPollService;

public class OvkApplication extends Application {
    public String version;
    public LongPollService longPollService;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public OvkAPIWrapper ovk_api;

    @Override
    public void onCreate() {
        super.onCreate();
        version = BuildConfig.VERSION_NAME;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getSharedPreferences("instance", 0);
        MonetCompat.enablePaletteCompat();
        SharedPreferences.Editor editor = instance_prefs.edit();
        if(!instance_prefs.contains("server")) {
            editor.putString("server", "");
        }
        if(!instance_prefs.contains("access_token")) {
            editor.putString("access_token", "");
        }
        if(!instance_prefs.contains("account_password_sha256")) {
            editor.putString("account_password_sha256", "");
        }
        if(!global_prefs.contains("enable_notification")) {
            editor.putBoolean("enable_notification", true);

        }
        if(!global_prefs.contains("theme_color")) {
            editor.putString("theme_color", "blue");
        }
        if(!global_prefs.contains("avatars_shape")) {
            editor.putString("avatars_shape", "circle");
        }
        if(!global_prefs.contains("interface_font")) {
            editor.putString("interface_font", "system");
        }
        if(!global_prefs.contains("hide_ovk_warn_for_beginners")) {
            editor.putBoolean("hide_ovk_warn_for_beginners", false);
        }
        editor.apply();
    }

    public SharedPreferences getGlobalPreferences() {
        return global_prefs;
    }

    public SharedPreferences getInstancePreferences() {
        return instance_prefs;
    }
    public static Locale getLocale(Context ctx) {
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String language = global_prefs.getString("ui_language", "System");
        String language_code = "en";
        if(language.equals("English")) {
            language_code = "en";
        } else if(language.equals("Русский")) {
            language_code = "ru";
//        } else if(language.equals("Украïнська")) {
//            language_code = "uk"; (later)
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                language_code = ctx.getResources().getConfiguration().getLocales().get(0).getLanguage();
            } else {
                language_code = ctx.getResources().getConfiguration().locale.getLanguage();
            }
        }


        return new Locale(language_code);
    }

}
