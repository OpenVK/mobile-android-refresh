package uk.openvk.android.refresh.user_interface.core.fragments.pub_pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.list.adapters.NewsfeedAdapter;

public class WallFragment extends Fragment {
    private View view;
    private RecyclerView wallView;
    public NewsfeedAdapter wallAdapter;
    private LinearLayoutManager llm;
    private ArrayList<WallPost> wallPosts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = new Bundle();
        data.putString("createState", "ok");
        setArguments(data);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wall_tab, container, false);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createWallAdapter(Context ctx, ArrayList<WallPost> posts) {
        this.wallPosts = posts;
        if(wallAdapter == null) {
            wallView = view.findViewById(R.id.wall_rv);
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            wallView.setLayoutManager(llm);
            wallAdapter = new NewsfeedAdapter(ctx, this.wallPosts);
            wallView.setAdapter(wallAdapter);
        } else {
            //newsfeedAdapter.setArray(wallPosts);
            wallAdapter.notifyDataSetChanged();
        }
    }
    public NewsfeedAdapter getWallAdapter() {
        return wallAdapter;
    }
}
