package uk.openvk.android.refresh.ui.core.activities;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.kieronquinn.monetcompat.core.MonetCompat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Conversation;
import uk.openvk.android.refresh.api.entities.Friend;
import uk.openvk.android.refresh.api.entities.Group;
import uk.openvk.android.refresh.api.entities.LongPollServer;
import uk.openvk.android.refresh.api.entities.User;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.JSONParser;
import uk.openvk.android.refresh.api.wrappers.NotificationManager;
import uk.openvk.android.refresh.services.LongPollService;
import uk.openvk.android.refresh.ui.FragmentNavigator;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.enumerations.PublicPageCounters;
import uk.openvk.android.refresh.ui.core.fragments.app.AboutApplicationFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.GroupsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.MessagesFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.MainSettingsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.PersonalizationFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.VideoSettingsFragment;
import uk.openvk.android.refresh.ui.list.adapters.NewsfeedToolbarSpinnerAdapter;
import uk.openvk.android.refresh.ui.list.items.ToolbarSpinnerItem;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class AppActivity extends BaseNetworkActivity {
    public Handler handler;
    public Menu activity_menu;
    public int old_friends_size;
    private DownloadManager downloadManager;
    public NewsfeedFragment newsfeedFragment;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    public ProfileFragment profileFragment;
    public FragmentTransaction ft;
    private ArrayList<ToolbarSpinnerItem> tbSpinnerItems;
    private NewsfeedToolbarSpinnerAdapter tbSpinnerAdapter;
    public MessagesFragment messagesFragment;
    private MenuItem prevMenuItem;
    public MainSettingsFragment mainSettingsFragment;
    private PersonalizationFragment personalizationFragment;
    private AboutApplicationFragment aboutAppFragment;
    public Fragment selectedFragment;
    private MenuItem prevBottomMenuItem;
    public FriendsFragment friendsFragment;
    private String current_fragment;
    private boolean isDarkTheme;
    public GroupsFragment groupsFragment;
    private boolean mShouldUnbind;
    private LongPollService longPollService;
    public LongPollServer longPollServer;
    private Intent longPollIntent;
    private VideoSettingsFragment videoSettingsFragment;
    private NotificationManager notifMan;
    private int screenOrientation;
    private int navBarHeight;
    public FragmentNavigator fn;
    private User user;
    public ArrayList<Conversation> conversations;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
        }
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
        fn = new FragmentNavigator(this);

        setNavView();
        setAPIWrapper();
        setNavDrawer();
        setAppBar();
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
            findViewById(R.id.app_toolbar)
                    .findViewById(R.id.spinner).setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(selectedFragment != null) {
            if(selectedFragment instanceof ProfileFragment) {
                getMenuInflater().inflate(R.menu.profile, menu);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if(item.getItemId() == R.id.copy_link) {
            if(selectedFragment instanceof ProfileFragment) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                String url = "";
                if(ovk_api.account.user.screen_name != null && !ovk_api.account.user.screen_name.isEmpty()) {
                    url = String.format("https://%s/%s",
                            instance_prefs.getString("server", ""),
                            ovk_api.account.user.screen_name);
                } else {
                    url = String.format("https://%s/id%s",
                            instance_prefs.getString("server", ""),
                            ovk_api.account.user.id);
                }
                android.content.ClipData clip =
                        android.content.ClipData.newPlainText("Profile URL", url);
                clipboard.setPrimaryClip(clip);
            }
        } else if(item.getItemId() == R.id.open_in_browser) {
            String url = "";
            if(ovk_api.account.user.screen_name != null && !ovk_api.account.user.screen_name.isEmpty()) {
                url = String.format("https://%s/%s",
                        instance_prefs.getString("server", ""), ovk_api.account.user.screen_name);
            } else {
                url = String.format("https://%s/id%s",
                        instance_prefs.getString("server", ""), ovk_api.account.user.id);
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
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
        toolbar.findViewById(R.id.spinner).setVisibility(VISIBLE);
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
                        if (navView.getVisibility() == VISIBLE) {
                            navView.setVisibility(View.GONE);
                        } else {
                            navView.setVisibility(VISIBLE);
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
                    intent.putExtra("owner_id", ovk_api.account.user.id);
                    intent.putExtra("account_id", ovk_api.account.id);
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
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
                            findViewById(R.id.drawer_layout)).build();
            NavigationUI.setupWithNavController(navView, navController);
            drawer = findViewById(R.id.drawer_layout);
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
        b_navView.setOnItemSelectedListener(item -> true);
        for(int i = 0; i < b_navView.getMenu().size(); i++) {
            b_navView.getMenu().getItem(i).setOnMenuItemClickListener(item -> {
                        switchNavItem(item);
                        return false;
                    });
        }
        for(int i = 0; i < navView.getMenu().size(); i++) {
            navView.getMenu().getItem(i).setOnMenuItemClickListener(item -> {
                        try {
                            drawer.closeDrawers();
                            switchNavItem(item);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return false;
                    });
        }
        ConstraintLayout header = (ConstraintLayout) navView.getHeaderView(0);
        header.findViewById(R.id.search_btn).setOnClickListener(v -> startQuickSearchActivity());
        String profile_name = getResources().getString(R.string.loading);
        ((TextView) header.findViewById(R.id.profile_name)).setText(profile_name);
        header.findViewById(R.id.screen_name).setVisibility(View.GONE);
        @SuppressLint("CutPasteId") ShapeableImageView avatar = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                .findViewById(R.id.profile_avatar);
        Global.setAvatarShape(this, avatar);
        findViewById(R.id.fab_newpost).setOnClickListener(v -> {});
        ((MaterialToolbar) findViewById(R.id.app_toolbar)).setOnMenuItemClickListener(item -> {
            onOptionsItemSelected(item);
            return true;
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

    public void switchNavItem(MenuItem item) {
        try {
            int itemId = item.getItemId();
            BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
            NavigationView navView = findViewById(R.id.nav_view);
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            ft = getSupportFragmentManager().beginTransaction();
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
                fn.navigateTo(selectedFragment, "newsfeed");
                findViewById(R.id.app_toolbar)
                        .findViewById(R.id.spinner).setVisibility(VISIBLE);
                ((NewsfeedFragment) selectedFragment).refreshAdapter();
                setToolbarTitle("", "");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                b_navView.getMenu().getItem(0).setChecked(true);
                navView.getMenu().getItem(1).setChecked(true);
                if (ovk_api.newsfeed.getWallPosts() != null) {
                    findViewById(R.id.fab_newpost).setVisibility(VISIBLE);
                }
            } else if (itemId == R.id.newsfeed) {
                prevBottomMenuItem = b_navView.getMenu().getItem(0);
                prevMenuItem = navView.getMenu().getItem(1);
                fn.navigateTo(selectedFragment, "newsfeed");
                findViewById(R.id.app_toolbar)
                        .findViewById(R.id.spinner).setVisibility(VISIBLE);
                setToolbarTitle("", "");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                ((NewsfeedFragment) selectedFragment).refreshAdapter();
                b_navView.getMenu().getItem(0).setChecked(true);
                navView.getMenu().getItem(1).setChecked(true);
                if (ovk_api.newsfeed.getWallPosts() != null) {
                    findViewById(R.id.fab_newpost).setVisibility(VISIBLE);
                }
            } else if (itemId == R.id.friends) {
                prevBottomMenuItem = b_navView.getMenu().getItem(1);
                prevMenuItem = navView.getMenu().getItem(2);
                fn.navigateTo(selectedFragment, "friends");
                findViewById(R.id.app_toolbar)
                        .findViewById(R.id.spinner).setVisibility(View.GONE);
                profileFragment.setData(ovk_api.account.user, ovk_api.friends,
                        ovk_api.account, ovk_api.wrapper);
                setToolbarTitle(getResources().getString(R.string.nav_friends), "");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (friendsFragment.getFriendsCount() == 0) {
                    ovk_api.friends.get(ovk_api.wrapper,
                            ovk_api.account.id, 25, "friends_list");
                }
                b_navView.getMenu().getItem(1).setChecked(true);
                navView.getMenu().getItem(2).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.groups) {
                prevBottomMenuItem = b_navView.getMenu().getItem(1);
                prevMenuItem = navView.getMenu().getItem(3);
                fn.navigateTo(selectedFragment, "groups");
                findViewById(R.id.app_toolbar)
                        .findViewById(R.id.spinner).setVisibility(View.GONE);
                profileFragment.setData(ovk_api.account.user,
                        ovk_api.friends, ovk_api.account, ovk_api.wrapper);
                setToolbarTitle(getResources().getString(R.string.nav_groups), "");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (ovk_api.groups.getList() == null || ovk_api.groups.getList().isEmpty()) {
                    ovk_api.groups.getGroups(ovk_api.wrapper, ovk_api.account.id, 25);
                }
                b_navView.getMenu().getItem(3).setChecked(true);
                navView.getMenu().getItem(3).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.messages) {
                prevBottomMenuItem = b_navView.getMenu().getItem(2);
                prevMenuItem = navView.getMenu().getItem(4);
                fn.navigateTo(selectedFragment, "messages");
                findViewById(R.id.app_toolbar)
                        .findViewById(R.id.spinner).setVisibility(View.GONE);
                profileFragment.setData(ovk_api.account.user, ovk_api.friends,
                        ovk_api.account, ovk_api.wrapper);
                setToolbarTitle(getResources().getString(R.string.nav_messages), "");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (conversations == null) {
                    ovk_api.messages.getConversations(ovk_api.wrapper);
                }
                ((MessagesFragment) selectedFragment).refreshAdapter();
                b_navView.getMenu().getItem(2).setChecked(true);
                navView.getMenu().getItem(4).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.profile) {
                prevBottomMenuItem = b_navView.getMenu().getItem(4);
                prevMenuItem = navView.getMenu().getItem(0);
                fn.navigateTo(selectedFragment, "profile");
                toolbar.findViewById(R.id.spinner).setVisibility(View.GONE);
                profileFragment.setData(ovk_api.account.user, ovk_api.friends,
                        ovk_api.account, ovk_api.wrapper);
                profileFragment.header.setCountersVisibility(PublicPageCounters.MEMBERS, false);
                profileFragment.header.setCountersVisibility(PublicPageCounters.FRIENDS, true);
                if (ovk_api.wall.getWallItems() == null) {
                    ovk_api.wall.get(ovk_api.wrapper, ovk_api.account.user.id, 50);
                }
                setToolbarTitle(getResources().getString(R.string.nav_profile), "");
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                b_navView.getMenu().getItem(4).setChecked(true);
                navView.getMenu().getItem(0).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                if(ovk_api.wall.getWallItems() != null) profileFragment.recreateWallAdapter();
                if(profileFragment.aboutItems != null) profileFragment.recreateAboutAdapter();
            } else if (itemId == R.id.settings) {
                fn.navigateTo(selectedFragment, "settings");
                findViewById(R.id.app_toolbar)
                        .findViewById(R.id.spinner).setVisibility(View.GONE);
                setToolbarTitle(getResources().getString(R.string.nav_settings), "");
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                navView.getMenu().getItem(5).setChecked(true);
                prevMenuItem = navView.getMenu().getItem(5);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setAPIWrapper() {
        user = new User();
    }

    public Fragment getSelectedFragment() {
        return selectedFragment;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void receiveState(int message, Bundle data) {
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
        // Handling OpenVK API and UI messages
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                ovk_api.newsfeed.get(ovk_api.wrapper, 25);
                ovk_api.users.getAccountUser(ovk_api.wrapper, ovk_api.account.id);
                ovk_api.messages.getLongPollServer(ovk_api.wrapper);
                ovk_api.messages.getConversations(ovk_api.wrapper);
                String profile_name = String.format("%s %s",
                        ovk_api.account.first_name, ovk_api.account.last_name);
                ConstraintLayout header = (ConstraintLayout)
                        ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
                ((TextView) header.findViewById(R.id.profile_name)).setText(profile_name);
            } else if (message == HandlerMessages.ACCOUNT_COUNTERS) {
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
                if(ovk_api.account.counters.friends_requests > 0) {
                    b_navView.getOrCreateBadge(R.id.friends)
                            .setNumber(ovk_api.account.counters.friends_requests);
                    b_navView.getOrCreateBadge(R.id.friends)
                            .setBackgroundColor(accentColor);
                }
                if(ovk_api.account.counters.new_messages > 0) {
                    b_navView.getOrCreateBadge(R.id.messages)
                            .setNumber(ovk_api.account.counters.new_messages);
                    b_navView.getOrCreateBadge(R.id.messages)
                            .setBackgroundColor(accentColor);
                }
            } else if (message == HandlerMessages.NEWSFEED_GET || message == HandlerMessages.NEWSFEED_GET_GLOBAL) {
                newsfeedFragment.createAdapter(this, ovk_api.newsfeed.getWallPosts());
                newsfeedFragment.setScrollingPositions(this, true);
                newsfeedFragment.disableUpdateState();
                findViewById(R.id.fab_newpost).setVisibility(VISIBLE);
                newsfeedFragment.disableLoadState();
            } else if (message == HandlerMessages.NEWSFEED_GET_MORE) {
                newsfeedFragment.createAdapter(this, ovk_api.newsfeed.getWallPosts());
                newsfeedFragment.setScrollingPositions(this, true);
            } else if (message == HandlerMessages.NEWSFEED_GET_MORE_GLOBAL) {
                newsfeedFragment.createAdapter(this, ovk_api.newsfeed.getWallPosts());
                newsfeedFragment.setScrollingPositions(this, true);
            } else if(message == HandlerMessages.LIKES_ADD) {
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedFragment.select(ovk_api.likes.position, "likes", 1);
                } else {
                    global_prefs.getString("current_screen", "");
                    //((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.LIKES_DELETE) {
                if (selectedFragment == newsfeedFragment) {
                    newsfeedFragment.select(ovk_api.likes.position, "likes", 0);
                } else if (selectedFragment == profileFragment) {
                    newsfeedFragment.select(ovk_api.likes.position, "likes", 0);
                }
            } else if (message == HandlerMessages.USERS_GET_ALT) {
                ovk_api.account.user = ovk_api.users.getList().get(0);
                ConstraintLayout header = (ConstraintLayout)
                        ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
                ovk_api.account.user.downloadAvatar(ovk_api.dlman, "high", "account_avatar");
                String profile_name = String.format("%s %s", ovk_api.account.first_name,
                        ovk_api.account.last_name);
                if(ovk_api.account.user.screen_name != null &&
                        !ovk_api.account.user.screen_name.isEmpty()) {
                    ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                            .findViewById(R.id.screen_name))
                            .setText(String.format("@%s", ovk_api.account.user.screen_name));
                    ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                            .findViewById(R.id.screen_name)
                            .setVisibility(VISIBLE);
                } else {
                    ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0)
                            .findViewById(R.id.screen_name)
                            .setVisibility(View.GONE);
                }
                ovk_api.friends.get(ovk_api.wrapper, ovk_api.account.user.id, 25, "profile_counter");
            } else if(message == HandlerMessages.MESSAGES_CONVERSATIONS) {
                messagesFragment.createAdapter(this, conversations, ovk_api.account);
                messagesFragment.disableUpdateState();
            } else if (message == HandlerMessages.MESSAGES_GET_LONGPOLL_SERVER) {
                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                longPollIntent = new Intent(this, LongPollService.class);
                PendingIntent pendingIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getService(this, 0, longPollIntent,
                            PendingIntent.FLAG_MUTABLE);
                } else {
                    pendingIntent = PendingIntent.getService(this, 0, longPollIntent,
                            PendingIntent.FLAG_IMMUTABLE);
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
            } else if (message == HandlerMessages.FRIENDS_GET_ALT) {
                profileFragment.setFriendsCount(ovk_api.friends.count);
            } else if (message == HandlerMessages.FRIENDS_GET) {
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                friendsFragment.disableUpdateState();
                friendsFragment.setScrollingPositions(this,
                        !ovk_api.friends.getFriends().isEmpty());
                ovk_api.friends.getRequests(ovk_api.wrapper);
            } else if (message == HandlerMessages.FRIENDS_GET_MORE) {
                int old_friends_size = ovk_api.friends.getFriends().size();
                ArrayList<Friend> friendsList = ovk_api.friends.getFriends();
                friendsFragment.createFriendsAdapter(this, friendsList);
                friendsFragment.setScrollingPositions(this, old_friends_size !=
                        ovk_api.friends.getFriends().size());
            } else if (message == HandlerMessages.FRIENDS_REQUESTS) {
                int old_friends_size = ovk_api.friends.getFriends().size();
                ArrayList<Friend> friendsList = ovk_api.friends.requests;
                friendsFragment.createRequestsAdapter(this, friendsList);
                friendsFragment.setScrollingPositions(this, old_friends_size !=
                        ovk_api.friends.getFriends().size());
            } else if (message == HandlerMessages.GROUPS_GET) {
                if (selectedFragment == groupsFragment) {
                    groupsFragment.createAdapter(this, ovk_api.groups.getList(),
                            "groups_list");
                }
            } else if (message == HandlerMessages.WALL_GET) {
                profileFragment.createWallAdapter(this, ovk_api.wall.getWallItems());
            } else if (message == HandlerMessages.OVK_ABOUTINSTANCE) {
                mainSettingsFragment.getInstanceInfo("stats", data.getString("response"));
            } else if (message == HandlerMessages.OVK_CHECK_HTTP ||
                    message == HandlerMessages.OVK_CHECK_HTTPS) {
                mainSettingsFragment.getInstanceInfo("checkHTTP", data.getString("response"));
                ovk_api.ovk.getVersion(ovk_api.wrapper);
                ovk_api.ovk.aboutInstance(ovk_api.wrapper);
            } else if (message == HandlerMessages.OVK_VERSION) {
                mainSettingsFragment.getInstanceInfo("instanceVersion", data.getString("response"));
            } else if(message == HandlerMessages.CONVERSATIONS_AVATARS) {
                messagesFragment.loadAvatars(conversations);
            } else if(message == HandlerMessages.NEWSFEED_AVATARS
                    || message == HandlerMessages.NEWSFEED_ATTACHMENTS
                    || message == HandlerMessages.WALL_AVATARS
                    || message == HandlerMessages.WALL_ATTACHMENTS) {
                if(selectedFragment == newsfeedFragment) {
                    if(newsfeedFragment.newsfeedAdapter != null) {
                        if (message == HandlerMessages.NEWSFEED_AVATARS) {
                            newsfeedFragment.loadAvatars();
                        } else {
                            newsfeedFragment.loadPhotos();
                        }
                    }
                } else if(selectedFragment == profileFragment) {
                    if(profileFragment.getWallAdapter() == null) {
                        profileFragment.createWallAdapter(this, ovk_api.wall.getWallItems());
                    }
                    if (message == HandlerMessages.WALL_AVATARS) {
                        //profileFragment.getWallAdapter().setAvatarLoadState(true);
                    } else {
                        //profileFragment.getWallAdapter().setPhotoLoadState(true);
                    }
                    profileFragment.refreshWallAdapter();
                }
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                Glide.with(this).load(
                                String.format("%s/photos_cache/account_avatar/avatar_%s",
                                        getCacheDir().getAbsolutePath(), ovk_api.account.user.id))
                        .into((ImageView) ((NavigationView) findViewById(R.id.nav_view))
                                .getHeaderView(0).findViewById(R.id.profile_avatar));
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsFragment.refreshAdapter();
            } else if (message == HandlerMessages.FRIENDS_ADD) {
                ovk_api.friends.requests.remove(friendsFragment.requests_cursor_index);
                friendsFragment.createRequestsAdapter(this, ovk_api.friends.requests);
            } else if(message == HandlerMessages.FRIENDS_DELETE) {
                JSONObject response = new JSONParser().parseJSON(data.getString("response"));
                int status = response.getInt("response");
                if(status == 1) {
                    user.friends_status = 0;
                }
                profileFragment.setFriendStatus(ovk_api.account.user, user.friends_status);
            } else if(message < 0) {
                newsfeedFragment.setError(true, message, v -> retryConnection());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void retryConnection() {
        if(ovk_api.account != null) {
            newsfeedFragment.setError(false, 0, null);
            ovk_api.newsfeed.get(ovk_api.wrapper, 25);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshNewsfeed(boolean progress) {
        if(ovk_api.newsfeed.getWallPosts() != null) {
            int pos = ((AppCompatSpinner) findViewById(R.id.app_toolbar)
                    .findViewById(R.id.spinner)).getSelectedItemPosition();
            if(ovk_api.newsfeed != null) {
                ovk_api.newsfeed.getWallPosts().clear();
                newsfeedFragment.newsfeedAdapter.notifyDataSetChanged();
            }
            if (ovk_api.account != null) {
                assert ovk_api.newsfeed != null;
                if (pos == 0) {
                    ovk_api.newsfeed.get(ovk_api.wrapper, 25);
                } else {
                    ovk_api.newsfeed.getGlobal(ovk_api.wrapper, 25);
                }
                if (progress) {
                    newsfeedFragment.showProgress();
                }
            }
        }
    }

    public void refreshFriendsList(boolean progress) {
        ovk_api.friends.get(ovk_api.wrapper, ovk_api.account.id, 25, "friends_list");
        if(user.id == ovk_api.account.id) {
            ovk_api.friends.getRequests(ovk_api.wrapper);
        }
        if (progress) {
            friendsFragment.showProgress();
        }
    }

    public void refreshGroupsList(boolean progress) {
        ovk_api.groups.getGroups(ovk_api.wrapper, ovk_api.account.id, 25);
        if (progress) {
            groupsFragment.showProgress();
        }
    }

    public void refreshConversations(boolean progress) {
        ovk_api.messages.getConversations(ovk_api.wrapper);
        if (progress) {
            messagesFragment.showProgress();
        }
    }

    public void refreshMyWall(boolean progress) {
        ovk_api.wall.get(ovk_api.wrapper, ovk_api.account.user.id, 50);
        if (progress) {
            profileFragment.showProgress();
        }
    }



    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if(selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("about_app"))
                || selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("personalization"))
                || selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("video_settings"))) {
            fn.navigateTo("settings");
        } else if(selectedFragment == Objects.requireNonNull(fm.findFragmentByTag("newsfeed"))) {
            finishAffinity();
            System.exit(0);
        } else {
            fn.navigateTo("newsfeed");
        }
    }

    public void setAvatarShape() {
        @SuppressLint("CutPasteId") ShapeableImageView avatar =
                ((NavigationView) findViewById(R.id.nav_view))
                        .getHeaderView(0).findViewById(R.id.profile_avatar);
        Global.setAvatarShape(this, avatar);
    }

    private final ServiceConnection lpConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

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
            b_navView.post(() -> navBarHeight = b_navView.getMeasuredHeight());
        } else {
            restart();
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
        if(ovk_api.friends != null) {
            ovk_api.friends.get(ovk_api.wrapper, ovk_api.account.id, 25, ovk_api.friends.offset);
        }
    }

    public void loadMoreNews() {
        if(ovk_api.newsfeed != null) {
            int pos = ((AppCompatSpinner) findViewById(R.id.app_toolbar)
                    .findViewById(R.id.spinner)).getSelectedItemPosition();
            if(pos == 0) {
                ovk_api.newsfeed.get(ovk_api.wrapper, 25, ovk_api.newsfeed.next_from);
            } else {
                ovk_api.newsfeed.getGlobal(ovk_api.wrapper, 25, ovk_api.newsfeed.next_from);
            }
        }
    }

    public void addToFriends(int user_id) {
        if(user_id != ovk_api.account.id) {
            ovk_api.friends.add(ovk_api.wrapper, user_id);
        }
    }

    public void setToolbarTitle(String title, String subtitle) {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }
}
