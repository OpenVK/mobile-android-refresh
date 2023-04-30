package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Friends;
import uk.openvk.android.refresh.api.Groups;
import uk.openvk.android.refresh.api.Likes;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.fragments.app.CommunityFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class FriendsIntentActivity extends MonetCompatActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public Handler handler;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    public Account account;
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    private Friends friends;
    private boolean isDarkTheme;
    private FriendsFragment friendsFragment;
    private long user_id;
    private Users users;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"),
                getWindow());
        Global.setInterfaceFont(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.intent_view);
        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            args = path.substring("openvk://friends/".length());
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                setAPIWrapper();
                setAppBar();
                createFragment();
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
                toolbar.setBackgroundColor(Objects.requireNonNull(
                        getMonet().getMonetColors().getAccent1().get(600))
                        .toLinearSrgb().toSrgb().quantize8());
                getWindow().setStatusBarColor(Objects.requireNonNull(
                        getMonet().getMonetColors().getAccent1().get(700))
                        .toLinearSrgb().toSrgb().quantize8());
            }
        }
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        friends = new Friends();
        users = new Users();
        account = new Account(this);
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("OpenVK", String.format("Handling API message: %s", msg.what));
                receiveState(msg.what, msg.getData());
            }
        };
        account.getProfileInfo(ovk_api);
    }

    private void createFragment() {
        setAppBar();
    }

    private void applyFragment() {
        friendsFragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putLong("user_id", user_id);
        args.putLong("account_id", account.id);
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

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"), ovk_api);
                if (args.startsWith("id")) {
                    try {
                        user_id = Integer.parseInt(args.substring(2));
                        friends.get(ovk_api, Integer.parseInt(args.substring(2)),
                                25, "friends_list");
                    } catch (Exception ex) {
                        users.search(ovk_api, args);
                    }
                } else {
                    users.search(ovk_api, args);
                }
                applyFragment();
            } else if (message == HandlerMessages.FRIENDS_GET) {
                friends.parse(data.getString("response"), downloadManager,
                        true, true);
                ArrayList<Friend> friendsList = friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                friendsFragment.disableUpdateState();
                //friendsFragment.setScrollingPositions(this, friends.getFriends().size() > 0);
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                int old_friends_size = friends.getFriends().size();
                friends.parse(data.getString("response"), downloadManager,
                        true, false);
                ArrayList<Friend> friendsList = friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                //friendsFragment.setScrollingPositions(this, old_friends_size != friends.getFriends().size());
            } if (message == HandlerMessages.FRIEND_AVATARS) {
                //friendsFragment.refreshAdapter();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            finish();
        }
    }

    public void refreshFriendsList(boolean progress) {
        friends.get(ovk_api, account.id, 25, "friends_list");
        if (progress) {
            friendsFragment.showProgress();
        }
    }

    public void openProfileFromFriends(int position) {
        Friend friend = friends.getFriends().get(position);
        String url = "";
        url = String.format("openvk://profile/id%s", friend.id);
        if(url.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            final PackageManager pm = getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> activityList =
                    pm.queryIntentActivities(i, 0);
            for (int index = 0; index < activityList.size(); index++) {
                ResolveInfo app = activityList.get(index);
                if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                    i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                }
            }
            startActivity(i);
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

    public void loadMoreFriends() {
        if(friends != null) {
            friends.get(ovk_api, user_id, 25, friends.offset);
        }
    }
}
