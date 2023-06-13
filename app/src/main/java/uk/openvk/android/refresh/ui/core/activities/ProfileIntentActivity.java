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
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;

import org.json.JSONObject;

import java.util.Locale;

import dev.kdrag0n.monet.theme.ColorScheme;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Friends;
import uk.openvk.android.refresh.api.Likes;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.JSONParser;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;
import uk.openvk.android.refresh.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class ProfileIntentActivity extends MonetCompatActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public Handler handler;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private Wall wall;
    public Account account;
    private Likes likes;
    private ProfileFragment profileFragment;
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    private Users users;
    private Friends friends;
    private User user;
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
        setMonetTheme();
        final Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            args = path.substring("openvk://profile/".length());
            if (instance_prefs.getString("access_token", "").length() == 0) {
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
        if(item.getItemId() == R.id.delete_friend) {
            if(user.friends_status > 1) {
                deleteFromFriends(user.id);
            }
        } else if(item.getItemId() == R.id.copy_link) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            String url = "";
            if(account.user.screen_name != null && account.user.screen_name.length() > 0) {
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
            if(account.user.screen_name != null && account.user.screen_name.length() > 0) {
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
        if(user_id != account.id) {
            friends.delete(ovk_api, user_id);
        }
    }

    private void setMonetTheme() {
        if(Global.checkMonet(this)) {
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            if (!isDarkTheme) {
                toolbar.setBackgroundColor(
                        Global.getMonetIntColor(getMonet(), "accent", 600));
                getWindow().setStatusBarColor(
                        Global.getMonetIntColor(getMonet(), "accent", 700));
            }
        }
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        users = new Users();
        friends = new Friends();
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

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.OVKAPI_ACCOUNT_PROFILE_INFO) {
                if (args.startsWith("id")) {
                    account.parse(data.getString("response"), ovk_api);
                    MaterialToolbar appBar = findViewById(R.id.app_toolbar);
                    appBar.getMenu().clear();
                    appBar.inflateMenu(R.menu.profile);
                    if(appBar.getMenu().getItem(0).getItemId() == R.id.delete_friend) {
                        if (Integer.parseInt(args.substring(2)) == account.id) {
                            appBar.getMenu().removeItem(R.id.delete_friend);
                        }
                    }
                    try {
                        users.getUser(ovk_api, Integer.parseInt(args.substring(2)));
                    } catch (Exception ex) {
                        users.search(ovk_api, args);
                    }
                } else {
                    users.search(ovk_api, args);
                }
                profileFragment.header.setCountersVisibility(PublicPageCounters.MEMBERS, false);
                profileFragment.header.setCountersVisibility(PublicPageCounters.FRIENDS, true);
            } else if (message == HandlerMessages.OVKAPI_USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileFragment.setData(user, friends, account, ovk_api);
                MaterialToolbar appBar = findViewById(R.id.app_toolbar);
                if(appBar.getMenu().getItem(0).getItemId() == R.id.delete_friend) {
                    if (user.id == account.id) {
                        appBar.getMenu().removeItem(R.id.delete_friend);
                    } else if (user.friends_status < 3) {
                        appBar.getMenu().removeItem(R.id.delete_friend);
                    }
                }
                user.downloadAvatar(downloadManager, "high", "profile_avatars");
                wall.get(ovk_api, user.id, 50);
                friends.get(ovk_api, user.id, 25, "profile_counter");
            } else if (message == HandlerMessages.OVKAPI_FRIENDS_GET_ALT) {
                friends.parse(data.getString("response"), downloadManager,
                        false, true);
                profileFragment.setFriendsCount(friends.count);
            } else if(message == HandlerMessages.OVKAPI_USERS_SEARCH) {
                users.parseSearch(data.getString("response"));
                users.getUser(ovk_api, users.getList().get(0).id);
            } else if (message == HandlerMessages.OVKAPI_WALL_GET) {
                wall.parse(this, downloadManager, "high", data.getString("response"));
                profileFragment.createWallAdapter(this, wall.getWallItems());
            } else if(message == HandlerMessages.DLM_WALL_AVATARS
                    || message == HandlerMessages.DLM_WALL_ATTACHMENTS) {
                if (profileFragment.getWallAdapter() == null) {
                    profileFragment.createWallAdapter(this, wall.getWallItems());
                }
                try {
                    if (message == HandlerMessages.DLM_WALL_AVATARS) {
                        profileFragment.getWallAdapter().setAvatarLoadState(true);
                    } else {
                        profileFragment.getWallAdapter().setPhotoLoadState(true);
                    }
                } catch (Exception ignored) {
                }
                profileFragment.refreshWallAdapter();
            } else if(message == HandlerMessages.OVKAPI_FRIENDS_ADD) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                user.friends_status = response.getInt("response");
                profileFragment.setFriendStatus(account.user, user.friends_status);
            } else if(message == HandlerMessages.OVKAPI_FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = 0;
                }
                profileFragment.setFriendStatus(account.user, user.friends_status);
            } else if(message == HandlerMessages.DLM_PROFILE_AVATARS) {
                profileFragment.setData(user, friends, account, ovk_api);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openProfileFromWall(int position) {
        WallPost post = wall.getWallItems().get(position);
        String url = "";
        if(post.author_id != user.id) {
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
    public void onMonetColorsChanged(@NonNull MonetCompat monet, @NonNull ColorScheme monetColors,
                                     boolean isInitialChange) {
        super.onMonetColorsChanged(monet, monetColors, isInitialChange);
        getMonet().updateMonetColors();
        setMonetTheme();
    }

    public void setToolbarTitle(String title, String subtitle) {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }
}
