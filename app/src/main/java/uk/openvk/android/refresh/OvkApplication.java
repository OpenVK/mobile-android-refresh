package uk.openvk.android.refresh;

import android.app.Application;
import android.app.LocaleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Locale;

import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.longpoll_api.LongPollService;

public class OvkApplication extends Application {
    public static boolean isTablet;
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
        createSettings();
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        boolean xlarge = ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        int swdp = (int)(getResources().getConfiguration().smallestScreenWidthDp);
        if((xlarge || large) && swdp >= 600) {
            isTablet = true;
        }
    }

    private void createSettings() {
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
        if(!global_prefs.contains("video_player")) {
            editor.putString("video_player", "built_in");
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
        switch (language) {
            case "English":
                language_code = "en";
                break;
            case "Русский":
                language_code = "ru";
                break;
            case "Украïнська":
                language_code = "uk";
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    language_code = ctx.getResources().getConfiguration().getLocales().get(0).getLanguage();
                } else {
                    language_code = ctx.getResources().getConfiguration().locale.getLanguage();
                }
                break;
        }


        return new Locale(language_code);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static LocaleListCompat getLocaleList(Context ctx) {
        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        LocaleList currentAppLocales =
                ctx.getSystemService(LocaleManager.class).getApplicationLocales();
        SharedPreferences.Editor editor = global_prefs.edit();
        if(currentAppLocales.toLanguageTags().startsWith("en")) {
            editor.putString("ui_language", "English");
        } else if(currentAppLocales.toLanguageTags().startsWith("ru")) {
            editor.putString("ui_language", "Russian");
        } else if(currentAppLocales.toLanguageTags().startsWith("uk")) {
            editor.putString("ui_language", "Украïнська");
        } else if(currentAppLocales.toLanguageTags().startsWith("und")) {
            editor.putString("ui_language", "System");
        }

        editor.apply();
        if(!currentAppLocales.toLanguageTags().startsWith("und")) {
            return LocaleListCompat.wrap(
                    new LocaleList(Locale.forLanguageTag(currentAppLocales.toLanguageTags())));
        } else {
            return LocaleListCompat.wrap(
                    new LocaleList(ctx.getResources().getConfiguration().locale));
        }
    }

}
