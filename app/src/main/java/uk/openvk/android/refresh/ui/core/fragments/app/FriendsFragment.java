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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Friends;
import uk.openvk.android.refresh.api.models.Conversation;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.refresh.ui.core.fragments.app.friends.FriendsListFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.pub_pages.WallFragment;
import uk.openvk.android.refresh.ui.core.listeners.OnRecyclerScrollListener;
import uk.openvk.android.refresh.ui.view.InfinityRecyclerView;
import uk.openvk.android.refresh.ui.view.layouts.ProgressLayout;
import uk.openvk.android.refresh.ui.list.adapters.FriendsAdapter;
import uk.openvk.android.refresh.ui.view.pager.adapters.FriendsPagerAdapter;

public class FriendsFragment extends Fragment {
    private View view;
    private InfinityRecyclerView friendsView;
    private LinearLayoutManager llm;
    private ArrayList<Friend> friends;
    private FriendsAdapter friendsAdapter;
    private SharedPreferences global_prefs;
    private boolean loading_more_friends;
    private FriendsPagerAdapter pagerAdapter;
    private int tabSetup;
    private ArrayList<Friend> friendRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.friends_fragment, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
/*
        Does not work yet!

        setTheme();
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout))
                .setProgressBackgroundColorSchemeResource(R.color.navbarColor);
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setVisibility(View.GONE);

        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshFriendsList(false);
                } else if(requireActivity().getClass().getSimpleName().equals("FriendsIntentActivity")) {
                    ((FriendsIntentActivity) requireActivity()).refreshFriendsList(false);
                }
            }
        });
*/
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        setTabsView();
        return view;
    }

    public void setTabsView() {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.pager);
        String[] frgData = {"A", "B"};
        pagerAdapter = new FriendsPagerAdapter(this, frgData, 2);
        pagerAdapter.createFragment(0);
        pagerAdapter.createFragment(1);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setSaveEnabled(false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position == 1) {
                    if (friendRequests != null) {
                        createRequestsAdapter(friendRequests);
                    }
                }
            }
        });
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if(position == 0) {
                        tab.setText(getResources().getString(R.string.friends_tab));
                    } else {
                        tab.setText(getResources().getString(R.string.friend_requests_tab));
                    }
                }
        );
        mediator.attach();
        tabSetup = 1;
    }

    private void createRequestsAdapter(ArrayList<Friend> friendRequests) {
    }

    private void setTheme() {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = global_prefs.getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setColorSchemeColors(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
            } else {
                ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setColorSchemeColors(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setColorSchemeColors(typedValue.data);
        }
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createFriendsAdapter(Context ctx, ArrayList<Friend> friends, String where) {
        (view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
        view.findViewById(R.id.friends_layout).setVisibility(View.VISIBLE);
        this.friends = friends;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // It is not immediately possible to get the RecyclerView from the embedded fragment, so this is only possible with a delay.
                    FriendsListFragment friendsListFragment = (FriendsListFragment) pagerAdapter.getFragment(0);
                    friendsListFragment.view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    friendsListFragment.view.findViewById(R.id.friends_rv).setVisibility(View.VISIBLE);
                    if(friendsListFragment.getFriendsAdapter() == null) {
                        friendsListFragment.createFriendsAdapter(ctx, friends);
                    }
                    FriendsFragment.this.friendsAdapter = friendsListFragment.getFriendsAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 120);
        //(view.findViewById(R.id.friends_swipe_layout)).setVisibility(View.VISIBLE);

    }

    public void disableUpdateState() {
        //((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setRefreshing(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadAvatars(ArrayList<Conversation> conversations) {
        Context ctx = requireContext();
        friendsAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshAdapter() {
        if(friendsAdapter != null) {
            friendsAdapter.notifyDataSetChanged();
        }
    }

    public void showProgress() {
        (view.findViewById(R.id.error_layout)).setVisibility(View.GONE);
        (view.findViewById(R.id.friends_swipe_layout)).setVisibility(View.GONE);
        (view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
    }

    public int getFriendsCount() {
        if(friendsAdapter != null) {
            return friendsAdapter.getItemCount();
        } else {
            return 0;
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        loading_more_friends = !infinity_scroll;
        friendsView.setOnScrollListener(new OnRecyclerScrollListener() {
            @Override
            public void onScroll(InfinityRecyclerView recyclerView, int x, int y, int old_x, int old_y) {
                View view = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
                int diff = (view.getBottom() - (recyclerView.getHeight() + recyclerView.getScrollY()));
                if (!loading_more_friends) {
                    if (diff == 0) {
                        if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            loading_more_friends = true;
                            ((AppActivity) ctx).loadMoreFriends();
                        } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                            loading_more_friends = true;
                            ((FriendsIntentActivity) ctx).loadMoreFriends();
                        }
                    }
                }
            }
        });
    }
}
