package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentTransaction;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Authorization;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.enumerations.UiMessages;
import uk.openvk.android.refresh.ui.core.fragments.auth.AuthFragment;
import uk.openvk.android.refresh.ui.core.fragments.auth.AuthProgressFragment;
import uk.openvk.android.refresh.ui.core.fragments.auth.AuthTwoFactorFragment;
import uk.openvk.android.refresh.ui.util.OvkAlertDialogBuilder;
import uk.openvk.android.refresh.ui.view.layouts.XConstraintLayout;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class AuthActivity extends BaseNetworkActivity {
    private FragmentTransaction ft;
    private XConstraintLayout auth_layout;
    private String instance;
    private String username;
    private String password;
    private AuthFragment authFragment;
    private Snackbar snackbar;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"), getWindow());
        Global.setInterfaceFont(this);
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.auth_screen);
        auth_layout = findViewById(R.id.auth_layout);
        auth_layout.setOnKeyboardStateListener(state -> {
            AppBarLayout appBar = findViewById(R.id.appbar);
            if (state) {
                appBar.setVisibility(View.GONE);
            } else {
                appBar.setVisibility(View.VISIBLE);
            }
        });
        authFragment = new AuthFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, authFragment);
        ft.commit();
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getSharedPreferences("instance", 0);
        showOvkWarning();
        if(getResources().getColor(R.color.navbarColor) == getResources().getColor(android.R.color.white)) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    WindowInsetsControllerCompat windowInsetsController =
                            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
                    windowInsetsController.setAppearanceLightStatusBars(true);
                } else {
                    getWindow().setStatusBarColor(Global.adjustAlpha(Color.BLACK, 0.5f));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setAppBar();
        setStatusBarColor(R.color.backgroundColor);
    }

    public void restart() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finishActivity(1);
    }

    private void showOvkWarning() {
        instance_prefs = getSharedPreferences("instance", 0);
        if ((instance_prefs.getString("server", "").isEmpty()
                || instance_prefs.getString("access_token", "").isEmpty()
                || instance_prefs.getString("account_password_sha256", "").isEmpty())
                && !global_prefs.getBoolean("hide_ovk_warn_for_beginners", false)) {
            Message msg = new Message();
            msg.what = UiMessages.SHOW_WARNING_DIALOG;
            try {
                handler.sendMessage(msg);
            } catch (Exception ignored) {
            }
        }
    }

    private void setAppBar() {
        ((Toolbar) findViewById(R.id.app_toolbar)).setOnMenuItemClickListener(
                item -> {
                    if(item.getItemId() == R.id.settings) {
                        Intent intent = new Intent(getApplicationContext(), MainSettingsActivity.class);
                        startActivity(intent);
                    }
                    return false;
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    public void signIn(String instance, String username, String password) {
        this.instance = instance;
        this.username = username;
        this.password = password;
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, new AuthProgressFragment());
        ft.commit();
        if(instance.equals("vk.com") || instance.equals("vk.ru") || instance.equals("vkontakte.ru")) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, new AuthFragment());
            instance = "openvk.su";
            ft.commit();
            authFragment.setAuthorizationData(instance, username, password);
            showOvkWarning();
        } else {
            ovk_api.wrapper.setServer(instance);
            ovk_api.wrapper.authorize(username, password);
        }
    }

    public void signIn(String twofactor_code) {
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, new AuthProgressFragment());
        ft.commit();
        if(snackbar != null) {
            snackbar.dismiss();
        }
        ovk_api.wrapper.setServer(instance);
        ovk_api.wrapper.authorize(username, password, twofactor_code);
    }

    public void receiveState(int message, Bundle data) {
        if(data.containsKey("address")) {
            String activityName = data.getString("address");
            if(activityName == null) {
                return;
            }
            boolean isCurrentActivity = activityName.equals(getLocalClassName());
            if(!isCurrentActivity) {
                return;
            }
        }
        if (message == HandlerMessages.AUTHORIZED) {
            SharedPreferences.Editor editor = instance_prefs.edit();
            Authorization auth = new Authorization(data.getString("response"));
            if(auth.getAccessToken() != null && !auth.getAccessToken().isEmpty()) {
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
            snackbar = Snackbar.make(auth_layout, R.string.invalid_email_or_password,
                    Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            Button snackActionBtn = snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout, R.string.auth_error_network,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry_btn, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn(instance, username, password);
                }
            });
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            Button snackActionBtn = (Button) snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.INSTANCE_UNAVAILABLE) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout, R.string.auth_instance_unavailable,
                    Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_text);
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            Button snackActionBtn = snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == HandlerMessages.UNKNOWN_ERROR) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(instance, username, password);
            ft.commit();
            snackbar = Snackbar.make(auth_layout,
                    R.string.auth_unknown_error, Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView snackTextView = (TextView) snackbarView
                    .findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            if(global_prefs.getBoolean("dark_theme", false)) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.navbarColor));
            } else {
                snackbar.setBackgroundTint(Color.WHITE);
            }
            snackTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
            Button snackActionBtn = snackbarView.findViewById(com.google.android.
                    material.R.id.snackbar_action);
            snackActionBtn.setLetterSpacing(0);
            snackbar.show();
        } else if(message == UiMessages.SHOW_WARNING_DIALOG) {
            View warn_view = getLayoutInflater().inflate(R.layout.layout_warn_message, null, false);
            OvkAlertDialogBuilder builder = new OvkAlertDialogBuilder(this, R.style.ApplicationTheme_AlertDialog);
            builder.setTitle(R.string.ovk_warning_title);
            builder.setView(warn_view);
            builder.setNegativeButton(android.R.string.ok, null);
            builder.show();
            AlertDialog dialog = builder.getDialog();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ((TextView) warn_view.findViewById(R.id.warn_message_text)).setText(
                        Html.fromHtml(getResources().getString(R.string.ovk_warning),
                                Html.FROM_HTML_MODE_COMPACT));
            } else {
                ((TextView) warn_view.findViewById(R.id.warn_message_text)).setText(
                        Html.fromHtml(getResources().getString(R.string.ovk_warning)));
            }
            ((TextView) warn_view.findViewById(R.id.warn_message_text)).setMovementMethod(
                    LinkMovementMethod.getInstance());
            ((MaterialCheckBox) warn_view.findViewById(R.id.do_not_show_messages))
                    .setOnCheckedChangeListener((compoundButton, b) -> {
                        SharedPreferences.Editor global_prefs_editor = global_prefs.edit();
                        global_prefs_editor.putBoolean("hide_ovk_warn_for_beginners", b);
                        global_prefs_editor.apply();
                    });
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
