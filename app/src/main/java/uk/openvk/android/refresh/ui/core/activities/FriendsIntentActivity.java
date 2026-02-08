package uk.openvk.android.refresh.ui.core.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Friend;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class FriendsIntentActivity extends BaseNetworkActivity {
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    private boolean isDarkTheme;
    private FriendsFragment friendsFragment;
    private long user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(
                this, global_prefs.getString("theme_color", "blue"), getWindow()
        );
        Global.setInterfaceFont(this);

        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        instance_prefs = getSharedPreferences("instance", 0);

        setContentView(R.layout.activity_intent);
        setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);

        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            args = path.substring("openvk://friends/".length());
            if (instance_prefs.getString("access_token", "").isEmpty()) {
                finish();
                return;
            }
            try {
                setAPIWrapper();
                setAppBar();
                createFragment();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setAPIWrapper() {
        ovk_api.account.getProfileInfo(ovk_api.wrapper);
    }

    private void createFragment() {
        setAppBar();
    }

    private void applyFragment() {
        friendsFragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putLong("user_id", user_id);
        args.putLong("account_id", ovk_api.account.id);
        friendsFragment.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_screen, friendsFragment, "friends");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
    }

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.nav_friends));
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        if(!Global.checkMonet(this)) {
            TypedValue typedValue = new TypedValue();
            boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkThemeEnabled) {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.background,
                        typedValue, true);
            } else {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark,
                        typedValue, true);
            }
            getWindow().setStatusBarColor(typedValue.data);
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
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if (args.startsWith("id")) {
                    try {
                        user_id = Integer.parseInt(args.substring(2));
                        ovk_api.friends.get(ovk_api.wrapper, Integer.parseInt(args.substring(2)),
                                25, "friends_list");
                    } catch (Exception ex) {
                        ovk_api.users.search(ovk_api.wrapper, args);
                    }
                } else {
                    ovk_api.users.search(ovk_api.wrapper, args);
                }
                applyFragment();
            } else if (message == HandlerMessages.FRIENDS_GET) {
                ovk_api.friends.parse(data.getString("response"), ovk_api.dlman,
                        true, true);
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                friendsFragment.disableUpdateState();
                //friendsFragment.setScrollingPositions(this, friends.getFriends().size() > 0);
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                int old_friends_size = ovk_api.friends.getFriends().size();
                ovk_api.friends.parse(data.getString("response"), ovk_api.dlman,
                        true, false);
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                //friendsFragment.setScrollingPositions(this, old_friends_size != friends.getFriends().size());
            } //friendsFragment.refreshAdapter();
        } catch (Exception ex) {
            ex.printStackTrace();
            finish();
        }
    }

    public void refreshFriendsList(boolean progress) {
        ovk_api.friends.get(ovk_api.wrapper, ovk_api.account.id, 25, "friends_list");
        if (progress) {
            friendsFragment.showProgress();
        }
    }

    @Override
    public void recreate() {

    }

    public void loadMoreFriends() {
        if(ovk_api.friends != null) {
            ovk_api.friends.get(ovk_api.wrapper, user_id, 25, ovk_api.friends.offset);
        }
    }
}
