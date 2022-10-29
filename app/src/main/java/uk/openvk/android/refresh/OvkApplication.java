package uk.openvk.android.refresh;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class OvkApplication extends Application {
    public String version;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        version = BuildConfig.VERSION_NAME;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getSharedPreferences("instance", 0);
        if(!instance_prefs.contains("server")) {
            SharedPreferences.Editor editor = instance_prefs.edit();
            editor.putString("server", "");
            editor.apply();
        }
        if(!instance_prefs.contains("access_token")) {
            SharedPreferences.Editor editor = instance_prefs.edit();
            editor.putString("access_token", "");
            editor.apply();
        }
        if(!instance_prefs.contains("account_password_sha256")) {
            SharedPreferences.Editor editor = instance_prefs.edit();
            editor.putString("account_password_sha256", "");
            editor.apply();
        }
        if(!global_prefs.contains("enable_notification")) {
            SharedPreferences.Editor editor = global_prefs.edit();
            editor.putBoolean("enable_notification", true);
            editor.apply();
        }
        if(!global_prefs.contains("use_ssl_connection")) {
            SharedPreferences.Editor editor = global_prefs.edit();
            editor.putBoolean("use_ssl_connection", true);
            editor.apply();
        }
    }

    public SharedPreferences getGlobalPreferences() {
        return global_prefs;
    }

    public SharedPreferences getInstancePreferences() {
        return instance_prefs;
    }
}
