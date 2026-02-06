package uk.openvk.android.refresh.ui.core.activities;

import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.kieronquinn.monetcompat.app.MonetCompatActivity;

import uk.openvk.android.refresh.OvkApplication;

public class MainActivity extends MonetCompatActivity {

    private OvkApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Splash screen code
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        app = ((OvkApplication) getApplicationContext());

        SharedPreferences instancePrefs = app.getInstancePreferences();

        if(instancePrefs.getString("server", "").isEmpty() ||
                instancePrefs.getString("access_token", "").isEmpty() ||
                instancePrefs.getString("account_password_sha256", "").isEmpty()
        ) {
            Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(getApplicationContext(), AppActivity.class);
            startActivity(intent);
            finish();
        }
    }
}