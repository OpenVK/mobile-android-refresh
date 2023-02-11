package uk.openvk.android.refresh.ui.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.fragments.pub_pages.AboutFragment;
import uk.openvk.android.refresh.ui.core.fragments.pub_pages.WallFragment;
import uk.openvk.android.refresh.ui.list.adapters.PublicPageAboutAdapter;
import uk.openvk.android.refresh.ui.list.items.PublicPageAboutItem;
import uk.openvk.android.refresh.ui.view.layouts.ErrorLayout;
import uk.openvk.android.refresh.ui.view.layouts.ProfileHeader;
import uk.openvk.android.refresh.ui.view.layouts.ProgressLayout;
import uk.openvk.android.refresh.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.refresh.ui.view.pager.adapters.PublicPagerAdapter;

public class CommunityFragment extends Fragment  implements AppBarLayout.OnOffsetChangedListener {
    public ProfileHeader header;
    private View view;
    private ArrayList<WallPost> wallPosts;
    private RecyclerView wallView;
    public NewsfeedAdapter wallAdapter;
    private LinearLayoutManager llm;
    private SharedPreferences global_prefs;
    private PublicPagerAdapter pagerAdapter;
    private Group group;
    private ArrayList<PublicPageAboutItem> aboutItems;
    private PublicPageAboutAdapter aboutAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.community_fragment, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        header = (ProfileHeader) view.findViewById(R.id.header);
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        Global.setAvatarShape(requireContext(), header.findViewById(R.id.profile_avatar));
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setColorSchemeColors(typedValue.data);
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshMyWall(false);
                }
            }
        });
        return view;
    }

    public void setData(Group group) {
        this.group = group;
        if(group != null && group.name != null) {
            setTabsView();
            header.setProfileName(group.name);
            header.setStatus(group.description);
            header.findViewById(R.id.last_seen).setVisibility(View.GONE);
            header.setVerified(group.verified, requireContext());
            Context ctx = requireContext();
            Global.setAvatarShape(getContext(), view.findViewById(R.id.profile_avatar));
            Glide.with(ctx).load(
                    String.format("%s/photos_cache/group_avatars/avatar_%s",
                            ctx.getCacheDir().getAbsolutePath(), group.id))
                    .placeholder(R.drawable.circular_avatar).error(R.drawable.circular_avatar)
                    .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.VISIBLE);
        }
    }

    public void setTabsView() {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.pager);
        pagerAdapter = new PublicPagerAdapter(this);
        pagerAdapter.createFragment(0);
        pagerAdapter.createFragment(1);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setSaveEnabled(false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position == 1) {
                    if (group != null) {
                        createAboutAdapter(group);
                    }
                }
            }
        });
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if(position == 0) {
                        if(group != null) {
                            tab.setText(getResources().getString(R.string.owner_wall_tab));
                        } else {
                            tab.setText("Tab 1");
                        }
                    } else {
                        tab.setText(getResources().getString(R.string.info_tab));
                    }
                }
        );
        mediator.attach();
    }

    private void createAboutAdapter(Group group) {
        aboutItems = new ArrayList<PublicPageAboutItem>();
        if(group.description != null && group.description.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.group_descr), group.description));
        if(group.site != null && group.site.length() > 0)
            aboutItems.add(new PublicPageAboutItem(getResources().getString(R.string.group_site), group.site));
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // It is not immediately possible to get the RecyclerView from the embedded fragment, so this is only possible with a delay.
                try {
                    ((AboutFragment) pagerAdapter.getFragment(1)).view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    ((AboutFragment) pagerAdapter.getFragment(1)).view.findViewById(R.id.about_rv).setVisibility(View.VISIBLE);
                    ((AboutFragment) pagerAdapter.getFragment(1)).createAboutAdapter(requireActivity(), aboutItems);
                    aboutAdapter = ((AboutFragment) pagerAdapter.getFragment(1)).getAboutAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 1000);
    }

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setRefreshing(false);
    }

    public void setError(boolean visible, int message, View.OnClickListener listener) {
        ErrorLayout errorLayout = view.findViewById(R.id.error_layout);
        if(visible) {
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.GONE);
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.GONE);
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayout.setRetryButtonClickListener(listener);
            if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
                ((TextView) errorLayout.findViewById(R.id.error_title)).setText(R.string.error_no_internet);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle)).setText(R.string.error_subtitle);
            } else if(message == HandlerMessages.INTERNAL_ERROR || message == HandlerMessages.UNKNOWN_ERROR) {
                ((TextView) errorLayout.findViewById(R.id.error_title)).setText(R.string.error_instance_failure);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle)).setText(R.string.error_subtitle_instance);
            } else if(message == HandlerMessages.INSTANCE_UNAVAILABLE) {
                ((TextView) errorLayout.findViewById(R.id.error_title)).setText(R.string.error_instance);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle)).setText(R.string.error_subtitle_instance);
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createWallAdapter(Context ctx, ArrayList<WallPost> posts) {
        this.wallPosts = posts;
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setRefreshing(false);
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment, so this is only possible with a delay.
                    ((WallFragment) pagerAdapter.getFragment(0)).view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    ((WallFragment) pagerAdapter.getFragment(0)).view.findViewById(R.id.wall_rv).setVisibility(View.VISIBLE);
                    ((WallFragment) pagerAdapter.getFragment(0)).createWallAdapter(ctx, posts);
                    wallAdapter = ((WallFragment) pagerAdapter.getFragment(0)).getWallAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 200);

    }

    public void recreateWallAdapter() {
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment, so this is only possible with a delay.
                    ((WallFragment) pagerAdapter.getFragment(0)).view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    ((WallFragment) pagerAdapter.getFragment(0)).view.findViewById(R.id.wall_rv).setVisibility(View.VISIBLE);
                    ((WallFragment) pagerAdapter.getFragment(0)).createWallAdapter(requireActivity(), wallPosts);
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
        if(wallAdapter != null) {
            wallAdapter.notifyDataSetChanged();
        }
    }

    public void showProgress() {
    }

    public void recreateAboutAdapter() {
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment, so this is only possible with a delay.
                    ((AboutFragment) pagerAdapter.getFragment(1)).view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    ((AboutFragment) pagerAdapter.getFragment(1)).view.findViewById(R.id.about_rv).setVisibility(View.VISIBLE);
                    ((AboutFragment) pagerAdapter.getFragment(1)).createAboutAdapter(requireActivity(), aboutItems);
                    aboutAdapter = ((AboutFragment) pagerAdapter.getFragment(1)).getAboutAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 200);
    }

    public NewsfeedAdapter getWallAdapter() {
        return ((WallFragment) pagerAdapter.getFragment(0)).getWallAdapter();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setEnabled(verticalOffset == 0);
    }
}
