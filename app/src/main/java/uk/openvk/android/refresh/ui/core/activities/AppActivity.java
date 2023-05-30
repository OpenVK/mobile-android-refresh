package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.LocaleManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.LocaleListCompat;
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
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;

import org.json.JSONObject;

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
import uk.openvk.android.refresh.api.Messages;
import uk.openvk.android.refresh.api.Newsfeed;
import uk.openvk.android.refresh.api.Ovk;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Conversation;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.api.models.LongPollServer;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.JSONParser;
import uk.openvk.android.refresh.api.wrappers.NotificationManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.longpoll_api.LongPollService;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;
import uk.openvk.android.refresh.ui.core.fragments.app.AboutApplicationFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.GroupsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.MainSettingsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.MessagesFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.PersonalizationFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.VideoSettingsFragment;
import uk.openvk.android.refresh.ui.list.adapters.NewsfeedToolbarSpinnerAdapter;
import uk.openvk.android.refresh.ui.list.items.ToolbarSpinnerItem;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class AppActivity extends MonetCompatActivity {
    public Handler handler;
    public OvkAPIWrapper ovk_api;
    private SharedPreferences instance_prefs;
    private SharedPreferences global_prefs;
    private DownloadManager downloadManager;
    public Account account;
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
    private MenuItem prevBottomMenuItem;
    private FriendsFragment friendsFragment;
    private String current_fragment;
    private boolean isDarkTheme;
    private GroupsFragment groupsFragment;

    private boolean mShouldUnbind;
    private LongPollService longPollService;
    private LongPollServer longPollServer;
    private Intent longPollIntent;
    private VideoSettingsFragment videoSettingsFragment;
    private NotificationManager notifMan;
    private int screenOrientation;
    private int navBarHeight;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        screenOrientation = getResources().getConfiguration().orientation;
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"), getWindow());
        Global.setInterfaceFont(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);

        notifMan = new NotificationManager(this, true, true, true, "");
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                current_fragment = extras.getString("current_fragment");
            }
        } else {
            current_fragment = savedInstanceState.getString("current_fragment");
        }
        setContentView(R.layout.activity_app);
        instance_prefs = getSharedPreferences("instance", 0);
        createFragments();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
        Global.setPerAppLanguage(this);
    }

    private void createFragments() {
        newsfeedFragment = new NewsfeedFragment();
        friendsFragment = new FriendsFragment();
        groupsFragment = new GroupsFragment();
        messagesFragment = new MessagesFragment();
        profileFragment = new ProfileFragment();
        mainSettingsFragment = new MainSettingsFragment();
        personalizationFragment = new PersonalizationFragment();
        aboutAppFragment = new AboutApplicationFragment();
        videoSettingsFragment = new VideoSettingsFragment();
        videoSettingsFragment.setGlobalPreferences(global_prefs);
        personalizationFragment.setGlobalPreferences(global_prefs);
        setNavView();
        setAPIWrapper();
        setNavDrawer();
        setAppBar();
        setMonetTheme();
        setFloatingActionButton();
        if (newsfeedFragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_screen, newsfeedFragment, "newsfeed");
            ft.add(R.id.fragment_screen, friendsFragment, "friends");
            ft.add(R.id.fragment_screen, groupsFragment, "groups");
            ft.add(R.id.fragment_screen, messagesFragment, "messages");
            ft.add(R.id.fragment_screen, profileFragment, "profile");
            ft.add(R.id.fragment_screen, mainSettingsFragment, "settings");
            ft.add(R.id.fragment_screen, videoSettingsFragment, "video_settings");
            ft.add(R.id.fragment_screen, personalizationFragment, "personalization");
            ft.add(R.id.fragment_screen, aboutAppFragment, "about_app");
            ft.commit();
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(friendsFragment);
            ft.hide(groupsFragment);
            ft.hide(messagesFragment);
            ft.hide(profileFragment);
            ft.hide(mainSettingsFragment);
            ft.hide(videoSettingsFragment);
            ft.hide(personalizationFragment);
            ft.hide(aboutAppFragment);
            selectedFragment = newsfeedFragment;
            ft.show(selectedFragment);
            ft.commit();
        }
        if(current_fragment != null && current_fragment.equals("personalization")) {
            ft.hide(selectedFragment);
            selectedFragment = personalizationFragment;
            ft.show(selectedFragment);
            ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).setVisibility(View.GONE);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_personalization);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setNavigationIcon(R.drawable.ic_arrow_back);
            NavigationView navView = findViewById(R.id.nav_view);
            navView.getMenu().getItem(4).setChecked(true);
            prevMenuItem = navView.getMenu().getItem(4);
            navView.getMenu().getItem(1).setChecked(false);
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        }
    }

    // Restarting to restore activity to its normal state or to apply theme and font changes
    public void restart() {
        Intent intent = new Intent(this, AppActivity.class);
        if(selectedFragment != null) {
            if (selectedFragment == personalizationFragment) {
                intent.putExtra("current_fragment", "personalization");
            }
        }
        startActivity(intent);
        finishAffinity();
    }

    // Setting Application Bar (by default, newsfeed fragment using custom layout with combo-box)
    private void setAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        tbSpinnerItems = new ArrayList<>();
        tbSpinnerItems.add(new ToolbarSpinnerItem(
                getResources().getString(R.string.nav_news),
                getResources().getString(R.string.my_news_item), 60));
        tbSpinnerItems.add(new ToolbarSpinnerItem(
                getResources().getString(R.string.nav_news),
                getResources().getString(R.string.all_news_item), 61));
        tbSpinnerAdapter = new NewsfeedToolbarSpinnerAdapter(this, tbSpinnerItems);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setAdapter(tbSpinnerAdapter);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshNewsfeed(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(v -> {
            if(selectedFragment == mainSettingsFragment
                    || selectedFragment == videoSettingsFragment
                    || selectedFragment == personalizationFragment
                    || selectedFragment == aboutAppFragment) {
                onBackPressed();
            } else {
                if(OvkApplication.isTablet
                        && screenOrientation == Configuration.ORIENTATION_LANDSCAPE){
                    try {
                        NavigationView navView = findViewById(R.id.nav_view);
                        if (navView.getVisibility() == View.VISIBLE) {
                            navView.setVisibility(View.GONE);
                        } else {
                            navView.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception ignored) {

                    }
                } else {
                    try {
                        drawer.open();
                    } catch (Exception ignored) {

                    }
                }
            }
        });
    }

    public void setFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab_newpost);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
                try {
                    intent.putExtra("owner_id", account.user.id);
                    intent.putExtra("account_id", account.id);
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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

    @SuppressLint({"CutPasteId", "ObsoleteSdkInt"})
    private void setNavDrawer() {
        try {
            BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_screen);
            NavController navController = Objects.requireNonNull(navHostFragment)
                    .getNavController();
            @SuppressLint("CutPasteId") AppBarConfiguration appBarConfiguration =
                    new AppBarConfiguration.Builder(navController.getGraph()).setDrawerLayout(
                    ((DrawerLayout) findViewById(R.id.drawer_layout))).build();
            NavigationUI.setupWithNavController(navView, navController);
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            toggle = new ActionBarDrawerToggle(this, drawer, android.R.string.ok,
                    android.R.string.cancel);
            drawer.addDrawerListener(toggle);
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
            drawer.setStatusBarBackgroundColor(typedValue.data);
            toggle.syncState();
        } catch (Exception ex) {
            restart();
        }
    }

    public void setNavView() {
        NavigationView navView = findViewById(R.id.nav_view);
        BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
        b_navView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return true;
            }
        });
        for(int i = 0; i < b_navView.getMenu().size(); i++) {
            b_navView.getMenu().getItem(i).setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    switchNavItem(item);
                    return false;
                }
            });
        }
        for(int i = 0; i < navView.getMenu().size(); i++) {
            navView.getMenu().getItem(i).setOnMenuItemClickListener(
                    new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    try {
                        drawer.closeDrawers();
                        switchNavItem(item);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
            });
        }
        ConstraintLayout header = (ConstraintLayout) navView.getHeaderView(0);
        header.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuickSearchActivity();
            }
        });
        if(account == null || account.user == null) {
            String profile_name = getResources().getString(R.string.loading);
            ((TextView) header.findViewById(R.id.profile_name)).setText(profile_name);
            header.findViewById(R.id.screen_name).setVisibility(View.GONE);
        }
        @SuppressLint("CutPasteId") ShapeableImageView avatar = ((ShapeableImageView)
                ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                        .findViewById(R.id.profile_avatar));
        Global.setAvatarShape(this, avatar);
        ((FloatingActionButton) findViewById(R.id.fab_newpost)).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void startQuickSearchActivity() {
        Intent intent = new Intent(getApplicationContext(), QuickSearchActivity.class);
        try {
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
        Setting Monet color scheme on unsupported views (in particular Material Design 2)
        using MonetCompat library:
        https://github.com/KieronQuinn/MonetCompat (X11 License)
    */
    private void setMonetTheme() {
        try {
            if (Global.checkMonet(this)) {
                MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
                if (!isDarkTheme) {
                    toolbar.setBackgroundColor(
                            Global.getMonetIntColor(getMonet(), "accent", 600));
                    drawer.setStatusBarBackgroundColor(
                            Global.getMonetIntColor(getMonet(), "accent", 700));
                }
                int colorOnSurface = MaterialColors.getColor(this,
                        com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
                NavigationView navView = findViewById(R.id.nav_view);
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_checked}, new int[]{}
                };
                int[] colors;
                if (isDarkTheme) {
                    colors = new int[]{
                            Global.getMonetIntColor(getMonet(), "accent", 100),
                            Global.adjustAlpha(colorOnSurface, 0.6f)
                    };
                } else {
                    colors = new int[]{
                            Global.getMonetIntColor(getMonet(), "accent", 500),
                            Global.adjustAlpha(colorOnSurface, 0.6f)
                    };
                }
                ColorStateList csl = new ColorStateList(states, colors);
                navView.setItemIconTintList(csl);
                navView.setItemTextColor(csl);

                BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
                if (isDarkTheme) {
                    colors = new int[]{
                            Global.getMonetIntColor(getMonet(), "accent", 200),
                            Global.adjustAlpha(colorOnSurface, 0.6f)
                    };
                } else {
                    colors = new int[]{
                            Global.getMonetIntColor(getMonet(), "accent", 500),
                            Global.adjustAlpha(colorOnSurface, 0.6f)
                    };
                }
                csl = new ColorStateList(states, colors);
                b_navView.setItemTextColor(csl);
                b_navView.setItemIconTintList(csl);
                b_navView.setItemRippleColor(ColorStateList.valueOf(
                        getMonet().getPrimaryColor(this, isDarkTheme)));
                FloatingActionButton fab = findViewById(R.id.fab_newpost);
                fab.setBackgroundTintList(ColorStateList.valueOf(
                        Global.getMonetIntColor(getMonet(), "accent", 700)));
                fab.setImageTintList(ColorStateList.valueOf(
                        Global.getMonetIntColor(getMonet(), "accent", 100)));
                fab.setRippleColor(ColorStateList.valueOf(
                        Global.getMonetIntColor(getMonet(), "accent", 400)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void switchNavItem(MenuItem item) {
        try {
            int itemId = item.getItemId();
            BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
            NavigationView navView = findViewById(R.id.nav_view);
            FragmentManager fm = getSupportFragmentManager();
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(selectedFragment);
            if (prevBottomMenuItem != null) prevBottomMenuItem.setChecked(false);
            else b_navView.getMenu().getItem(0).setChecked(false);
            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);
            } else {
                navView.getMenu().getItem(1).setChecked(false);
            }
            if (itemId == R.id.home) {
                prevBottomMenuItem = b_navView.getMenu().getItem(0);
                prevMenuItem = navView.getMenu().getItem(1);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("newsfeed"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
                ((NewsfeedFragment) selectedFragment).refreshAdapter();
                toolbar.setTitle("");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                b_navView.getMenu().getItem(0).setChecked(true);
                navView.getMenu().getItem(1).setChecked(true);
                if (newsfeed.getWallPosts() != null) {
                    findViewById(R.id.fab_newpost).setVisibility(View.VISIBLE);
                }
            } else if (itemId == R.id.newsfeed) {
                prevBottomMenuItem = b_navView.getMenu().getItem(0);
                prevMenuItem = navView.getMenu().getItem(1);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("newsfeed"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
                toolbar.setTitle("");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                ((NewsfeedFragment) selectedFragment).refreshAdapter();
                b_navView.getMenu().getItem(0).setChecked(true);
                navView.getMenu().getItem(1).setChecked(true);
                if (newsfeed.getWallPosts() != null) {
                    findViewById(R.id.fab_newpost).setVisibility(View.VISIBLE);
                }
            } else if (itemId == R.id.friends) {
                prevBottomMenuItem = b_navView.getMenu().getItem(1);
                prevMenuItem = navView.getMenu().getItem(2);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("friends"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.GONE);
                profileFragment.setData(account.user, friends, account, ovk_api);
                toolbar.setTitle(R.string.nav_friends);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (friendsFragment.getFriendsCount() == 0) {
                    friends.get(ovk_api, account.id, 25, "friends_list");
                }
                b_navView.getMenu().getItem(1).setChecked(true);
                navView.getMenu().getItem(2).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.groups) {
                prevBottomMenuItem = b_navView.getMenu().getItem(1);
                prevMenuItem = navView.getMenu().getItem(3);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("groups"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.GONE);
                profileFragment.setData(account.user, friends, account, ovk_api);
                toolbar.setTitle(R.string.nav_groups);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (groups.getList() == null || groups.getList().size() == 0) {
                    groups.getGroups(ovk_api, account.id, 25);
                }
                b_navView.getMenu().getItem(3).setChecked(true);
                navView.getMenu().getItem(3).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.messages) {
                prevBottomMenuItem = b_navView.getMenu().getItem(2);
                prevMenuItem = navView.getMenu().getItem(4);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("messages"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.GONE);
                profileFragment.setData(account.user, friends, account, ovk_api);
                toolbar.setTitle(R.string.nav_messages);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (conversations == null) {
                    messages.getConversations(ovk_api);
                }
                ((MessagesFragment) selectedFragment).refreshAdapter();
                b_navView.getMenu().getItem(2).setChecked(true);
                navView.getMenu().getItem(4).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.profile) {
                prevBottomMenuItem = b_navView.getMenu().getItem(4);
                prevMenuItem = navView.getMenu().getItem(0);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("profile"));
                ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setVisibility(View.GONE);
                profileFragment.setData(account.user,friends, account, ovk_api);
                profileFragment.header.setCountersVisibility(PublicPageCounters.MEMBERS, false);
                profileFragment.header.setCountersVisibility(PublicPageCounters.FRIENDS, true);
                if (wall.getWallItems() == null) {
                    wall.get(ovk_api, account.user.id, 50);
                }
                toolbar.setTitle(R.string.nav_profile);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                b_navView.getMenu().getItem(4).setChecked(true);
                navView.getMenu().getItem(0).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                if(wall.getWallItems() != null) profileFragment.recreateWallAdapter();
                if(profileFragment.aboutItems != null) profileFragment.recreateAboutAdapter();
            } else if (itemId == R.id.settings) {
                ft.hide(selectedFragment);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("settings"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.GONE);
                toolbar.setTitle(R.string.nav_settings);
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                navView.getMenu().getItem(5).setChecked(true);
                prevMenuItem = navView.getMenu().getItem(5);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            }
            ft.show(selectedFragment);
            ft.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setAPIWrapper() {
        ((OvkApplication) getApplicationContext()).ovk_api = new OvkAPIWrapper(this);
        ovk_api = ((OvkApplication) getApplicationContext()).ovk_api;
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

    public Fragment getSelectedFragment() {
        return selectedFragment;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void receiveState(int message, Bundle data) {
        // Handling OpenVK API and UI messages
        try {
            if (message == HandlerMessages.OVKAPI_ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"), ovk_api);
                newsfeed.get(ovk_api, 25);
                users.getAccountUser(ovk_api, account.id);
                messages.getLongPollServer(ovk_api);
                messages.getConversations(ovk_api);
            } else if (message == HandlerMessages.OVKAPI_ACCOUNT_COUNTERS) {
                account.parseCounters(data.getString("response"));
                BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
                int accentColor;
                if(Global.checkMonet(this) && !OvkApplication.isTablet) {
                    if(isDarkTheme) {
                        accentColor = Global.getMonetIntColor(
                                MonetCompat.getInstance(), "accent", 200);
                    } else {
                        accentColor = Global.getMonetIntColor(
                                MonetCompat.getInstance(), "accent", 500);
                    }
                } else {
                    accentColor = MaterialColors.getColor(this, com.kieronquinn
                                    .monetcompat.R.attr.colorAccent,
                            getResources().getColor(R.color.accentColorRed));
                }
                if(account.counters.friends_requests > 0) {
                    b_navView.getOrCreateBadge(R.id.friends)
                            .setNumber(account.counters.friends_requests);
                    b_navView.getOrCreateBadge(R.id.friends)
                            .setBackgroundColor(accentColor);
                }
                if(account.counters.new_messages > 0) {
                    b_navView.getOrCreateBadge(R.id.messages)
                            .setNumber(account.counters.new_messages);
                    b_navView.getOrCreateBadge(R.id.messages)
                            .setBackgroundColor(accentColor);
                }
            } else if (message == HandlerMessages.OVKAPI_NEWSFEED_GET) {
                newsfeed.parse(this, downloadManager, data.getString("response"),
                        "high", true);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.disableUpdateState();
                if(selectedFragment == newsfeedFragment) {
                    findViewById(R.id.fab_newpost).setVisibility(View.VISIBLE);
                }
                newsfeedFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.OVKAPI_NEWSFEED_GET_GLOBAL) {
                newsfeed.parse(this, downloadManager, data.getString("response"),
                        "high", true);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.setScrollingPositions(this, true);
                newsfeedFragment.disableUpdateState();
            } else if (message == HandlerMessages.OVKAPI_NEWSFEED_GET_MORE) {
                newsfeed.parse(this, downloadManager, data.getString("response"),
                        global_prefs.getString("photos_quality", ""), false);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.OVKAPI_NEWSFEED_GET_MORE_GLOBAL) {
                newsfeed.parse(this, downloadManager, data.getString("response"),
                        global_prefs.getString("photos_quality", ""), false);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.setScrollingPositions(this, true);
            } else if(message == HandlerMessages.OVKAPI_LIKES_ADD) {
                likes.parse(data.getString("response"));
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedFragment.select(likes.position, "likes", 1);
                } else {
                    global_prefs.getString("current_screen", "");
                    //((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.OVKAPI_LIKES_DELETE) {
                likes.parse(data.getString("response"));
                if (selectedFragment == newsfeedFragment) {
                    newsfeedFragment.select(likes.position, "likes", 0);
                } else if (selectedFragment == profileFragment) {
                    newsfeedFragment.select(likes.position, "likes", 0);
                }
            } else if (message == HandlerMessages.OVKAPI_USERS_GET_ALT) {
                users.parse(data.getString("response"));
                account.user = users.getList().get(0);
                account.user.downloadAvatar(downloadManager, "high", "account_avatar");
                String profile_name = String.format("%s %s", account.first_name, account.last_name);
                ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                        .findViewById(R.id.profile_name))
                        .setText(profile_name);
                if(account.user.screen_name != null && account.user.screen_name.length() > 0) {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                            .findViewById(R.id.screen_name))
                            .setText(String.format("@%s", account.user.screen_name));
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                            .findViewById(R.id.screen_name))
                            .setVisibility(View.VISIBLE);
                } else {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                            .findViewById(R.id.screen_name))
                            .setVisibility(View.GONE);
                }
                friends.get(ovk_api, account.user.id, 25, "profile_counter");
            } else if(message == HandlerMessages.OVKAPI_MESSAGES_CONVERSATIONS) {
                conversations = messages.parseConversationsList(data.getString("response"),
                        downloadManager);
                messagesFragment.createAdapter(this, conversations, account);
                messagesFragment.disableUpdateState();
            } else if (message == HandlerMessages.OVKAPI_MESSAGES_GET_LONGPOLL_SERVER) {
                longPollServer = messages.parseLongPollServer(data.getString("response"));
                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                longPollIntent = new Intent(this, LongPollService.class);
                PendingIntent pendingIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getService(this, 0, longPollIntent,
                            PendingIntent.FLAG_MUTABLE);
                } else {
                    pendingIntent = PendingIntent.getService(this, 0, longPollIntent, PendingIntent.FLAG_IMMUTABLE);
                }
                longPollIntent.setPackage("uk.openvk.android.refresh.longpoll_api");
                longPollIntent.putExtra("access_token", instance_prefs.getString("access_token", ""));
                longPollIntent.putExtra("instance", instance_prefs.getString("server", ""));
                longPollIntent.putExtra("server", longPollServer.address);
                longPollIntent.putExtra("key", longPollServer.key);
                longPollIntent.putExtra("ts", longPollServer.ts);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(longPollIntent);
                } else {
                    startService(longPollIntent);
                }
                bindService(longPollIntent, lpConnection, Context.BIND_AUTO_CREATE);
                // Wake up service
                mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis(), 60 * 1000, pendingIntent);
            } else if (message == HandlerMessages.OVKAPI_FRIENDS_GET_ALT) {
                friends.parse(data.getString("response"), downloadManager, false,
                        true);
                profileFragment.setFriendsCount(friends.count);
            } else if (message == HandlerMessages.OVKAPI_FRIENDS_GET) {
                friends.parse(data.getString("response"), downloadManager, true,
                        true);
                ArrayList<Friend> friendsList = friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                friendsFragment.disableUpdateState();
                friendsFragment.setScrollingPositions(this, friends.getFriends().size() > 0);
                friends.getRequests(ovk_api);
            } else if (message == HandlerMessages.OVKAPI_FRIENDS_GET_MORE) {
                int old_friends_size = friends.getFriends().size();
                friends.parse(data.getString("response"), downloadManager, true,
                        false);
                ArrayList<Friend> friendsList = friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                friendsFragment.setScrollingPositions(this, old_friends_size !=
                        friends.getFriends().size());
            } else if (message == HandlerMessages.OVKAPI_FRIENDS_REQUESTS) {
                int old_friends_size = friends.getFriends().size();
                friends.parseRequests(data.getString("response"), downloadManager, true);
                ArrayList<Friend> friendsList = friends.requests;
                friendsFragment.createRequestsAdapter(this, friendsList);
                friendsFragment.setScrollingPositions(this, old_friends_size !=
                        friends.getFriends().size());
            } else if (message == HandlerMessages.OVKAPI_GROUPS_GET) {
                groups.parse(data.getString("response"), downloadManager,
                        global_prefs.getString("photos_quality", ""), true, true);
                ArrayList<Group> groupsList = groups.getList();
                if (selectedFragment == groupsFragment) {
                    groupsFragment.createAdapter(this, groups.getList(), "groups_list");
                }
            } else if (message == HandlerMessages.OVKAPI_WALL_GET) {
                wall.parse(this, downloadManager, "high", data.getString("response"));
                profileFragment.createWallAdapter(this, wall.getWallItems());
            } else if (message == HandlerMessages.OVKAPI_OVK_ABOUTINSTANCE) {
                mainSettingsFragment.getInstanceInfo("stats", data.getString("response"));
            } else if (message == HandlerMessages.OVKAPI_OVK_CHECK_HTTP || message == HandlerMessages.OVKAPI_OVK_CHECK_HTTPS) {
                mainSettingsFragment.getInstanceInfo("checkHTTP", data.getString("response"));
                Ovk ovk = new Ovk();
                ovk.getVersion(ovk_api);
                ovk.aboutInstance(ovk_api);
            } else if (message == HandlerMessages.OVKAPI_OVK_VERSION) {
                mainSettingsFragment.getInstanceInfo("instanceVersion", data.getString("response"));
            } else if(message == HandlerMessages.DLM_CONVERSATIONS_AVATARS) {
                messagesFragment.loadAvatars(conversations);
            } else if(message == HandlerMessages.DLM_NEWSFEED_AVATARS
                    || message == HandlerMessages.DLM_NEWSFEED_ATTACHMENTS
                    || message == HandlerMessages.DLM_WALL_AVATARS
                    || message == HandlerMessages.DLM_WALL_ATTACHMENTS) {
                if(selectedFragment == newsfeedFragment) {
                    if(newsfeedFragment.newsfeedAdapter != null) {
                        if (message == HandlerMessages.DLM_NEWSFEED_AVATARS) {
                            newsfeedFragment.newsfeedAdapter.setAvatarLoadState(true);
                        } else {
                            newsfeedFragment.newsfeedAdapter.setPhotoLoadState(true);
                            newsfeedFragment.disableLoadState();
                        }
                        newsfeedFragment.refreshAdapter();
                    }
                } else if(selectedFragment == profileFragment) {
                    if(profileFragment.getWallAdapter() == null) {
                        profileFragment.createWallAdapter(this, wall.getWallItems());
                    }
                    if (message == HandlerMessages.DLM_WALL_AVATARS) {
                        profileFragment.getWallAdapter().setAvatarLoadState(true);
                    } else {
                        profileFragment.getWallAdapter().setPhotoLoadState(true);
                    }
                    profileFragment.refreshWallAdapter();
                }
            } else if(message == HandlerMessages.DLM_ACCOUNT_AVATAR) {
                Glide.with(this).load(
                                String.format("%s/photos_cache/account_avatar/avatar_%s",
                                        getCacheDir().getAbsolutePath(), account.user.id))
                        .into((ImageView) ((NavigationView) findViewById(R.id.nav_view))
                                .getHeaderView(0).findViewById(R.id.profile_avatar));
            } else if (message == HandlerMessages.DLM_FRIEND_AVATARS) {
                friendsFragment.refreshAdapter();
            } else if (message == HandlerMessages.OVKAPI_FRIENDS_ADD) {
                friends.requests.remove(friendsFragment.requests_cursor_index);
                friendsFragment.createRequestsAdapter(this, friends.requests);
            } else if(message == HandlerMessages.OVKAPI_FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = 0;
                }
                profileFragment.setFriendStatus(account.user, user.friends_status);
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
            if(selectedFragment == newsfeedFragment)
                account.addQueue("Newsfeed.get", "count=25&extended=1");
            else if(selectedFragment == friendsFragment)
                account.addQueue("Friends.get", "count=25&extended=1");
            else if(selectedFragment == profileFragment)
                account.addQueue("Wall.get", String.format("owner_id=%s&count=50",
                        account.id));
            account.getProfileInfo(ovk_api);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshNewsfeed(boolean progress) {
        if(newsfeed.getWallPosts() != null) {
            int pos = ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).getSelectedItemPosition();
            if(newsfeed != null) {
                newsfeed.getWallPosts().clear();
                newsfeedFragment.newsfeedAdapter.notifyDataSetChanged();
            }
            if (account != null) {
                assert newsfeed != null;
                if (pos == 0) {
                    newsfeed.get(ovk_api, 25);
                } else {
                    newsfeed.getGlobal(ovk_api, 25);
                }
                if (progress) {
                    newsfeedFragment.showProgress();
                }
            }
        }
    }

    public void refreshFriendsList(boolean progress) {
        friends.get(ovk_api, account.id, 25, "friends_list");
        if(user.id == account.id) {
            friends.getRequests(ovk_api);
        }
        if (progress) {
            friendsFragment.showProgress();
        }
    }

    public void refreshGroupsList(boolean progress) {
        groups.getGroups(ovk_api, account.id, 25);
        if (progress) {
            groupsFragment.showProgress();
        }
    }

    public void refreshConversations(boolean progress) {
        messages.getConversations(ovk_api);
        if (progress) {
            messagesFragment.showProgress();
        }
    }

    public void refreshMyWall(boolean progress) {
        wall.get(ovk_api, account.user.id, 50);
        if (progress) {
            profileFragment.showProgress();
        }
    }

    public void switchFragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        ft = getSupportFragmentManager().beginTransaction();
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("newsfeed")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("messages")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("profile")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("settings")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("video_settings")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("personalization")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("about_app")));
        BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
        NavigationView navView = findViewById(R.id.nav_view);
        switch (tag) {
            case "newsfeed":
                switchNavItem(b_navView.getMenu().getItem(0));
                break;
            case "settings":
                switchNavItem(navView.getMenu().getItem(5));
                ft.hide(selectedFragment);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("settings"));
                ft.show(selectedFragment);
                ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.nav_settings);
                ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .setNavigationIcon(R.drawable.ic_arrow_back);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                break;
            case "video_settings":
                switchNavItem(navView.getMenu().getItem(5));
                ft.hide(selectedFragment);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("video_settings"));
                ft.show(selectedFragment);
                ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_video);
                ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .setNavigationIcon(R.drawable.ic_arrow_back);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                break;
            case "personalization":
                switchNavItem(navView.getMenu().getItem(5));
                ft.hide(selectedFragment);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("personalization"));
                ft.show(selectedFragment);
                ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_personalization);
                ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .setNavigationIcon(R.drawable.ic_arrow_back);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                break;
            case "about_app":
                switchNavItem(navView.getMenu().getItem(5));
                ft.hide(selectedFragment);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("about_app"));
                ft.show(selectedFragment);
                ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_about_app);
                ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .setNavigationIcon(R.drawable.ic_arrow_back);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                break;
        }
        ft = getSupportFragmentManager().beginTransaction();
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if(selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("about_app"))
                || selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("personalization"))
                || selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("video_settings"))) {
            switchFragment("settings");
        } else if(selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("newsfeed"))) {
            finishAffinity();
            System.exit(0);
        } else {
            switchFragment("newsfeed");
        }
    }

    public void addLike(int position, String post, View view) {
        WallPost item;
        if (selectedFragment == profileFragment) {
            item = wall.getWallItems().get(position);
            profileFragment.wallSelect(position, "likes", "add");
        } else {
            item = newsfeed.getWallPosts().get(position);
            newsfeedFragment.select(position, "likes", "add");
        }
        likes.add(ovk_api, item.owner_id, item.post_id, position);
    }

    public void deleteLike(int position, String post, View view) {
        WallPost item;
        if (selectedFragment == profileFragment) {
            item = wall.getWallItems().get(position);
            profileFragment.wallSelect(0, "likes", "delete");
        } else {
            item = newsfeed.getWallPosts().get(position);
            newsfeedFragment.select(0, "likes", "delete");
        }
        likes.delete(ovk_api, item.owner_id, item.post_id, position);
    }

    public void setAvatarShape() {
        @SuppressLint("CutPasteId") ShapeableImageView avatar =
                ((ShapeableImageView) ((NavigationView) findViewById(R.id.nav_view))
                        .getHeaderView(0).findViewById(R.id.profile_avatar));
        Global.setAvatarShape(this, avatar);
    }

    public void openConversation(int position) {
        Conversation conversation = conversations.get(position);
        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
        try {
            intent.putExtra("peer_id", conversation.peer_id);
            intent.putExtra("conv_title", conversation.title);
            intent.putExtra("online", conversation.online);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openProfileFromWall(int position) {
        if(selectedFragment == newsfeedFragment) {
            WallPost post = newsfeed.getWallPosts().get(position);
            String url = "";
            if(post.author_id > 0) {
                url = String.format("openvk://profile/id%s", post.author_id);
            } else if(post.author_id < 0) {
                url = String.format("openvk://group/club%s", post.author_id);
            }
            if(url.length() > 0) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                final PackageManager pm = getPackageManager();
                @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo>
                        activityList = pm.queryIntentActivities(i, 0);
                for (int index = 0; index < activityList.size(); index++) {
                    ResolveInfo app = activityList.get(index);
                    if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                        i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                    }
                }
                startActivity(i);
            }
        } else {
            WallPost post = wall.getWallItems().get(position);
            String url = "";
            if(post.author_id > 0) {
                url = String.format("openvk://profile/id%s", post.author_id);
            } else if(post.author_id < 0) {
                url = String.format("openvk://group/club%s", post.author_id);
            }
            if(url.length() > 0) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }
    }

    public void openProfileFromFriends(int position, boolean isRequest) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        String url = "";
        Friend friend;
        if(isRequest) {
            friend = friends.requests.get(position);
        } else {
            friend = friends.getFriends().get(position);
        }
        url = String.format("openvk://profile/id%s", friend.id);
        if(url.length() > 0) {
            i.setData(Uri.parse(url));
            final PackageManager pm = getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo>
                    activityList = pm.queryIntentActivities(i, 0);
            for (int index = 0; index < activityList.size(); index++) {
                ResolveInfo app = activityList.get(index);
                if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                    i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                }
            }
            startActivity(i);
        }
    }

    public void openCommunityPage(int position) {
        Group group = groups.getList().get(position);
        String url = "";
        url = String.format("openvk://group/club%s", group.id);
        if(url.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            final PackageManager pm = getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo>
                    activityList = pm.queryIntentActivities(i, 0);
            for (int index = 0; index < activityList.size(); index++) {
                ResolveInfo app = activityList.get(index);
                if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                    i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                }
            }
            startActivity(i);
        }
    }

    private final ServiceConnection lpConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            LongPollService.LongPollBinder binder = (LongPollService.LongPollBinder) service;
            longPollService = binder.getService();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppActivity.this.startForegroundService(longPollIntent);
                Notification notification = notifMan.createServiceNotification(AppActivity.this);
                longPollService.startForeground(180, notification);
            }
            longPollService.run(AppActivity.this, instance_prefs.getString("server", ""),
                    longPollServer.address, longPollServer.key, longPollServer.ts, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppActivity.this.unbindService(this);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            longPollService = null;
        }
    };

    void doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(lpConnection);
            mShouldUnbind = false;
        }
    }

    @Override
    public void recreate() {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        screenOrientation = newConfig.orientation;
        float dp = getResources().getDisplayMetrics().density;

        BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
        Configuration oldConfig = getResources().getConfiguration();
        // Layout adaptation under configurations
        if(oldConfig.smallestScreenWidthDp != newConfig.smallestScreenWidthDp) {
            restart();
        } else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                !OvkApplication.isTablet) {
            b_navView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);
            navBarHeight = (int) (38 * (dp));
            b_navView.getLayoutParams().height = navBarHeight;
        } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT &&
                !OvkApplication.isTablet) {
            b_navView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
            b_navView.getLayoutParams().height = DrawerLayout.LayoutParams.WRAP_CONTENT;
            b_navView.post(new Runnable() {
                @Override
                public void run() {
                    navBarHeight = b_navView.getMeasuredHeight();
                }
            });
        } else {
            restart();
        }
    }


    @Override
    public void onMonetColorsChanged(@NonNull MonetCompat monet, @NonNull ColorScheme monetColors,
                                     boolean isInitialChange) {
        try {
            super.onMonetColorsChanged(monet, monetColors, isInitialChange);
            getMonet().updateMonetColors();
            setMonetTheme();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(longPollIntent != null) {
            stopService(longPollIntent);
        }
        doUnbindService();
    }

    public void loadMoreFriends() {
        if(friends != null) {
            friends.get(ovk_api, account.id, 25, friends.offset);
        }
    }

    public void loadMoreNews() {
        if(newsfeed != null) {
            int pos = ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                    .findViewById(R.id.spinner)).getSelectedItemPosition();
            if(pos == 0) {
                newsfeed.get(ovk_api, 25, newsfeed.next_from);
            } else {
                newsfeed.getGlobal(ovk_api, 25, newsfeed.next_from);
            }
        }
    }

    public void addToFriends(int user_id) {
        if(user_id != account.id) {
            friends.add(ovk_api, user_id);
        }
    }
}
