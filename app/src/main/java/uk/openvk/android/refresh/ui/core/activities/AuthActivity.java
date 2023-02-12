package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Locale;
import java.util.Objects;

import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Authorization;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.fragments.auth.AuthFragment;
import uk.openvk.android.refresh.ui.core.fragments.auth.AuthProgressFragment;
import uk.openvk.android.refresh.ui.core.fragments.auth.AuthTwoFactorFragment;
import uk.openvk.android.refresh.ui.view.layouts.XConstraintLayout;
import uk.openvk.android.refresh.ui.core.listeners.OnKeyboardStateListener;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class AuthActivity extends MonetCompatActivity {
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
    private Snackbar snackbar;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"));
        Global.setInterfaceFont(this);
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.auth_screen);
        auth_layout = findViewById(R.id.auth_layout);
        auth_layout.setOnKeyboardStateListener(new OnKeyboardStateListener() {
            @Override
            public void onKeyboardStateChanged(boolean state) {
                AppBarLayout appBar = findViewById(R.id.appbar);
                if (state) {
                    appBar.setVisibility(View.GONE);
                } else {
                    appBar.setVisibility(View.VISIBLE);
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
        if(getResources().getColor(R.color.navbarColor) == getResources().getColor(android.R.color.white)) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    WindowInsetsControllerCompat windowInsetsController =
                            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                    windowInsetsController.setAppearanceLightStatusBars(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setAppBar();
    }

    private void setAppBar() {
        ((Toolbar) findViewById(R.id.toolbar)).setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.settings) {
                    Intent intent = new Intent(getApplicationContext(), MainSettingsActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                receiveState(msg.what, msg.getData());
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
        if(snackbar != null) {
            snackbar.dismiss();
        }
        if(ovk_api == null) {
            ovk_api = new OvkAPIWrapper(this);
            ovk_api.setServer(instance);
        }
        ovk_api.authorize(username, password, twofactor_code);
    }

    private void receiveState(int message, Bundle data) {
        if (message == HandlerMessages.AUTHORIZED) {
            SharedPreferences.Editor editor = instance_prefs.edit();
            Authorization auth = new Authorization(data.getString("response"));
            if(auth.getAccessToken() != null && auth.getAccessToken().length() > 0) {
                editor.putString("server", instance);
                editor.putString("access_token", auth.getAccessToken());
                editor.putString("account_password_sha256", Global.generateSHA256Hash(password));
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), AppActivity.class);
                startActivity(intent);
                finish();
            }
        } else if (message == HandlerMessages.TWOFACTOR_CODE_REQUIRED) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, new AuthTwoFactorFragment());
            ft.commit();
        } else if (message == HandlerMessages.INVALID_USERNAME_OR_PASSWORD) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout, R.string.invalid_email_or_password, Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            if(Global.checkMonet(this)) {
                if(global_prefs.getBoolean("dark_theme", false)) {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
                } else {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
                }
            } else {
                snackbar.setActionTextColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorAccent,
                        getResources().getColor(R.color.accentColor)));
            }
            Button snackActionBtn = (Button) snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout, R.string.auth_error_network, Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry_btn, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn(instance, username, password);
                }
            });
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            if(Global.checkMonet(this)) {
                if(global_prefs.getBoolean("dark_theme", false)) {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
                } else {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
                }
            } else {
                snackbar.setActionTextColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorAccent,
                        getResources().getColor(R.color.accentColor)));
            }
            Button snackActionBtn = (Button) snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.INSTANCE_UNAVAILABLE) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout, R.string.auth_instance_unavailable, Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            if(Global.checkMonet(this)) {
                if(global_prefs.getBoolean("dark_theme", false)) {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
                } else {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
                }
            } else {
                snackbar.setActionTextColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorAccent,
                        getResources().getColor(R.color.accentColor)));
            }
            Button snackActionBtn = (Button) snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.UNKNOWN_ERROR) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout, R.string.auth_unknown_error, Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            if(Global.checkMonet(this)) {
                if(global_prefs.getBoolean("dark_theme", false)) {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
                } else {
                    snackbar.setActionTextColor(Objects.requireNonNull(
                            getMonet().getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
                }
            } else {
                snackbar.setActionTextColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorAccent,
                        getResources().getColor(R.color.accentColor)));
            }
            Button snackActionBtn = (Button) snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
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

    @Override
    public void recreate() {

    }

    @Override
    public void onMonetColorsChanged(@NonNull MonetCompat monet, @NonNull ColorScheme monetColors, boolean isInitialChange) {
        super.onMonetColorsChanged(monet, monetColors, isInitialChange);
        getMonet().updateMonetColors();
    }
}
