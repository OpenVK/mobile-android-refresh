package uk.openvk.android.refresh.ui;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;

public class FragmentNavigator {
    private final Context ctx;

    public FragmentNavigator(Context ctx) {
        this.ctx = ctx;
    }

    public void navigateTo(String where) {
        if(ctx instanceof Activity) {
            if(ctx instanceof AppActivity activity) {
                FragmentManager fm = activity.getSupportFragmentManager();
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.hide(activity.selectedFragment);
                BottomNavigationView b_navView = activity.findViewById(R.id.bottom_nav_view);
                NavigationView navView = activity.findViewById(R.id.nav_view);
                switch (where) {
                    case "newsfeed" -> activity.switchNavItem(b_navView.getMenu().getItem(0));
                    case "settings" -> {
                        activity.switchNavItem(navView.getMenu().getItem(5));
                        activity.selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("settings"));
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar)).setTitle(R.string.nav_settings);
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar))
                                .setNavigationIcon(R.drawable.ic_arrow_back);
                        activity.findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                    }
                    case "video_settings" -> {
                        activity.switchNavItem(navView.getMenu().getItem(5));
                        activity.selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("video_settings"));
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar)).setTitle(R.string.pref_video);
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar))
                                .setNavigationIcon(R.drawable.ic_arrow_back);
                        activity.findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                    }
                    case "personalization" -> {
                        activity.switchNavItem(navView.getMenu().getItem(5));
                        activity.selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("personalization"));
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar)).setTitle(R.string.pref_personalization);
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar))
                                .setNavigationIcon(R.drawable.ic_arrow_back);
                        activity.findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                    }
                    case "about_app" -> {
                        activity.switchNavItem(navView.getMenu().getItem(5));
                        activity.selectedFragment = Objects.requireNonNull(fm.findFragmentByTag("about_app"));
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar)).setTitle(R.string.pref_about_app);
                        ((MaterialToolbar) activity.findViewById(R.id.app_toolbar))
                                .setNavigationIcon(R.drawable.ic_arrow_back);
                        activity.findViewById(R.id.fab_newpost).setVisibility(View.GONE);
                    }
                }
                ft.show(activity.selectedFragment);
                ft.commit();
            }
        }
    }

    public void navigateTo(Fragment prevFragment, String where) {
        if (ctx instanceof Activity) {
            if (ctx instanceof AppActivity activity) {
                FragmentManager fm = activity.getSupportFragmentManager();
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.hide(prevFragment);
                BottomNavigationView b_navView = activity.findViewById(R.id.bottom_nav_view);
                NavigationView navView = activity.findViewById(R.id.nav_view);
                MaterialToolbar appBar = activity.findViewById(R.id.app_toolbar);
                switch (where) {
                    case "newsfeed" -> {
                        ft.show(activity.newsfeedFragment);
                        activity.selectedFragment = activity.newsfeedFragment;
                    }
                    case "friends" -> {
                        ft.show(activity.friendsFragment);
                        activity.selectedFragment = activity.friendsFragment;
                    }
                    case "groups" -> {
                        ft.show(activity.groupsFragment);
                        activity.selectedFragment = activity.groupsFragment;
                    }
                    case "messages" -> {
                        ft.show(activity.messagesFragment);
                        activity.selectedFragment = activity.messagesFragment;
                    }
                    case "profile" -> {
                        ft.show(activity.profileFragment);
                        activity.selectedFragment = activity.profileFragment;
                    }
                    case "settings" -> {
                        ft.show(activity.mainSettingsFragment);
                        activity.selectedFragment = activity.mainSettingsFragment;
                    }
                }

                if(where.equals("profile")) {
                    appBar.inflateMenu(R.menu.profile);
                    appBar.getMenu().removeItem(R.id.delete_friend);
                } else {
                    appBar.getMenu().clear();
                }

                ft.commit();
            }
        }
    }
}
