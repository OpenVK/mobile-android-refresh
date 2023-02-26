package uk.openvk.android.refresh.ui.core.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Locale;
import java.util.Objects;

import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Groups;
import uk.openvk.android.refresh.api.Likes;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;
import uk.openvk.android.refresh.ui.core.fragments.app.CommunityFragment;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class GroupIntentActivity extends MonetCompatActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public Handler handler;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private Wall wall;
    public Account account;
    private Likes likes;
    private CommunityFragment communityFragment;
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    private Groups groups;
    private Group group;
    private boolean isDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"));
        Global.setInterfaceFont(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.intent_view);
        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            args = path.substring("openvk://group/".length());
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                setAPIWrapper();
                createFragments();
                setAppBar();
                setMonetTheme();
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

    private void setMonetTheme() {
        if(Global.checkMonet(this)) {
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            if (!isDarkTheme) {
                toolbar.setBackgroundColor(Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(600)).toLinearSrgb().toSrgb().quantize8());
                getWindow().setStatusBarColor(Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(700)).toLinearSrgb().toSrgb().quantize8());
            }
        }
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        groups = new Groups();
        wall = new Wall();
        account = new Account(this);
        likes = new Likes();
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("OpenVK", String.format("Handling API message: %s", msg.what));
                receiveState(msg.what, msg.getData());
            }
        };
        account.getProfileInfo(ovk_api);
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if(!Global.checkMonet(this)) {
            TypedValue typedValue = new TypedValue();
            boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkThemeEnabled) {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.background, typedValue, true);
            } else {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
            }
            getWindow().setStatusBarColor(typedValue.data);
        }
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if (args.startsWith("club")) {
                    try {
                        groups.getGroupByID(ovk_api, Integer.parseInt(args.substring(4)));
                    } catch (Exception ex) {
                        groups.search(ovk_api, args);
                    }
                } else {
                    groups.search(ovk_api, args);
                }
                communityFragment.header.hideSendMessageButton();
                communityFragment.header.setCountersVisibility(PublicPageCounters.FRIENDS, false);
                communityFragment.header.setCountersVisibility(PublicPageCounters.MEMBERS, true);
            } else if (message == HandlerMessages.GROUPS_GET) {
                groups.parseSearch(data.getString("response"));
                group = groups.getList().get(0);
                communityFragment.setData(group);
                group.downloadAvatar(downloadManager, "high");
                wall.get(ovk_api, -group.id, 50);
            } else if (message == HandlerMessages.GROUPS_GET_BY_ID) {
                groups.parse(data.getString("response"));
                group = groups.getList().get(0);
                communityFragment.setData(group);
                group.downloadAvatar(downloadManager, "high");
                wall.get(ovk_api, -group.id, 50);
            } else if(message == HandlerMessages.GROUPS_SEARCH) {
                groups.parseSearch(data.getString("response"));
                groups.getGroups(ovk_api, groups.getList().get(0).id, 1);
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, "high", data.getString("response"));
                communityFragment.createWallAdapter(this, wall.getWallItems());
            } else if(message == HandlerMessages.WALL_AVATARS || message == HandlerMessages.WALL_ATTACHMENTS) {
                if(message == HandlerMessages.WALL_AVATARS) {
                    communityFragment.wallAdapter.setAvatarLoadState(true);
                } else {
                    communityFragment.wallAdapter.setPhotoLoadState(true);
                }
            } else if(message == HandlerMessages.GROUP_AVATARS) {
                communityFragment.setData(group);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openProfileFromWall(int position) {
        WallPost post = wall.getWallItems().get(position);
        String url = "";
        if(post.author_id != group.id) {
            if (post.author_id > 0) {
                url = String.format("openvk://profile/id%s", post.author_id);
            } else if (post.author_id < 0) {
                url = String.format("openvk://group/club%s", post.author_id);
            }

            if (url.length() > 0) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
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
