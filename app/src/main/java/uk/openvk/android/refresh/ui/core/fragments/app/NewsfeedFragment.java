package uk.openvk.android.refresh.ui.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.WallPost;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.refresh.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.refresh.ui.view.InfinityRecyclerView;
import uk.openvk.android.refresh.ui.view.layouts.ErrorLayout;
import uk.openvk.android.refresh.ui.view.layouts.ProgressLayout;

public class NewsfeedFragment extends Fragment {
    private LinearLayoutManager layoutManager;
    private InfinityRecyclerView newsfeedView;
    private ArrayList<WallPost> wallPosts;
    private View view;
    public NewsfeedAdapter newsfeedAdapter;
    private LinearLayoutManager llm;
    private SharedPreferences global_prefs;
    public boolean loading_more_posts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.fragment_newsfeed, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setVisibility(View.GONE);
        try {
            if (OvkApplication.isTablet) {
                view.findViewById(R.id.newsfeed_layout).setVisibility(View.GONE);
            }
        } catch (Exception ignored) {

        }
        setTheme();
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshNewsfeed(false);
                }
            }
        });
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        return view;
    }

    private void setTheme() {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent,
                typedValue, true);
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = global_prefs.getBoolean("dark_theme", false);
            int accentColor = 0;
            if(isDarkTheme) {
                accentColor = Global.getMonetIntColor(monet, "accent", 200);
                ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                        .setColorSchemeColors(accentColor);
            } else {
                accentColor = Global.getMonetIntColor(monet, "accent", 500);
                ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                        .setColorSchemeColors(accentColor);
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                    .setColorSchemeColors(typedValue.data);
        }
        if(global_prefs.getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                    .setProgressBackgroundColorSchemeColor(getResources().getColor(
                            com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                    .setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createAdapter(Context ctx, ArrayList<WallPost> wallPosts) {
        this.wallPosts = wallPosts;
        newsfeedView = view.findViewById(R.id.newsfeed_rv);
        if(newsfeedAdapter == null) {
            if(ctx instanceof AppActivity) {
                newsfeedAdapter = new NewsfeedAdapter(getActivity(), this.wallPosts,
                        ((AppActivity) ctx).ovk_api.account);
            } else if(ctx instanceof ProfileIntentActivity) {
                newsfeedAdapter = new NewsfeedAdapter(getActivity(), this.wallPosts,
                        ((ProfileIntentActivity) ctx).ovk_api.account);
            } else if(ctx instanceof GroupIntentActivity) {
                newsfeedAdapter = new NewsfeedAdapter(getActivity(), this.wallPosts,
                        ((GroupIntentActivity) ctx).ovk_api.account);
            }
            llm = new LinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            newsfeedView.setLayoutManager(llm);
            newsfeedView.setAdapter(newsfeedAdapter);
        } else {
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setRefreshing(false);
    }

    public void disableLoadState() {
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
                if(OvkApplication.isTablet) {
                    view.findViewById(R.id.newsfeed_layout).setVisibility(View.VISIBLE);
                }
                ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setVisibility(View.VISIBLE);
            }
        }, 200);

    }

    public void setError(boolean visible, int message, View.OnClickListener listener) {
        ErrorLayout errorLayout = view.findViewById(R.id.error_layout);
        if(visible) {
            ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                    .setVisibility(View.GONE);
            ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                    .setVisibility(View.GONE);
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayout.setRetryButtonClickListener(listener);
            if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_no_internet);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle);
            } else if(message == HandlerMessages.CONNECTION_TIMEOUT) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_instance_nrps);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle_instance);
            } else if(message == HandlerMessages.INTERNAL_ERROR
                    || message == HandlerMessages.UNKNOWN_ERROR) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_instance_failure);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle_instance);
            } else if(message == HandlerMessages.INSTANCE_UNAVAILABLE) {
                ((TextView) errorLayout.findViewById(R.id.error_title))
                        .setText(R.string.error_instance);
                ((TextView) errorLayout.findViewById(R.id.error_subtitle))
                        .setText(R.string.error_subtitle_instance);
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                    .setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            ((ProgressLayout) view.findViewById(R.id.progress_layout))
                    .setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshAdapter() {
        if(newsfeedAdapter != null) {
            newsfeedAdapter.setArray(wallPosts);
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void showProgress() {
        ((ErrorLayout) view.findViewById(R.id.error_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void select(int position, String item, int value) {
        if(item.equals("likes")) {
            wallPosts.get(position).counters.isLiked = value == 1;
            newsfeedAdapter.notifyItemChanged(position, false);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void select(int position, String item, String value) {
        if(item.equals("likes")) {
            if(value.equals("add")) {
                wallPosts.get(position).counters.isLiked = true;
            } else {
                wallPosts.get(position).counters.isLiked = false;
            }
            newsfeedAdapter.notifyItemChanged(position, false);
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        loading_more_posts = !infinity_scroll;
        newsfeedView.setOnScrollListener((recyclerView, x, y, old_x, old_y) -> {
            View view = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
            if(recyclerView.getChildCount() > 2) {
                int diff = (view.getBottom() - (recyclerView.getHeight() + recyclerView.getScrollY()));
                if (!loading_more_posts) {
                    if (diff == 0) {
                        if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).loadMoreNews();
                            loading_more_posts = true;
                        }
                    }
                }
            }
        });
    }
}
