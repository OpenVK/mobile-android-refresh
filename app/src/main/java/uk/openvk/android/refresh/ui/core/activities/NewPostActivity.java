package uk.openvk.android.refresh.ui.core.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;
import com.kieronquinn.monetcompat.extensions.views.ViewExtensions_EditTextKt;

import java.util.Locale;
import java.util.Objects;

import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class NewPostActivity extends MonetCompatActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private Wall wall;
    public Handler handler;
    private long owner_id;
    private long account_id;
    private String account_first_name;
    private TextInputEditText statusEditText;
    private MaterialToolbar toolbar;
    private boolean isDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"), getWindow());
        Global.setInterfaceFont(this);
        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.new_post);
        setAPIWrapper();
        setAppBar();

        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        statusEditText = findViewById(R.id.status_edit);
        setMonetTheme();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
                return;
            } else {
                owner_id = extras.getLong("owner_id");
                account_id = extras.getLong("account_id");
                account_first_name = extras.getString("account_first_name");
                setAppBar();
                setAPIWrapper();
                if (owner_id == 0) {
                    finish();
                }
                statusEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            if (statusEditText.getText().toString().length() > 0) {
                                toolbar.getMenu().getItem(0).setEnabled(true);
                            } else {
                                toolbar.getMenu().getItem(0).setEnabled(false);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setMonetTheme() {
        if(Global.checkMonet(this)) {
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_selected}, new int[] { }
            };
            int[] colors;
            int colorOnSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
            if (!isDarkTheme) {
                toolbar.setBackgroundColor(Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(600)).toLinearSrgb().toSrgb().quantize8());
                getWindow().setStatusBarColor(Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(700)).toLinearSrgb().toSrgb().quantize8());

                colors = new int[]{
                        Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(600)).toLinearSrgb().toSrgb().quantize8(),
                        Global.adjustAlpha(colorOnSurface, 0.6f)
                };
                Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)).getEditText())
                        .setHighlightColor(
                                Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(200)).toLinearSrgb().toSrgb().quantize8());
            } else {
                colors = new int[]{
                        Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(200)).toLinearSrgb().toSrgb().quantize8(),
                        Global.adjustAlpha(colorOnSurface, 0.6f)
                };
                Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)).getEditText())
                        .setHighlightColor(
                                Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
            }
            Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)))
                    .setHintTextColor(ColorStateList.valueOf(colors[0]));
            Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)))
                    .setBoxStrokeColor(colors[0]);
        } else {
            int rippleColor = MaterialColors.getColor(this, com.google.android.material.R.attr.rippleColor, Color.GRAY);
            int accentColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorAccent, Color.BLACK);
            Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)).getEditText())
                    .setHighlightColor(rippleColor);
            Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)))
                    .setHintTextColor(ColorStateList.valueOf(accentColor));
            Objects.requireNonNull(((TextInputLayout) findViewById(R.id.status_edit_layout)))
                    .setBoxStrokeColor(accentColor);
        }
    }

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.new_post_title));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.send) {
                    sendPost();
                }
                return false;
            }
        });
        if(!Global.checkMonet(this)) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = new TypedValue();
            boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkThemeEnabled) {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.background, typedValue, true);
            } else {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
            }
            window.setStatusBarColor(typedValue.data);
        }
    }

    private void sendPost() {
        if (statusEditText.getText().toString().length() > 0) {
            try {
                wall.post(ovk_api, owner_id, statusEditText.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        wall = new Wall();
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("OpenVK", String.format("Handling API message: %s", msg.what));
                receiveState(msg.what, msg.getData());
            }
        };
    }

    private void receiveState(int message, Bundle data) {
        try {
            if(message == HandlerMessages.WALL_POST) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG).show();
                finish();
            } else if(message == HandlerMessages.ACCESS_DENIED){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_access_denied), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_error), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void recreate() {

    }

    @Override
    public void onMonetColorsChanged(@NonNull MonetCompat monet, @NonNull ColorScheme monetColors, boolean isInitialChange) {
        super.onMonetColorsChanged(monet, monetColors, isInitialChange);
        getMonet().updateMonetColors();
        setMonetTheme();
    }
}
