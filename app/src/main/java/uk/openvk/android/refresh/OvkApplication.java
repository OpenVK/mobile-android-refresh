package uk.openvk.android.refresh;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;

public class OvkApplication extends Application {
    public String version;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public OvkAPIWrapper ovk_api;

    @Override
    public void onCreate() {
        super.onCreate();
        version = BuildConfig.VERSION_NAME;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getSharedPreferences("instance", 0);
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
        editor.apply();
    }

    public SharedPreferences getGlobalPreferences() {
        return global_prefs;
    }

    public SharedPreferences getInstancePreferences() {
        return instance_prefs;
    }
}
