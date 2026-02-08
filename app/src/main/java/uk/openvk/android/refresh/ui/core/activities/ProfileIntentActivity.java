package uk.openvk.android.refresh.ui.core.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.User;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;
import uk.openvk.android.refresh.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class ProfileIntentActivity extends BaseNetworkActivity {
    private ProfileFragment profileFragment;
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    public User user;
    private boolean isDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);

        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"), getWindow());
        Global.setInterfaceFont(this);

        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.activity_intent);
        setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);

        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            args = path.substring("openvk://profile/".length());
            if (instance_prefs.getString("access_token", "").isEmpty()) {
                finish();
                return;
            }
            try {
                setAPIWrapper();
                createFragments();
                setAppBar();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String screen_name = ovk_api.account.user.screen_name;
        if(item.getItemId() == R.id.delete_friend) {
            if(user.friends_status > 1) {
                deleteFromFriends(user.id);
            }
        } else if(item.getItemId() == R.id.copy_link) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            String url = "";
            if(screen_name != null && screen_name.length() > 0) {
                url = String.format("https://%s/%s",
                        instance_prefs.getString("server", ""), user.screen_name);
            } else {
                url = String.format("https://%s/id%s",
                        instance_prefs.getString("server", ""), user.id);
            }
            android.content.ClipData clip =
                    android.content.ClipData.newPlainText("Profile URL", url);
            clipboard.setPrimaryClip(clip);
        } else if(item.getItemId() == R.id.open_in_browser) {
            String url = "";
            if(screen_name != null && !screen_name.isEmpty()) {
                url = String.format("https://%s/%s",
                        instance_prefs.getString("server", ""), user.screen_name);
            } else {
                url = String.format("https://%s/id%s",
                        instance_prefs.getString("server", ""), user.id);
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteFromFriends(long user_id) {
        if(user_id != ovk_api.account.id) {
            ovk_api.friends.delete(ovk_api.wrapper, user_id);
        }
    }

    private void setAPIWrapper() {
        ovk_api.account.getProfileInfo(ovk_api.wrapper);
    }

    private void createFragments() {
        //friendsFragment = new FriendsFragment();
        profileFragment = new ProfileFragment();
        setAPIWrapper();
        setAppBar();
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        //ft.add(R.id.fragment_screen, friendsFragment, "friends");
        ft.add(R.id.fragment_screen, profileFragment, "profile");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
    }

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.profile_title));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
                    MaterialToolbar appBar = findViewById(R.id.app_toolbar);
                    appBar.getMenu().clear();
                    appBar.inflateMenu(R.menu.profile);
                    if(appBar.getMenu().getItem(0).getItemId() == R.id.delete_friend) {
                        if (Integer.parseInt(args.substring(2)) == ovk_api.account.id) {
                            appBar.getMenu().removeItem(R.id.delete_friend);
                        }
                    }
                    try {
                        ovk_api.users.getUser(ovk_api.wrapper, Integer.parseInt(args.substring(2)));
                    } catch (Exception ex) {
                        ovk_api.users.search(ovk_api.wrapper, args);
                    }
                } else {
                    ovk_api.users.search(ovk_api.wrapper, args);
                }
                profileFragment.header.setCountersVisibility(PublicPageCounters.MEMBERS, false);
                profileFragment.header.setCountersVisibility(PublicPageCounters.FRIENDS, true);
            } else if (message == HandlerMessages.USERS_GET) {
                user = ovk_api.users.getList().get(0);
                profileFragment.setData(user, ovk_api.friends, ovk_api.account, ovk_api.wrapper);
                MaterialToolbar appBar = findViewById(R.id.app_toolbar);
                if(appBar.getMenu().getItem(0).getItemId() == R.id.delete_friend) {
                    if (user.id == ovk_api.account.id) {
                        appBar.getMenu().removeItem(R.id.delete_friend);
                    } else if (user.friends_status < 3) {
                        appBar.getMenu().removeItem(R.id.delete_friend);
                    }
                }
                user.downloadAvatar(ovk_api.dlman, "high", "profile_avatars");
                ovk_api.wall.get(ovk_api.wrapper, user.id, 50);
                ovk_api.friends.get(ovk_api.wrapper, user.id, 25, "profile_counter");
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                profileFragment.setFriendsCount(ovk_api.friends.count);
            } else if(message == HandlerMessages.USERS_SEARCH) {
                ovk_api.users.getUser(ovk_api.wrapper, ovk_api.users.getList().get(0).id);
            } else if (message == HandlerMessages.WALL_GET) {
                profileFragment.createWallAdapter(this, ovk_api.wall.getWallItems());
            } else if(message == HandlerMessages.WALL_AVATARS
                    || message == HandlerMessages.WALL_ATTACHMENTS) {
                if (profileFragment.getWallAdapter() == null) {
                    profileFragment.createWallAdapter(this, ovk_api.wall.getWallItems());
                }
                try {
                    if (message == HandlerMessages.WALL_AVATARS) {
                        //profileFragment.getWallAdapter().setAvatarLoadState(true);
                    } else {
                        //profileFragment.getWallAdapter().setPhotoLoadState(true);
                    }
                } catch (Exception ignored) {
                }
                profileFragment.refreshWallAdapter();
            } else if(message == HandlerMessages.FRIENDS_ADD) {
                profileFragment.setFriendStatus(ovk_api.account.user, user.friends_status);
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                profileFragment.setFriendStatus(ovk_api.account.user, user.friends_status);
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                profileFragment.setData(user, ovk_api.friends, ovk_api.account, ovk_api.wrapper);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setToolbarTitle(String title, String subtitle) {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }
}
