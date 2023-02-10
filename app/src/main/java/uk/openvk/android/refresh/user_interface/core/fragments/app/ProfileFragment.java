package uk.openvk.android.refresh.user_interface.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.core.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.core.fragments.pub_pages.WallFragment;
import uk.openvk.android.refresh.user_interface.view.layouts.ErrorLayout;
import uk.openvk.android.refresh.user_interface.view.layouts.ProfileHeader;
import uk.openvk.android.refresh.user_interface.view.layouts.ProgressLayout;
import uk.openvk.android.refresh.user_interface.list.adapters.NewsfeedAdapter;
import uk.openvk.android.refresh.user_interface.view.pager.adapters.PublicPagerAdapter;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        header = (ProfileHeader) view.findViewById(R.id.header);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        Global.setAvatarShape(requireContext(), header.findViewById(R.id.profile_avatar));
        setTheme();
        appBar = view.findViewById(R.id.app_bar);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshMyWall(false);
                }
            }
        });
        return view;
    }

    // Setting ViewPagers with embedded fragments: WallFragment and AboutFragment

    public void setTabsView() {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.pager);
        pagerAdapter = new PublicPagerAdapter(this);
        pagerAdapter.createFragment(0);
        pagerAdapter.createFragment(1);
        viewPager.setAdapter(pagerAdapter);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if(position == 0) {
                        if(user != null) {
                            tab.setText(getResources().getString(R.string.owner_wall_tab, user.first_name));
                        } else {
                            tab.setText("Tab 1");
                        }
                    } else {
                        tab.setText(getResources().getString(R.string.info_tab));
                    }
                }
        );
        mediator.attach();

        // WORKAROUND: Auto-resizing ViewPager2, because fragments have different height
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                resizeViewPager(pagerAdapter.getFragment(viewPager.getCurrentItem()).getView(), viewPager);
            }
        });
    }

    private void resizeViewPager(View page, ViewPager2 viewPager) {
        Objects.requireNonNull(page).post(new Runnable() {
            @Override
            public void run() {
                int wMeasureSpec = View.MeasureSpec.makeMeasureSpec(page.getWidth(), View.MeasureSpec.EXACTLY);
                int hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                page.measure(wMeasureSpec, hMeasureSpec);
                Log.d("OpenVK", String.format("ViewPager height: %s | Page measured height: %s", viewPager.getLayoutParams().height, page.getMeasuredHeight()));
                if (viewPager.getLayoutParams().height != page.getMeasuredHeight()) {
                    viewPager.getLayoutParams().height = page.getMeasuredHeight();
                }
                viewPager.invalidate();
            }
        });
    }

    private void resizeViewPager(View page, ViewPager2 viewPager, int interval) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                viewPager.requestTransform();
            }
        }, 0, interval);

    }

    private void setTheme() {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = global_prefs.getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setColorSchemeColors(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
            } else {
                ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setColorSchemeColors(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setColorSchemeColors(typedValue.data);
        }

        if(global_prefs.getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
    }

    public void setData(User user) {
        this.user = user;
        if(user != null && user.first_name != null && user.last_name != null) {
            if(pagerAdapter == null) setTabsView();
            header.setProfileName(String.format("%s %s", user.first_name, user.last_name));
            header.setLastSeen(user.sex, user.ls_date, user.ls_platform);
            header.setStatus(user.status);
            header.setVerified(user.verified, requireContext());
            header.setOnline(user.online);
            Context ctx = requireContext();
            Global.setAvatarShape(getContext(), view.findViewById(R.id.profile_avatar));
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                Glide.with(ctx).load(
                                String.format("%s/photos_cache/account_avatar/avatar_%s",
                                        ctx.getCacheDir().getAbsolutePath(), user.id))
                        .placeholder(R.drawable.circular_avatar).error(R.drawable.circular_avatar)
                        .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
            } else {
                Glide.with(ctx).load(
                                String.format("%s/photos_cache/profile_avatars/avatar_%s",
                                        ctx.getCacheDir().getAbsolutePath(), user.id))
                        .placeholder(R.drawable.circular_avatar).error(R.drawable.circular_avatar)
                        .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
            }
            if(user.verified) {
                view.findViewById(R.id.verified_icon).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.verified_icon).setVisibility(View.GONE);
            }
        }
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
        ((WallFragment) pagerAdapter.getFragment(0)).createWallAdapter(ctx, posts);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        appBar.removeOnOffsetChangedListener(this);
    }
}
