package uk.openvk.android.refresh.ui.core.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class NewPostActivity extends BaseNetworkActivity {
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
        Global.setColorTheme(this,
                global_prefs.getString("theme_color", "blue"), getWindow());
        Global.setInterfaceFont(this);
        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.activity_new_post);
        setAppBar();

        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        statusEditText = findViewById(R.id.status_edit);

        setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);

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
                            toolbar.getMenu().getItem(0)
                                    .setEnabled(
                                            !Objects.requireNonNull(statusEditText.getText())
                                                .toString().isEmpty()
                                    );
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

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.new_post_title));
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.send) {
                sendPost();
            }
            return false;
        });
    }

    private void sendPost() {
        if (!Objects.requireNonNull(statusEditText.getText()).toString().isEmpty()) {
            try {
                ovk_api.wall.post(ovk_api.wrapper, owner_id, statusEditText.getText().toString(),
                        false, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveState(int message, Bundle data) {
        try {
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
            if(message == HandlerMessages.WALL_POST) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.posted_successfully), Toast.LENGTH_LONG).show();
                finish();
            } else if(message == HandlerMessages.ACCESS_DENIED){
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.posting_access_denied),
                        Toast.LENGTH_LONG).show();
            } else if(message < 0){
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.posting_error),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void recreate() {

    }
}
