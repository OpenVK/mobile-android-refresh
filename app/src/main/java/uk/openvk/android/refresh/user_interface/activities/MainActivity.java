package uk.openvk.android.refresh.user_interface.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;

import uk.openvk.android.refresh.OvkApplication;

public class MainActivity extends AppCompatActivity {

    private OvkApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        app = ((OvkApplication) getApplicationContext());
        if(app.getInstancePreferences().getString("server", "").length() == 0 ||
                app.getInstancePreferences().getString("access_token", "").length() == 0 ||
                app.getInstancePreferences().getString("account_password_sha256", "").length() == 0) {
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