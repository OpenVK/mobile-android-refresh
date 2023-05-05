package uk.openvk.android.refresh.ui.core.fragments.app.pub_pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.core.fragments.app.friends.FriendRequestsFragment;
import uk.openvk.android.refresh.ui.list.adapters.PublicPageAboutAdapter;
import uk.openvk.android.refresh.ui.list.items.PublicPageAboutItem;

public class AboutFragment extends Fragment {
    public View view;
    private Context ctx;
    private ArrayList<PublicPageAboutItem> aboutItems;
    private RecyclerView about_rv;
    private RecyclerView aboutView;
    private LinearLayoutManager llm;
    private PublicPageAboutAdapter aboutAdapter;
    public Handler handler;

    public static AboutFragment createInstance(int page) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        fragment.setArguments(args);
        fragment.handler = new Handler();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        handler = new Handler();
        view = inflater.inflate(R.layout.tab_about_page, container, false);
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        about_rv = view.findViewById(R.id.about_rv);
        loading_layout.setVisibility(View.VISIBLE);
        about_rv.setVisibility(View.GONE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createAboutAdapter(Context ctx, ArrayList<PublicPageAboutItem> items) {
        this.ctx = ctx;
        this.aboutItems = items;
        if(aboutAdapter == null) {
            if(OvkApplication.isTablet && getResources().getConfiguration().smallestScreenWidthDp >= 760) {
                GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
                about_rv.setLayoutManager(glm);
            } else {
                llm = new LinearLayoutManager(getContext());
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                about_rv.setLayoutManager(llm);
            }
            aboutAdapter = new PublicPageAboutAdapter(ctx, this.aboutItems);
            about_rv.setAdapter(aboutAdapter);
        } else {
            aboutAdapter.notifyDataSetChanged();
        }
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        @SuppressLint("CutPasteId") RecyclerView about_rv = view.findViewById(R.id.about_rv);
        loading_layout.setVisibility(View.GONE);
        about_rv.setVisibility(View.VISIBLE);
    }

    public PublicPageAboutAdapter getAboutAdapter() {
        return aboutAdapter;
    }
}
