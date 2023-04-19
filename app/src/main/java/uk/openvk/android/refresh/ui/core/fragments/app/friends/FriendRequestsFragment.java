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
import uk.openvk.android.refresh.ui.list.adapters.FriendRequestsAdapter;
import uk.openvk.android.refresh.ui.list.items.PublicPageAboutItem;

public class FriendRequestsFragment extends Fragment {
    public View view;
    private Context ctx;
    private ArrayList<PublicPageAboutItem> aboutItems;
    private SwipeRefreshLayout friends_req_srl;
    private LinearLayoutManager llm;
    private FriendRequestsAdapter requestsAdapter;
    private RecyclerView requests_rv;
    private ArrayList<Friend> friendRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_friend_requests, container, false);
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        friends_req_srl = view.findViewById(R.id.friends_swipe_layout);
        requests_rv = view.findViewById(R.id.requests_rv);
        loading_layout.setVisibility(View.VISIBLE);
        requests_rv.setVisibility(View.GONE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createRequestsAdapter(Context ctx, ArrayList<Friend> items) {
        this.ctx = ctx;
        this.friendRequests = items;
        if(requestsAdapter == null) {
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            requests_rv.setLayoutManager(llm);
            requestsAdapter = new FriendRequestsAdapter(ctx, this.friendRequests);
            requests_rv.setAdapter(requestsAdapter);
        } else {
            requestsAdapter.notifyDataSetChanged();
        }
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        @SuppressLint("CutPasteId") RecyclerView requests_rv = view.findViewById(R.id.requests_rv);
        loading_layout.setVisibility(View.GONE);
        requests_rv.setVisibility(View.VISIBLE);
    }

    public FriendRequestsAdapter getRequestsAdapter() {
        return requestsAdapter;
    }
}
