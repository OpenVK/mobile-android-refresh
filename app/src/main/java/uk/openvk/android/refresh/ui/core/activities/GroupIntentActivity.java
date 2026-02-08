package uk.openvk.android.refresh.ui.core.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Group;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;
import uk.openvk.android.refresh.ui.core.fragments.app.CommunityFragment;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class GroupIntentActivity extends BaseNetworkActivity {
    private CommunityFragment communityFragment;
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    public Group group;
    private boolean isDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"),
                getWindow());
        Global.setInterfaceFont(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        instance_prefs = getSharedPreferences("instance", 0);

        setContentView(R.layout.activity_intent);
        setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);

        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            args = path.substring("openvk://group/".length());
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
        String screenname = ovk_api.account.user.screen_name;
        if(item.getItemId() == R.id.leave_group) {
            if(group.is_member > 0) {
                group.leave(ovk_api.wrapper);
            } else {
                group.join(ovk_api.wrapper);
            }
        } else if(item.getItemId() == R.id.copy_link) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            String url = "";
            if(screenname != null && !screenname.isEmpty()) {
                url = String.format("https://%s/%s",
                        instance_prefs.getString("server", ""), group.screen_name);
            } else {
                url = String.format("https://%s/club%s",
                        instance_prefs.getString("server", ""), group.id);
            }
            android.content.ClipData clip =
                    android.content.ClipData.newPlainText("Profile URL", url);
            clipboard.setPrimaryClip(clip);
        } else if(item.getItemId() == R.id.open_in_browser) {
            String url = "";
            if(screenname != null && !screenname.isEmpty()) {
                url = String.format("https://%s/%s",
                        instance_prefs.getString("server", ""), group.screen_name);
            } else {
                url = String.format("https://%s/club%s",
                        instance_prefs.getString("server", ""), group.id);
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAPIWrapper() {
        ovk_api.account.getProfileInfo(ovk_api.wrapper);
    }

    private void createFragments() {
        //friendsFragment = new FriendsFragment();
        communityFragment = new CommunityFragment();
        setAPIWrapper();
        setAppBar();
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        //ft.add(R.id.fragment_screen, friendsFragment, "friends");
        ft.add(R.id.fragment_screen, communityFragment, "group");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
    }

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.community_title));
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
                if (args.startsWith("club")) {
                    try {
                        ovk_api.groups.getGroupByID(
                                ovk_api.wrapper,
                                Integer.parseInt(args.substring(4))
                        );
                    } catch (Exception ex) {
                        ovk_api.groups.search(ovk_api.wrapper, args);
                    }
                } else {
                    ovk_api.groups.search(ovk_api.wrapper, args);
                }
                communityFragment.header.hideSendMessageButton();
                communityFragment.header.setCountersVisibility(PublicPageCounters.FRIENDS, false);
                communityFragment.header.setCountersVisibility(PublicPageCounters.MEMBERS, true);
                communityFragment.addGroupCollapseListener();
            } else if (message == HandlerMessages.GROUPS_GET) {
                group = ovk_api.groups.getList().get(0);
                MaterialToolbar appBar = findViewById(R.id.app_toolbar);
                appBar.getMenu().clear();
                appBar.inflateMenu(R.menu.group);
                if(group.is_member < 1) {
                    if(appBar.getMenu().getItem(0).getItemId() == R.id.leave_group) {
                        appBar.getMenu().removeItem(R.id.leave_group);
                    }
                }
                communityFragment.setData(group, ovk_api.wrapper);
                group.downloadAvatar(ovk_api.dlman, "high");
                ovk_api.wall.get(ovk_api.wrapper, -group.id, 50);
            } else if (message == HandlerMessages.GROUPS_GET_BY_ID) {
                group = ovk_api.groups.getList().get(0);
                MaterialToolbar appBar = findViewById(R.id.app_toolbar);
                appBar.getMenu().clear();
                appBar.inflateMenu(R.menu.group);
                if(group.is_member < 1) {
                    if(appBar.getMenu().getItem(0).getItemId() == R.id.leave_group) {
                        appBar.getMenu().removeItem(R.id.leave_group);
                    }
                }
                communityFragment.setData(group, ovk_api.wrapper);
                group.downloadAvatar(ovk_api.dlman, "high");
                ovk_api.wall.get(ovk_api.wrapper, -group.id, 50);
            } else if(message == HandlerMessages.GROUPS_SEARCH) {
                ovk_api.groups.getGroups(ovk_api.wrapper, ovk_api.groups.getList().get(0).id, 1);
            } else if (message == HandlerMessages.WALL_GET) {
                communityFragment.createWallAdapter(this, ovk_api.wall.getWallItems());
            } else if(message == HandlerMessages.WALL_AVATARS
                    || message == HandlerMessages.WALL_ATTACHMENTS) {
                if(message == HandlerMessages.WALL_AVATARS) {
                    //communityFragment.wallAdapter.setAvatarLoadState(true);
                } else {
                    //communityFragment.wallAdapter.setPhotoLoadState(true);
                }
            } else if(message == HandlerMessages.GROUP_AVATARS) {
                communityFragment.setData(group, ovk_api.wrapper);
            } else if (message == HandlerMessages.GROUPS_JOIN) {
                communityFragment.setJoinStatus(group, 1);
            } else if (message == HandlerMessages.GROUPS_LEAVE) {
                communityFragment.setJoinStatus(group, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void recreate() {

    }

    public void setToolbarTitle(String title, String subtitle) {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }
}
