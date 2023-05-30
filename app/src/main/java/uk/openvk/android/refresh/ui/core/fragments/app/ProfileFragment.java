package uk.openvk.android.refresh.ui.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Friends;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.ConversationActivity;
import uk.openvk.android.refresh.ui.core.fragments.app.pub_pages.AboutFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.pub_pages.WallFragment;
import uk.openvk.android.refresh.ui.list.adapters.PublicPageAboutAdapter;
import uk.openvk.android.refresh.ui.list.items.PublicPageAboutItem;
import uk.openvk.android.refresh.ui.view.layouts.ErrorLayout;
import uk.openvk.android.refresh.ui.view.layouts.ProfileHeader;
import uk.openvk.android.refresh.ui.view.layouts.ProgressLayout;
import uk.openvk.android.refresh.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.refresh.ui.view.pager.adapters.PublicPagerAdapter;

public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {
    public ProfileHeader header;
    private View view;
    private ArrayList<WallPost> wallPosts;
    private RecyclerView wallView;
    public NewsfeedAdapter wallAdapter;
    private LinearLayoutManager llm;
    private SharedPreferences global_prefs;
    private User user;
    private PublicPagerAdapter pagerAdapter;
    private AppBarLayout appBar;
    public ArrayList<PublicPageAboutItem> aboutItems;
    private PublicPageAboutAdapter aboutAdapter;

    private int tabSetup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        header = (ProfileHeader) view.findViewById(R.id.header);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        Global.setAvatarShape(requireContext(), header.findViewById(R.id.profile_avatar));
        setTheme();
        appBar = view.findViewById(R.id.app_bar);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout))
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshMyWall(false);
                }
            }
        });
        setFriendsCount(0);
        return view;
    }

    public void setFriendsCount(int count) {
        if(view != null) {
            ((Button) header.findViewById(R.id.show_friends_btn))
                    .setText(String.format(getResources().getStringArray(R.array.friends_count)[2], count));
        }
    }

    // Setting ViewPagers with embedded fragments: WallFragment and AboutFragment

    public void setTabsView() {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.pager);
        String[] frgData = {"A", "B"};
        pagerAdapter = new PublicPagerAdapter(this, frgData, 2);
        pagerAdapter.createFragment(0);
        pagerAdapter.createFragment(1);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setSaveEnabled(false);
        viewPager.setOffscreenPageLimit(2);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position == 1) {
                    if (user != null) {
                        createAboutAdapter(user);
                    }
                }
            }
        });
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if(position == 0) {
                        tab.setText(getResources().getString(R.string.owner_wall_tab));
                    } else {
                        tab.setText(getResources().getString(R.string.info_tab));
                    }
                }
        );
        mediator.attach();
        tabSetup = 1;
    }

    private void setTheme() {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent,
                typedValue, true);
        int unselectedColor = MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
        int accentColor = MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorAccent, Color.BLACK);
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = global_prefs.getBoolean("dark_theme", false);
            if(isDarkTheme) {
                accentColor = Global.getMonetIntColor(monet, "accent", 200);
            } else {
                accentColor = Global.getMonetIntColor(monet, "accent", 500);
            }
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout))
                    .setColorSchemeColors(accentColor);
            ((TabLayout) view.findViewById(R.id.tab_layout))
                    .setSelectedTabIndicatorColor(accentColor);
            ((TabLayout) view.findViewById(R.id.tab_layout))
                    .setTabTextColors(Global.adjustAlpha(unselectedColor, 0.6f), accentColor);
            ((MaterialButton) view.findViewById(R.id.show_friends_btn))
                    .setRippleColor(
                            Global.getMonetRippleColorList(monet, 0, isDarkTheme, 1)
                    );
            ((MaterialButton) view.findViewById(R.id.show_friends_btn)).setTextColor(accentColor);
            ((MaterialButton) view.findViewById(R.id.add_to_btn)).setIconTint(
                    ColorStateList.valueOf(accentColor));
            ((MaterialButton) view.findViewById(R.id.send_msg_btn)).setBackgroundColor(accentColor);
        } else {
            ((TabLayout) view.findViewById(R.id.tab_layout))
                    .setSelectedTabIndicatorColor(accentColor);
            ((TabLayout) view.findViewById(R.id.tab_layout))
                    .setTabTextColors(Global.adjustAlpha(unselectedColor, 0.6f), accentColor);
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout))
                    .setColorSchemeColors(typedValue.data);
        }

        if(global_prefs.getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout))
                    .setProgressBackgroundColorSchemeColor(getResources()
                            .getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout))
                    .setProgressBackgroundColorSchemeColor(getResources()
                            .getColor(android.R.color.white));
        }

    }

    public void setData(User user, Friends friends, Account account, OvkAPIWrapper ovk) {
        this.user = user;
        if(user != null && user.first_name != null && user.last_name != null) {
            if(tabSetup == 0) setTabsView();
            header.setProfileName(String.format("%s %s", user.first_name, user.last_name));
            header.setLastSeen(user.sex, user.ls_date, user.ls_platform);
            header.setStatus(user.status);
            header.setVerified(user.verified, requireActivity());
            header.setOnline(user.online);
            header.setJoinButtonOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.friends_status == 1 || user.friends_status == 3) {
                        friends.delete(ovk, user.id);
                    } else {
                        friends.add(ovk, user.id);
                    }
                }
            });
            if(account.id == user.id) {
                header.findViewById(R.id.send_msg_btn).setVisibility(View.GONE);
                header.findViewById(R.id.add_to_btn).setVisibility(View.GONE);
            } else {
                header.findViewById(R.id.send_msg_btn).setVisibility(View.VISIBLE);
                header.findViewById(R.id.add_to_btn).setVisibility(View.VISIBLE);
                header.setAddToFriendsButtonVisibility(user.friends_status);
            }
            Context ctx = requireContext();
            Global.setAvatarShape(getContext(), view.findViewById(R.id.profile_avatar));
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                Glide.with(ctx).load(
                                String.format("%s/photos_cache/account_avatar/avatar_%s",
                                        ctx.getCacheDir().getAbsolutePath(), user.id))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .placeholder(R.drawable.circular_avatar).error(R.drawable.circular_avatar)
                        .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
            } else {
                Glide.with(ctx).load(
                                String.format("%s/photos_cache/profile_avatars/avatar_%s",
                                        ctx.getCacheDir().getAbsolutePath(), user.id))
                        .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .placeholder(R.drawable.circular_avatar).error(R.drawable.circular_avatar)
                        .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
            }
            if(user.verified) {
                view.findViewById(R.id.verified_icon).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.verified_icon).setVisibility(View.GONE);
            }
            header.findViewById(R.id.show_friends_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(String.format("openvk://friends/id%s", user.id)));
                    final PackageManager pm = requireContext().getPackageManager();
                    @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> activityList
                            = pm.queryIntentActivities(i, 0);
                    for (int index = 0; index < activityList.size(); index++) {
                        ResolveInfo app = activityList.get(index);
                        if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                            i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                        }
                    }
                    startActivity(i);
                }
            });
            header.findViewById(R.id.send_msg_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ConversationActivity.class);
                    try {
                        intent.putExtra("peer_id", user.id);
                        intent.putExtra("conv_title", String.format("%s %s",
                                user.first_name, user.last_name));
                        if(user.online) {
                            intent.putExtra("online", 1);
                        } else {
                            intent.putExtra("online", 0);
                        }
                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public void setFriendStatus(User user, int status) {
        this.user = user;
        this.user.friends_status = status;
        header.setAddToFriendsButtonVisibility(this.user.friends_status);
    }

    private void createAboutAdapter(User user) {
        aboutItems = new ArrayList<PublicPageAboutItem>();
        if(user.status != null && user.status.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.profile_status), user.status));
        if(user.interests != null && user.interests.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.profile_interests), user.interests));
        if(user.music != null && user.music.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.profile_music), user.music));
        if(user.movies != null && user.movies.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.profile_movies), user.movies));
        if(user.tv != null && user.tv.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.profile_tv), user.tv));
        if(user.books != null && user.books.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.profile_books), user.books));
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        AboutFragment aboutFragment = ((AboutFragment) pagerAdapter.getFragment(1));
        aboutFragment.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // It is not immediately possible to get the RecyclerView from the embedded fragment,
                // so this is only possible with a delay.
                try {
                    aboutFragment.view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    aboutFragment.view.findViewById(R.id.about_rv).setVisibility(View.VISIBLE);
                    aboutFragment.createAboutAdapter(requireActivity(), aboutItems);
                    aboutAdapter = ((AboutFragment) pagerAdapter.getFragment(1))
                            .getAboutAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 200);
    }

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setRefreshing(false);
    }

    public void setError(boolean visible, int message, View.OnClickListener listener) {
        ErrorLayout errorLayout = view.findViewById(R.id.error_layout);
        if(visible) {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.GONE);
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayout.setRetryButtonClickListener(listener);
            if(message == HandlerMessages.OVKAPI_NO_INTERNET_CONNECTION) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_no_internet);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle);
            } else if(message == HandlerMessages.OVKAPI_INTERNAL_ERROR || message == HandlerMessages.OVKAPI_UNKNOWN_ERROR) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_instance_failure);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle_instance);
            } else if(message == HandlerMessages.OVKAPI_INSTANCE_UNAVAILABLE) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_instance);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle_instance);
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createWallAdapter(Context ctx, ArrayList<WallPost> posts) {
        this.wallPosts = posts;
        view.findViewById(R.id.progress_layout).setVisibility(View.GONE);
        view.findViewById(R.id.profile_swipe_layout).setVisibility(View.VISIBLE);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment,
                    // so this is only possible with a delay.
                    WallFragment wallFragment = ((WallFragment) pagerAdapter.getFragment(0));
                    wallFragment.view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    wallFragment.view.findViewById(R.id.wall_rv).setVisibility(View.VISIBLE);
                    if(wallFragment.getWallAdapter() == null) {
                        wallFragment.createWallAdapter(ctx, posts);
                    }
                    ProfileFragment.this.wallAdapter = wallFragment.getWallAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 40);

    }

    public void recreateWallAdapter() {
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment,
                    // so this is only possible with a delay.
                    ((WallFragment) pagerAdapter.getFragment(0))
                            .view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    ((WallFragment) pagerAdapter.getFragment(0))
                            .view.findViewById(R.id.wall_rv).setVisibility(View.VISIBLE);
                    ((WallFragment) pagerAdapter.getFragment(0))
                            .createWallAdapter(requireActivity(), wallPosts);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 100);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void wallSelect(int position, String item, int value) {
        if(item.equals("likes")) {
            if(value == 1) {
                wallPosts.get(position).counters.isLiked = true;
            } else {
                wallPosts.get(position).counters.isLiked = false;
            }
            wallAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void wallSelect(int position, String item, String value) {
        if(item.equals("likes")) {
            if(value.equals("add")) {
                wallPosts.get(position).counters.isLiked = true;
            } else {
                wallPosts.get(position).counters.isLiked = false;
            }
            wallAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshWallAdapter() {
        wallAdapter = ((WallFragment) pagerAdapter.getFragment(0)).getWallAdapter();
        ViewPager2 pager = view.findViewById(R.id.pager);
        if(wallAdapter != null) {
            wallAdapter.notifyDataSetChanged();
        }
    }

    public void recreateAboutAdapter() {
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment,
                    // so this is only possible with a delay.
                    ((AboutFragment) pagerAdapter.getFragment(1)).view
                            .findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    ((AboutFragment) pagerAdapter.getFragment(1)).view
                            .findViewById(R.id.about_rv).setVisibility(View.VISIBLE);
                    ((AboutFragment) pagerAdapter.getFragment(1))
                            .createAboutAdapter(requireActivity(), aboutItems);
                    aboutAdapter = ((AboutFragment) pagerAdapter.getFragment(1))
                            .getAboutAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 200);
    }

    public void showProgress() {
    }

    public NewsfeedAdapter getWallAdapter() {
        return ((WallFragment) pagerAdapter.getFragment(0)).getWallAdapter();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setEnabled(verticalOffset == 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        appBar.addOnOffsetChangedListener(this);
        if(wallAdapter != null) recreateWallAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        appBar.removeOnOffsetChangedListener(this);
    }
}
