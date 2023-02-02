package uk.openvk.android.refresh.user_interface.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.layouts.ErrorLayout;
import uk.openvk.android.refresh.user_interface.layouts.ProfileHeader;
import uk.openvk.android.refresh.user_interface.layouts.ProgressLayout;
import uk.openvk.android.refresh.user_interface.list_adapters.NewsfeedAdapter;

public class ProfileFragment extends Fragment {
    public ProfileHeader header;
    private View view;
    private ArrayList<WallPost> wallPosts;
    private RecyclerView wallView;
    private NewsfeedAdapter wallAdapter;
    private LinearLayoutManager llm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        header = (ProfileHeader) view.findViewById(R.id.header);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        Global.setAvatarShape(requireContext(), header.findViewById(R.id.profile_avatar));
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setColorSchemeColors(typedValue.data);
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

    public void setData(User user) {
        if(user != null && user.first_name != null && user.last_name != null) {
            header.setProfileName(String.format("%s %s", user.first_name, user.last_name));
            header.setLastSeen(user.sex, user.ls_date, user.ls_platform);
            header.setStatus(user.status);
            header.setVerified(true, requireContext());
            header.setOnline(user.online);
            Context ctx = requireContext();
            Global.setAvatarShape(getContext(), view.findViewById(R.id.profile_avatar));
            Glide.with(ctx).load(
                    String.format("%s/photos_cache/account_avatar/avatar_%s",
                            ctx.getCacheDir().getAbsolutePath(), user.id))
                    .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
            ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.VISIBLE);
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
        wallView = (RecyclerView) view.findViewById(R.id.wall_rv);
        if(wallAdapter == null) {
            wallAdapter = new NewsfeedAdapter(getActivity(), this.wallPosts);
            llm = new LinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            wallView.setLayoutManager(llm);
            wallView.setAdapter(wallAdapter);
        } else {
            //newsfeedAdapter.setArray(wallPosts);
            wallAdapter.notifyDataSetChanged();
        }
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setRefreshing(false);
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setVisibility(View.VISIBLE);
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
}
