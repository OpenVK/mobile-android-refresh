package uk.openvk.android.refresh.user_interface.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Authorization;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.user_interface.fragments.auth.AuthFragment;
import uk.openvk.android.refresh.user_interface.fragments.auth.AuthProgressFragment;
import uk.openvk.android.refresh.user_interface.fragments.auth.AuthTwoFactorFragment;
import uk.openvk.android.refresh.user_interface.layouts.XConstraintLayout;
import uk.openvk.android.refresh.user_interface.listeners.OnKeyboardStateListener;

public class AuthActivity extends AppCompatActivity {
    public Handler handler;
    public OvkAPIWrapper ovk_api;
    private FragmentTransaction ft;
    private XConstraintLayout auth_layout;
    private String instance;
    private String username;
    private String password;
    private AuthFragment authFragment;
    private SharedPreferences instance_prefs;
    private SharedPreferences global_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.ApplicationTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_screen);
        auth_layout = findViewById(R.id.auth_layout);
        auth_layout.setOnKeyboardStateListener(new OnKeyboardStateListener() {
            @Override
            public void onKeyboardStateChanged(boolean state) {
                ImageView auth_logo = (ImageView) findViewById(R.id.ovk_logo);
                if (state) {
                    auth_logo.setVisibility(View.GONE);
                } else {
                    auth_logo.setVisibility(View.VISIBLE);
                }
            }
        });
        authFragment = new AuthFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, authFragment);
        ft.commit();
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getSharedPreferences("instance", 0);
        setAPIWrapper();
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                recieveState(msg.what, msg.getData());
            }
        };
    }

    public void signIn(String instance, String username, String password) {
        this.instance = instance;
        this.username = username;
        this.password = password;
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, new AuthProgressFragment());
        ft.commit();
        if(ovk_api == null) {
            ovk_api = new OvkAPIWrapper(this);
        }
        ovk_api.setServer(instance);
        ovk_api.authorize(username, password);
    }

    public void signIn(String twofactor_code) {
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, new AuthProgressFragment());
        ft.commit();
        if(ovk_api == null) {
            ovk_api = new OvkAPIWrapper(this);
            ovk_api.setServer(instance);
        }
        ovk_api.authorize(username, password, twofactor_code);
    }

    private void recieveState(int message, Bundle data) {
        if (message == HandlerMessages.AUTHORIZED) {
            SharedPreferences.Editor editor = instance_prefs.edit();
            Authorization auth = new Authorization(data.getString("response"));
            editor.putString("server", instance);
            editor.putString("access_token", auth.getAccessToken());
            editor.putString("account_password_sha256", Global.generateSHA256Hash(password));
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), AppActivity.class);
            startActivity(intent);
            finish();
        } else if (message == HandlerMessages.TWOFACTOR_CODE_REQUIRED) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, new AuthTwoFactorFragment());
            ft.commit();
        } else if (message == HandlerMessages.INVALID_USERNAME_OR_PASSWORD) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            Snackbar.make(auth_layout, R.string.invalid_email_or_password, Snackbar.LENGTH_LONG).show();
        } else if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            Snackbar snackbar = Snackbar.make(auth_layout, R.string.auth_error_network, Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry_btn, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn(instance, username, password);
                }
            });
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setActionTextColor(getResources().getColor(R.color.primaryColor));
            Button snackActionBtn = (Button) snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.UNKNOWN_ERROR) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            Snackbar.make(auth_layout, R.string.auth_unknown_error, Snackbar.LENGTH_LONG).show();
        }
    }

    public void changeFragment(String name) {
        if(name.equals("auth_form")) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
        }
    }
}
