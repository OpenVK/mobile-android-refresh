package uk.openvk.android.refresh.user_interface.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.viewbinding.BuildConfig;

import com.google.android.material.appbar.MaterialToolbar;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Likes;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.user_interface.fragments.app.AboutApplicationFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.FriendsFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.MainSettingsFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.MessagesFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.NewsfeedFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.PersonalizationFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.ProfileFragment;

public class ProfileIntentActivity extends AppCompatActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public Handler handler;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private Wall wall;
    private Account account;
    private Likes likes;
    private ProfileFragment profileFragment;
    private FragmentTransaction ft;
    private String args;
    private MaterialToolbar toolbar;
    private Users users;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"));
        instance_prefs = getSharedPreferences("instance", 0);
        setContentView(R.layout.intent_view);
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

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        users = new Users();
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
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                if (args.startsWith("id")) {
                    account.parse(data.getString("response"), ovk_api);
                    try {
                        users.getUser(ovk_api, Integer.parseInt(args.substring(2)));
                    } catch (Exception ex) {
                        users.search(ovk_api, args);
                    }
                } else {
                    users.search(ovk_api, args);
                }
            } else if (message == HandlerMessages.USERS_GET) {
                users.parse(data.getString("response"));
                user = users.getList().get(0);
                profileFragment.setData(user);
                user.downloadAvatar(downloadManager, "high", "profile_avatars");
                wall.get(ovk_api, user.id, 50);
            } else if(message == HandlerMessages.USERS_SEARCH) {
                users.parseSearch(data.getString("response"));
                users.getUser(ovk_api, users.getList().get(0).id);
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, "high", data.getString("response"));
                profileFragment.createWallAdapter(this, wall.getWallItems());
            } else if(message == HandlerMessages.WALL_AVATARS || message == HandlerMessages.WALL_ATTACHMENTS) {
                if(message == HandlerMessages.WALL_AVATARS) {
                    profileFragment.wallAdapter.setAvatarLoadState(true);
                } else {
                    profileFragment.wallAdapter.setPhotoLoadState(true);
                }
            } else if(message == HandlerMessages.PROFILE_AVATARS) {
                profileFragment.setData(user);
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
}
