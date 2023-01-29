package uk.openvk.android.refresh.user_interface.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Friends;
import uk.openvk.android.refresh.api.Groups;
import uk.openvk.android.refresh.api.Likes;
import uk.openvk.android.refresh.api.Messages;
import uk.openvk.android.refresh.api.Newsfeed;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.user_interface.fragments.app.NewsfeedFragment;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public Handler handler;
    private OvkAPIWrapper ovk_api;
    private SharedPreferences instance_prefs;
    private SharedPreferences global_prefs;
    private DownloadManager downloadManager;
    private Account account;
    private Newsfeed newsfeed;
    private User user;
    private Likes likes;
    private Messages messages;
    private Users users;
    private Friends friends;
    private Groups groups;
    private Wall wall;
    private NewsfeedFragment newsfeedFragment;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app);
        instance_prefs = getSharedPreferences("instance", 0);
        newsfeedFragment = new NewsfeedFragment();
        if (newsfeedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_screen, newsfeedFragment).commit();
        }
        setNavView();
        setAPIWrapper();
        setNavDrawer();
        setAppBar();
    }

    private void setAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.open();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNavDrawer() {
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_screen);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).setDrawerLayout(
                ((DrawerLayout) findViewById(R.id.drawer_layout))).build();
        NavigationUI.setupWithNavController(navView, navController);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, android.R.string.ok, android.R.string.cancel);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setNavView() {
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    selectedFragment = newsfeedFragment;
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_screen, selectedFragment).commit();
                }
                return false;
            }
        });
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("OpenVK", String.format("Handling API message: %s", msg.what));
                recieveState(msg.what, msg.getData());
            }
        };
        account = new Account(this);
        account.getProfileInfo(ovk_api);
        newsfeed = new Newsfeed();
        user = new User();
        likes = new Likes();
        messages = new Messages();
        users = new Users();
        friends = new Friends();
        groups = new Groups();
        wall = new Wall();
    }

    private void recieveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"), ovk_api);
                newsfeed.get(ovk_api, 25);
                users.getAccountUser(ovk_api, account.id);
            } else if (message == HandlerMessages.NEWSFEED_GET) {
                newsfeed.parse(this, downloadManager, data.getString("response"), "original", true);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.disableUpdateState();
            } else if (message == HandlerMessages.USERS_GET_ALT) {
                users.parse(data.getString("response"));
                account.user = users.getList().get(0);
                account.user.downloadAvatar(downloadManager, "high", "account_avatar");
                String profile_name = String.format("%s %s", account.first_name, account.last_name);
                ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_name))
                        .setText(profile_name);
                if(account.user.screen_name.length() > 0) {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                            .setText(String.format("@%s", account.user.screen_name));
                } else {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                            .setVisibility(View.GONE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshNewsfeed() {
        if(account != null) {
            newsfeed.get(ovk_api, 25);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
