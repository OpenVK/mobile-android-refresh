package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Group;
import uk.openvk.android.refresh.api.entities.User;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.core.enumerations.UiMessages;
import uk.openvk.android.refresh.ui.list.sections.CommunitiesSearchSection;
import uk.openvk.android.refresh.ui.list.sections.PeopleSearchSection;

public class QuickSearchActivity extends BaseNetworkActivity {
    private boolean isDarkTheme;
    private MaterialSearchBar searchBar;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private PeopleSearchSection peopleSection;
    private CommunitiesSearchSection commsSection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"),
                getWindow());
        Global.setInterfaceFont(this);
        instance_prefs = getSharedPreferences("instance", 0);

        setContentView(R.layout.activity_search);
        setStatusBarColor(R.color.backgroundColor);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isDarkTheme) {
            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
        searchBar = findViewById(R.id.search_bar);
        searchBar.openSearch();
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                searchBar.setEnabled(false);
                if(!searchBar.isSearchOpened()) {
                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 500);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                String query = text.toString();
                if(sectionAdapter != null) {
                    sectionAdapter.removeAllSections();
                    peopleSection = null;
                    commsSection = null;
                }
                ovk_api.groups.search(ovk_api.wrapper, query);
                ovk_api.users.search(ovk_api.wrapper, query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void createSearchResultsAdapter(RecyclerView rv) {
        if(sectionAdapter == null) {
            sectionAdapter = new SectionedRecyclerViewAdapter();
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(sectionAdapter);
        }
        if(commsSection == null) {
            commsSection = new CommunitiesSearchSection(QuickSearchActivity.this,
                    ovk_api.groups.getList());
            sectionAdapter.addSection(commsSection);
        } else {
            commsSection = new CommunitiesSearchSection(QuickSearchActivity.this,
                    ovk_api.groups.getList());
        }
        if(peopleSection == null) {
            peopleSection = new PeopleSearchSection(QuickSearchActivity.this,
                    ovk_api.users.getList());
            sectionAdapter.addSection(peopleSection);
        } else {
            peopleSection = new PeopleSearchSection(QuickSearchActivity.this,
                    ovk_api.users.getList());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.USERS_SEARCH) {
            handler.sendEmptyMessage(UiMessages.UPTIME_QUICK_SEARCH);
        } else if(message == HandlerMessages.GROUPS_SEARCH) {
            handler.sendEmptyMessage(UiMessages.UPTIME_QUICK_SEARCH);
        } else if(message == UiMessages.UPTIME_QUICK_SEARCH) {
            final RecyclerView searchResultsView = findViewById(R.id.results_rv);
            createSearchResultsAdapter(searchResultsView);
            sectionAdapter.notifyDataSetChanged();
        }
    }

    private void setMonetTheme() {
    }

    public void openProfile(int position) {
        User user = ovk_api.users.getList().get(position);
        String url = "";
        if(user.id > 0) {
            url = String.format("openvk://profile/id%s", user.id);
        }
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

    public void openGroup(int position) {
        Group group = ovk_api.groups.getList().get(position);
        String url = "";
        if(group.id > 0) {
            url = String.format("openvk://group/club%s", group.id);
        }
        if(!url.isEmpty()) {
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
}
