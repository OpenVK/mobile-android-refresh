package uk.openvk.android.refresh.user_interface.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.Global;
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
import uk.openvk.android.refresh.api.models.Conversation;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.user_interface.fragments.app.AboutApplicationFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.MainSettingsFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.MessagesFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.NewsfeedFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.PersonalizationFragment;
import uk.openvk.android.refresh.user_interface.fragments.app.ProfileFragment;
import uk.openvk.android.refresh.user_interface.list_adapters.NewsfeedToolbarSpinnerAdapter;
import uk.openvk.android.refresh.user_interface.list_items.ToolbarSpinnerItem;

public class AppActivity extends AppCompatActivity {
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
    private MessagesFragment messagesFragment;
    private ArrayList<Conversation> conversations;
    private MenuItem prevMenuItem;
    private MainSettingsFragment mainSettingsFragment;
    private PersonalizationFragment personalizationFragment;
    private AboutApplicationFragment aboutAppFragment;
    private Fragment selectedFragment;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"));
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.app);
        instance_prefs = getSharedPreferences("instance", 0);
        createFragments();
    }

    private void createFragments() {
        newsfeedFragment = new NewsfeedFragment();
        messagesFragment = new MessagesFragment();
        profileFragment = new ProfileFragment();
        mainSettingsFragment = new MainSettingsFragment();
        personalizationFragment = new PersonalizationFragment();
        aboutAppFragment = new AboutApplicationFragment();
        personalizationFragment.setGlobalPreferences(global_prefs);
        setNavView();
        setAPIWrapper();
        setNavDrawer();
        setAppBar();
        if (newsfeedFragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_screen, newsfeedFragment, "newsfeed");
            ft.add(R.id.fragment_screen, messagesFragment, "messages");
            ft.add(R.id.fragment_screen, profileFragment, "profile");
            ft.add(R.id.fragment_screen, mainSettingsFragment, "main_settings");
            ft.add(R.id.fragment_screen, personalizationFragment, "personalization");
            ft.add(R.id.fragment_screen, aboutAppFragment, "about_app");
            ft.commit();
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(messagesFragment);
            ft.hide(profileFragment);
            ft.hide(mainSettingsFragment);
            ft.hide(personalizationFragment);
            ft.hide(aboutAppFragment);
            selectedFragment = newsfeedFragment;
            ft.show(selectedFragment);
            ft.commit();
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
        ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                .findViewById(R.id.spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshNewsfeed(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        TypedValue typedValue = new TypedValue();
        boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;
        if(isDarkThemeEnabled) {
            getTheme().resolveAttribute(androidx.appcompat.R.attr.background, typedValue, true);
        } else {
            getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
        }
        drawer.setStatusBarBackgroundColor(typedValue.data);
        toggle.syncState();
    }

    private void setNavView() {
        NavigationView navView = findViewById(R.id.nav_view);
        BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
        b_navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return true;
            }
        });
        for(int i = 0; i < b_navView.getMenu().size(); i++) {
            b_navView.getMenu().getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    item.setChecked(true);
                    switchNavItem(item);
                    return false;
                }
            });
        }
        for(int i = 0; i < navView.getMenu().size(); i++) {
            navView.getMenu().getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    if (prevMenuItem != null) {
                        prevMenuItem.setChecked(false);
                    } else {
                        if(item.getItemId() != R.id.newsfeed) navView.getMenu().getItem(0).setChecked(false);
                    }

                    item.setChecked(true);
                    drawer.closeDrawers();
                    prevMenuItem = item;
                    switchNavItem(item);
                    return false;
                }
            });
        }
        String profile_name = getResources().getString(R.string.loading);
        ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_name)).setText(profile_name);
        ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                .setVisibility(View.GONE);
    }

    public void switchNavItem(MenuItem item) {
        int itemId = item.getItemId();
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(selectedFragment);
        if (itemId == R.id.home) {
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("newsfeed"));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle("");
        } else if (itemId == R.id.newsfeed) {
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("newsfeed"));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle("");
        } else if(itemId == R.id.messages) {
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("messages"));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.GONE);
            profileFragment.setData(account.user);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.nav_messages);
            if(messages == null) {
                messages = new Messages();
            }
            messages.getConversations(ovk_api);
        } else if(itemId == R.id.profile) {
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("profile"));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.GONE);
            profileFragment.setData(account.user);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.nav_profile);
        } else if(itemId == R.id.settings) {
            ft.hide(selectedFragment);
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("settings"));
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.GONE);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.nav_settings);
        }
        ft.show(selectedFragment);
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
                newsfeed.parse(this, downloadManager, data.getString("response"), "medium", true);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.disableUpdateState();
            } else if (message == HandlerMessages.NEWSFEED_GET_GLOBAL) {
                newsfeed.parse(this, downloadManager, data.getString("response"), "medium", true);
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
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                            .setVisibility(View.VISIBLE);
                } else {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                            .setVisibility(View.GONE);
                }
            } else if(message == HandlerMessages.MESSAGES_CONVERSATIONS) {
                conversations = messages.parseConversationsList(data.getString("response"), downloadManager);
                messagesFragment.createAdapter(this, conversations, account);
            } else if(message == HandlerMessages.CONVERSATIONS_AVATARS) {
                messagesFragment.loadAvatars(conversations);
            } else if(message == HandlerMessages.NEWSFEED_AVATARS || message == HandlerMessages.NEWSFEED_ATTACHMENTS) {
                newsfeedFragment.refreshAdapter();
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                Glide.with(this).load(
                                String.format("%s/photos_cache/account_avatar/avatar_%s",
                                        getCacheDir().getAbsolutePath(), account.user.id))
                        .into((ImageView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_avatar));
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
        } else {
            account = new Account(this);
            account.addQueue("Newsfeed.get", "count=25&extended=1");
            account.getProfileInfo(ovk_api);
        }
    }

    public void refreshNewsfeed(boolean progress) {
        if(newsfeed.getWallPosts() != null) {
            int pos = ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar)).findViewById(R.id.spinner)).getSelectedItemPosition();

            if (account != null) {
                if (pos == 0) {
                    newsfeed.get(ovk_api, 25);
                    if (progress) {
                        newsfeedFragment.showProgress();
                    }
                }
                else
                    Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void switchFragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("newsfeed")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("messages")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("profile")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("main_settings")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("personalization")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("about_app")));
        if(tag.equals("personalization")) {
            ft.show(Objects.requireNonNull(fm.findFragmentByTag("personalization")));
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_personalization);
        } else {
            ft.show(Objects.requireNonNull(fm.findFragmentByTag("about_app")));
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_about_app);
        }
        ft.commit();
    }
}
