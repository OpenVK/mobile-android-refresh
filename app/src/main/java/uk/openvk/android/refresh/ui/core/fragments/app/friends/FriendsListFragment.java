package uk.openvk.android.refresh.ui.core.fragments.app.friends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.ui.list.adapters.FriendsAdapter;
import uk.openvk.android.refresh.ui.list.adapters.PublicPageAboutAdapter;

public class FriendsListFragment extends Fragment {
    public View view;
    private Context ctx;
    private LinearLayoutManager llm;
    private PublicPageAboutAdapter aboutAdapter;
    private SwipeRefreshLayout friends_srl;
    private RecyclerView friends_rv;
    private ArrayList<Friend> friends;
    private FriendsAdapter friendsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_friends, container, false);
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        friends_srl = view.findViewById(R.id.friends_swipe_layout);
        friends_rv = view.findViewById(R.id.friends_rv);
        loading_layout.setVisibility(View.VISIBLE);
        friends_srl.setVisibility(View.GONE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createFriendsAdapter(Context ctx, ArrayList<Friend> items) {
        this.ctx = ctx;
        this.friends = items;
        if(aboutAdapter == null) {
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            friends_rv.setLayoutManager(llm);
            friendsAdapter = new FriendsAdapter(ctx, this.friends);
            friends_rv.setAdapter(aboutAdapter);
        } else {
            friendsAdapter.notifyDataSetChanged();
        }
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        @SuppressLint("CutPasteId") RecyclerView friends_rv = view.findViewById(R.id.friends_rv);
        loading_layout.setVisibility(View.GONE);
        friends_rv.setVisibility(View.VISIBLE);
    }

    public FriendsAdapter getFriendsAdapter() {
        return friendsAdapter;
    }
}
