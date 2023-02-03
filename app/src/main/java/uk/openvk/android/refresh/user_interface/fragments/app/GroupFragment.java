package uk.openvk.android.refresh.user_interface.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.layouts.ErrorLayout;
import uk.openvk.android.refresh.user_interface.layouts.ProfileHeader;
import uk.openvk.android.refresh.user_interface.layouts.ProgressLayout;
import uk.openvk.android.refresh.user_interface.list_adapters.NewsfeedAdapter;

public class GroupFragment extends Fragment {
    public ProfileHeader header;
    private View view;
    private ArrayList<WallPost> wallPosts;
    private RecyclerView wallView;
    public NewsfeedAdapter wallAdapter;
    private LinearLayoutManager llm;
    private SharedPreferences global_prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.group_fragment, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Global.setInterfaceFont((AppCompatActivity) requireActivity(), R.style.ApplicationFont_Inter);
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
        if(group != null && group.name != null) {
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

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setRefreshing(false);
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
            ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.GONE);
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
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setRefreshing(false);
        ((SwipeRefreshLayout) view.findViewById(R.id.group_swipe_layout)).setVisibility(View.VISIBLE);
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
