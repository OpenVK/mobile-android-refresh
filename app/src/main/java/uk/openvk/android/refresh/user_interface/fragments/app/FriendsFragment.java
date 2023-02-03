package uk.openvk.android.refresh.user_interface.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Conversation;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.layouts.ErrorLayout;
import uk.openvk.android.refresh.user_interface.layouts.ProgressLayout;
import uk.openvk.android.refresh.user_interface.list_adapters.FriendsAdapter;

public class FriendsFragment extends Fragment {
    private View view;
    private RecyclerView friendsView;
    private LinearLayoutManager llm;
    private ArrayList<Friend> friends;
    private FriendsAdapter friendsAdapter;
    private SharedPreferences global_prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity(), R.style.ApplicationFont_Comfortaa);
        view = inflater.inflate(R.layout.friends_fragment, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout))
                .setProgressBackgroundColorSchemeResource(R.color.navbarColor);
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setVisibility(View.GONE);
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setColorSchemeColors(typedValue.data);
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshFriendsList(false);
                }
            }
        });
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createAdapter(Context ctx, ArrayList<Friend> friends, String where) {
        this.friends = friends;
        friendsView = (RecyclerView) view.findViewById(R.id.friends_rv);
        if(friendsAdapter == null) {
            friendsAdapter = new FriendsAdapter(getContext(), this.friends);
            llm = new LinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            friendsView.setLayoutManager(llm);
            friendsView.setAdapter(friendsAdapter);
        } else {
            friendsAdapter.notifyDataSetChanged();
        }
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setVisibility(View.VISIBLE);
    }

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setRefreshing(false);
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
        ((ErrorLayout) view.findViewById(R.id.error_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.friends_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
    }
}
