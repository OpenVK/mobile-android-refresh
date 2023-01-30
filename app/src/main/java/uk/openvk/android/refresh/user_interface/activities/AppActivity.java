package uk.openvk.android.refresh.user_interface.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
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
import uk.openvk.android.refresh.user_interface.fragments.app.ProfileFragment;
import uk.openvk.android.refresh.user_interface.list_adapters.NewsfeedToolbarSpinnerAdapter;
import uk.openvk.android.refresh.user_interface.list_items.ToolbarSpinnerItem;

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
    private ProfileFragment profileFragment;
    private FragmentTransaction ft;
    private ArrayList<ToolbarSpinnerItem> tbSpinnerItems;
    private NewsfeedToolbarSpinnerAdapter tbSpinnerAdapter;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app);
        instance_prefs = getSharedPreferences("instance", 0);
        newsfeedFragment = new NewsfeedFragment();
        profileFragment = new ProfileFragment();
        setNavView();
        setAPIWrapper();
        setNavDrawer();
        setAppBar();
        if (newsfeedFragment != null) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_screen, newsfeedFragment, "newsfeed");
            ft.add(R.id.fragment_screen, profileFragment, "profile");
            ft.commit();
            ft.hide(profileFragment);
        }
    }

    private void setAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.open();
            }
        });
        tbSpinnerItems = new ArrayList<>();
        tbSpinnerItems.add(new ToolbarSpinnerItem(getResources().getString(R.string.nav_news), getResources().getString(R.string.my_news_item), 60));
        tbSpinnerItems.add(new ToolbarSpinnerItem(getResources().getString(R.string.nav_news), getResources().getString(R.string.all_news_item), 61));
        tbSpinnerAdapter = new NewsfeedToolbarSpinnerAdapter(this, tbSpinnerItems);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setAdapter(tbSpinnerAdapter);
        ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                .findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
        ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"CutPasteId", "ObsoleteSdkInt"})
    private void setNavDrawer() {
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_screen);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        @SuppressLint("CutPasteId") AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).setDrawerLayout(
                ((DrawerLayout) findViewById(R.id.drawer_layout))).build();
        NavigationUI.setupWithNavController(navView, navController);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, android.R.string.ok, android.R.string.cancel);
        drawer.addDrawerListener(toggle);
        drawer.setStatusBarBackground(R.color.statusBarColor);
        toggle.syncState();
    }

    private void setNavView() {
        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return true;
            }
        });
        for(int i = 0; i < navView.getMenu().size(); i++) {
            navView.getMenu().getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    item.setChecked(true);
                    switchNavItem(item);
                    return false;
                }
            });
        }
    }

    public void switchNavItem(MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        if (itemId == R.id.home) {
            ft.hide(Objects.requireNonNull(fm.findFragmentByTag("profile")));
            ft.show(Objects.requireNonNull(fm.findFragmentByTag("newsfeed")));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle("");
        } else if(itemId == R.id.profile) {
            ft.hide(Objects.requireNonNull(fm.findFragmentByTag("newsfeed")));
            ft.show(Objects.requireNonNull(fm.findFragmentByTag("profile")));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.GONE);
            profileFragment.setData(account.user);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.nav_profile);
        }
        ft.commit();
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
                receiveState(msg.what, msg.getData());
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

    private void receiveState(int message, Bundle data) {
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
                if(account.user.screen_name != null && account.user.screen_name.length() > 0) {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                            .setText(String.format("@%s", account.user.screen_name));
                } else {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                            .setVisibility(View.GONE);
                }
            } else if(message < 0) {
                newsfeedFragment.setError(true, message, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        retryConnection();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void retryConnection() {
        if(account != null) {
            newsfeedFragment.setError(false, 0, null);
            newsfeed.get(ovk_api, 25);
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
