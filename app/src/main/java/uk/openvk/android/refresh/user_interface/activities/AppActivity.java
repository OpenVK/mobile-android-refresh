package uk.openvk.android.refresh.user_interface.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
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

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
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
import uk.openvk.android.refresh.api.models.Friend;
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
import uk.openvk.android.refresh.user_interface.list_adapters.NewsfeedToolbarSpinnerAdapter;
import uk.openvk.android.refresh.user_interface.list_items.ToolbarSpinnerItem;

public class AppActivity extends MonetCompatActivity {
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
    private MenuItem prevBottomMenuItem;
    private FriendsFragment friendsFragment;
    private String current_fragment;
    private MonetCompat monet;
    private boolean isDarkTheme;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Global.setInterfaceFont(this);
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"));
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                current_fragment = extras.getString("current_fragment");
            }
        } else {
            current_fragment = savedInstanceState.getString("current_fragment");
        }
        setContentView(R.layout.app);
        instance_prefs = getSharedPreferences("instance", 0);
        createFragments();
    }

    private void createFragments() {
        newsfeedFragment = new NewsfeedFragment();
        friendsFragment = new FriendsFragment();
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
        setMonetTheme();
        setFloatingActionButton();
        if (newsfeedFragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_screen, newsfeedFragment, "newsfeed");
            ft.add(R.id.fragment_screen, friendsFragment, "friends");
            ft.add(R.id.fragment_screen, messagesFragment, "messages");
            ft.add(R.id.fragment_screen, profileFragment, "profile");
            ft.add(R.id.fragment_screen, mainSettingsFragment, "settings");
            ft.add(R.id.fragment_screen, personalizationFragment, "personalization");
            ft.add(R.id.fragment_screen, aboutAppFragment, "about_app");
            ft.commit();
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(friendsFragment);
            ft.hide(messagesFragment);
            ft.hide(profileFragment);
            ft.hide(mainSettingsFragment);
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
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.nav_settings);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setNavigationIcon(R.drawable.ic_menu);
            NavigationView navView = findViewById(R.id.nav_view);
            navView.getMenu().getItem(4).setChecked(true);
            prevMenuItem = navView.getMenu().getItem(4);
            navView.getMenu().getItem(1).setChecked(false);
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        }
    }

    private void setMonetTheme() {
        if(Global.checkMonet(this)) {
            int colorOnSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
            NavigationView navView = findViewById(R.id.nav_view);
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_checked}, new int[] { }
            };
            int[] colors;
            colors = new int[]{
                    getMonet().getAccentColor(this, isDarkTheme),
                    Global.adjustAlpha(colorOnSurface, 0.6f)
            };
            ColorStateList csl = new ColorStateList(states, colors);
            navView.setItemIconTintList(csl);
            navView.setItemTextColor(csl);

            BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
            colors = new int[]{
                    getMonet().getAccentColor(this, isDarkTheme),
                    Global.adjustAlpha(colorOnSurface, 0.6f)
            };
            csl = new ColorStateList(states, colors);
            b_navView.setItemTextColor(csl);
            b_navView.setItemIconTintList(csl);
            b_navView.setItemRippleColor(ColorStateList.valueOf(getMonet().getPrimaryColor(this, isDarkTheme)));
            FloatingActionButton fab = findViewById(R.id.fab_newpost);

            fab.setBackgroundTintList(ColorStateList.valueOf(getMonet().getBackgroundColor(this, isDarkTheme)));
            fab.setImageTintList(ColorStateList.valueOf(getMonet().getAccentColor(this, isDarkTheme)));
            fab.setRippleColor(ColorStateList.valueOf(getMonet().getPrimaryColor(this, isDarkTheme)));
        }
    }

    public void restart() {
        Intent intent = new Intent(this, AppActivity.class);
        if(selectedFragment != null) {
            if (selectedFragment == personalizationFragment) {
                intent.putExtra("current_fragment", "personalization");
            }
        }
        startActivity(intent);
        finishActivity(1);
    }

    private void setAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        tbSpinnerItems = new ArrayList<>();
        tbSpinnerItems.add(new ToolbarSpinnerItem(getResources().getString(R.string.nav_news), getResources().getString(R.string.my_news_item), 60));
        tbSpinnerItems.add(new ToolbarSpinnerItem(getResources().getString(R.string.nav_news), getResources().getString(R.string.all_news_item), 61));
        tbSpinnerAdapter = new NewsfeedToolbarSpinnerAdapter(this, tbSpinnerItems);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setAdapter(tbSpinnerAdapter);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setVisibility(View.VISIBLE);
        ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshNewsfeed(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFragment == mainSettingsFragment || selectedFragment == personalizationFragment) {
                    onBackPressed();
                } else {
                    drawer.open();
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
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_screen);
            NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
            @SuppressLint("CutPasteId") AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).setDrawerLayout(
                    ((DrawerLayout) findViewById(R.id.drawer_layout))).build();
            NavigationUI.setupWithNavController(navView, navController);
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            toggle = new ActionBarDrawerToggle(this, drawer, android.R.string.ok, android.R.string.cancel);
            drawer.addDrawerListener(toggle);
            TypedValue typedValue = new TypedValue();
            boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkThemeEnabled) {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.background, typedValue, true);
            } else {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
            }
            drawer.setStatusBarBackgroundColor(typedValue.data);
            toggle.syncState();
        } catch (Exception ex) {
            System.exit(0);
        }
    }

    public void setNavView() {
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
                    switchNavItem(item);
                    return false;
                }
            });
        }
        for(int i = 0; i < navView.getMenu().size(); i++) {
            navView.getMenu().getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                    drawer.closeDrawers();
                    switchNavItem(item);
                    return false;
                }
            });
        }
        if(account == null || account.user == null) {
            String profile_name = getResources().getString(R.string.loading);
            ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_name)).setText(profile_name);
            ((TextView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.screen_name))
                    .setVisibility(View.GONE);
        }
        @SuppressLint("CutPasteId") ShapeableImageView avatar = ((ShapeableImageView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_avatar));
        Global.setAvatarShape(this, avatar);
        ((FloatingActionButton) findViewById(R.id.fab_newpost)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
                profileFragment.setData(account.user);
                toolbar.setTitle(R.string.nav_friends);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (friends.getFriends() == null || friends.getFriends().size() == 0) {
                    friends.get(ovk_api, account.id, 25, "friends_list");
                }
                b_navView.getMenu().getItem(1).setChecked(true);
                navView.getMenu().getItem(2).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.messages) {
                prevBottomMenuItem = b_navView.getMenu().getItem(2);
                prevMenuItem = navView.getMenu().getItem(3);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("messages"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.GONE);
                profileFragment.setData(account.user);
                toolbar.setTitle(R.string.nav_messages);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                if (conversations == null) {
                    messages.getConversations(ovk_api);
                }
                ((MessagesFragment) selectedFragment).refreshAdapter();
                b_navView.getMenu().getItem(2).setChecked(true);
                navView.getMenu().getItem(3).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.profile) {
                prevBottomMenuItem = b_navView.getMenu().getItem(3);
                prevMenuItem = navView.getMenu().getItem(0);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("profile"));
                ((AppCompatSpinner) toolbar.findViewById(R.id.spinner)).setVisibility(View.GONE);
                profileFragment.setData(account.user);
                if (wall.getWallItems() == null) {
                    wall.get(ovk_api, account.user.id, 50);
                }
                toolbar.setTitle(R.string.nav_profile);
                toolbar.setNavigationIcon(R.drawable.ic_menu);
                b_navView.getMenu().getItem(3).setChecked(true);
                navView.getMenu().getItem(0).setChecked(true);
                findViewById(R.id.fab_newpost).setVisibility(View.GONE);
            } else if (itemId == R.id.settings) {
                ft.hide(selectedFragment);
                selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("settings"));
                ((AppCompatSpinner) ((MaterialToolbar) findViewById(R.id.app_toolbar))
                        .findViewById(R.id.spinner)).setVisibility(View.GONE);
                toolbar.setTitle(R.string.nav_settings);
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                navView.getMenu().getItem(4).setChecked(true);
                prevMenuItem = navView.getMenu().getItem(4);
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

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                account.parse(data.getString("response"), ovk_api);
                newsfeed.get(ovk_api, 25);
                users.getAccountUser(ovk_api, account.id);
            } else if (message == HandlerMessages.NEWSFEED_GET) {
                newsfeed.parse(this, downloadManager, data.getString("response"), "high", true);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.disableUpdateState();
                if(selectedFragment == newsfeedFragment) {
                    findViewById(R.id.fab_newpost).setVisibility(View.VISIBLE);
                }
            } else if (message == HandlerMessages.NEWSFEED_GET_GLOBAL) {
                newsfeed.parse(this, downloadManager, data.getString("response"), "high", true);
                newsfeedFragment.createAdapter(this, newsfeed.getWallPosts());
                newsfeedFragment.disableUpdateState();
            } else if(message == HandlerMessages.LIKES_ADD) {
                likes.parse(data.getString("response"));
                if (global_prefs.getString("current_screen", "").equals("newsfeed")) {
                    newsfeedFragment.select(likes.position, "likes", 1);
                } else if (global_prefs.getString("current_screen", "").equals("profile")) {
                    //((WallLayout) profileLayout.findViewById(R.id.wall_layout)).select(likes.position, "likes", 1);
                }
            } else if(message == HandlerMessages.LIKES_DELETE) {
                likes.parse(data.getString("response"));
                if (selectedFragment == newsfeedFragment) {
                    newsfeedFragment.select(likes.position, "likes", 0);
                } else if (selectedFragment == profileFragment) {
                    newsfeedFragment.select(likes.position, "likes", 0);
                }
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
                messagesFragment.disableUpdateState();
            } else if (message == HandlerMessages.FRIENDS_GET) {
                friends.parse(data.getString("response"), downloadManager, true, true);
                ArrayList<Friend> friendsList = friends.getFriends();
                friendsFragment.createAdapter(this, friendsList, "friends");
                friendsFragment.disableUpdateState();
            } else if (message == HandlerMessages.WALL_GET) {
                wall.parse(this, downloadManager, "high", data.getString("response"));
                profileFragment.createWallAdapter(this, wall.getWallItems());
            } else if(message == HandlerMessages.CONVERSATIONS_AVATARS) {
                messagesFragment.loadAvatars(conversations);
            } else if(message == HandlerMessages.NEWSFEED_AVATARS || message == HandlerMessages.NEWSFEED_ATTACHMENTS
                    || message == HandlerMessages.WALL_AVATARS || message == HandlerMessages.WALL_ATTACHMENTS) {
                if(selectedFragment == newsfeedFragment) {
                    if(message == HandlerMessages.NEWSFEED_AVATARS) {
                        newsfeedFragment.newsfeedAdapter.setAvatarLoadState(true);
                    } else {
                        newsfeedFragment.newsfeedAdapter.setPhotoLoadState(true);
                    }
                } else if(selectedFragment == profileFragment) {
                    if(message == HandlerMessages.WALL_AVATARS) {
                        profileFragment.wallAdapter.setAvatarLoadState(true);
                    } else {
                        profileFragment.wallAdapter.setPhotoLoadState(true);
                    }
                }
            } else if(message == HandlerMessages.ACCOUNT_AVATAR) {
                Glide.with(this).load(
                                String.format("%s/photos_cache/account_avatar/avatar_%s",
                                        getCacheDir().getAbsolutePath(), account.user.id))
                        .into((ImageView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_avatar));
            } else if (message == HandlerMessages.FRIEND_AVATARS) {
                friendsFragment.refreshAdapter();
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
                account.addQueue("Wall.get", String.format("owner_id=%s&count=50", account.id));
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

    public void refreshFriendsList(boolean progress) {
        friends.get(ovk_api, account.id, 25, "friends_list");
        if (progress) {
            friendsFragment.showProgress();
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
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("personalization")));
        ft.hide(Objects.requireNonNull(fm.findFragmentByTag("about_app")));
        BottomNavigationView b_navView = findViewById(R.id.bottom_nav_view);
        NavigationView navView = findViewById(R.id.nav_view);
        if(tag.equals("newsfeed")) {
            switchNavItem(b_navView.getMenu().getItem(0));
        } else if(tag.equals("personalization")) {
            switchNavItem(navView.getMenu().getItem(4));
            ft.hide(selectedFragment);
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("personalization"));
            ft.show(selectedFragment);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_personalization);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setNavigationIcon(R.drawable.ic_arrow_back);
            findViewById(R.id.fab_newpost).setVisibility(View.GONE);
        } else if(tag.equals("about_app")) {
            switchNavItem(navView.getMenu().getItem(4));
            ft.hide(selectedFragment);
            selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("about_app"));
            ft.show(selectedFragment);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setTitle(R.string.pref_about_app);
            ((MaterialToolbar) findViewById(R.id.app_toolbar)).setNavigationIcon(R.drawable.ic_arrow_back);
            findViewById(R.id.fab_newpost).setVisibility(View.GONE);
        }
        ft = getSupportFragmentManager().beginTransaction();
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if(selectedFragment != Objects.requireNonNull(fm.findFragmentByTag("newsfeed"))) {
            switchFragment("newsfeed");
        } else {
            finishAffinity();
            System.exit(0);
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
        @SuppressLint("CutPasteId") ShapeableImageView avatar = ((ShapeableImageView) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0).findViewById(R.id.profile_avatar));
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

    public void openProfileFromFriends(int position) {
        Friend friend = friends.getFriends().get(position);
        String url = "";
        url = String.format("openvk://profile/id%s", friend.id);
        if(url.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }
}
