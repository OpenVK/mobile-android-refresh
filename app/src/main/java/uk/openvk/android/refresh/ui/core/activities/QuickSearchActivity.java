package uk.openvk.android.refresh.ui.core.activities;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Groups;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.enumerations.UiMessages;
import uk.openvk.android.refresh.ui.list.sections.CommunitiesSearchSection;
import uk.openvk.android.refresh.ui.list.sections.PeopleSearchSection;

public class QuickSearchActivity extends MonetCompatActivity {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    public Handler handler;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    private boolean isDarkTheme;
    private Users users;
    private Groups groups;
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
        setMonetTheme();
        searchBar = ((MaterialSearchBar) findViewById(R.id.search_bar));
        searchBar.openSearch();
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!searchBar.isSearchOpened()) {
                    finish();
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
                groups.search(ovk_api, query);
                users.search(ovk_api, query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });
        setAPIWrapper();
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
                    groups.getList());
            sectionAdapter.addSection(commsSection);
        } else {
            commsSection = new CommunitiesSearchSection(QuickSearchActivity.this,
                    groups.getList());
        }
        if(peopleSection == null) {
            peopleSection = new PeopleSearchSection(QuickSearchActivity.this,
                    users.getList());
            sectionAdapter.addSection(peopleSection);
        } else {
            peopleSection = new PeopleSearchSection(QuickSearchActivity.this,
                    users.getList());
        }
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        users = new Users();
        groups = new Groups();
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("OpenVK", String.format("Handling API message: %s", msg.what));
                receiveState(msg.what, msg.getData());
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.USERS_SEARCH) {
            users.parseSearch(data.getString("response"));
            handler.sendEmptyMessage(UiMessages.UPTIME_QUICK_SEARCH);
        } else if(message == HandlerMessages.GROUPS_SEARCH) {
            groups.parseSearch(data.getString("response"));
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
        User user = users.getList().get(position);
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
        Group group = groups.getList().get(position);
        String url = "";
        if(group.id > 0) {
            url = String.format("openvk://group/club%s", group.id);
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
}
