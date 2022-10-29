package uk.openvk.android.refresh.user_interface.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.layouts.ProgressLayout;
import uk.openvk.android.refresh.user_interface.list_adapters.NewsfeedAdapter;

public class NewsfeedFragment extends Fragment {
    private LinearLayoutManager layoutManager;
    private RecyclerView newsfeedView;
    private ArrayList<WallPost> wallPosts;
    private View view;
    private NewsfeedAdapter newsfeedAdapter;
    private LinearLayoutManager llm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.newsfeed, container, false);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                .setProgressBackgroundColorSchemeResource(R.color.navbarColor);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setColorSchemeResources(R.color.primaryColor);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshNewsfeed();
                }
            }
        });
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createAdapter(Context ctx, ArrayList<WallPost> wallPosts) {
        this.wallPosts = wallPosts;
        newsfeedView = (RecyclerView) view.findViewById(R.id.newsfeed_rv);
        if(newsfeedAdapter == null) {
            newsfeedAdapter = new NewsfeedAdapter(getContext(), this.wallPosts);
            llm = new LinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            newsfeedView.setLayoutManager(llm);
            newsfeedView.setAdapter(newsfeedAdapter);
        } else {
            //newsfeedAdapter.setArray(wallPosts);
            newsfeedAdapter.notifyDataSetChanged();
        }
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setVisibility(View.VISIBLE);
    }

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setRefreshing(false);
    }
}
