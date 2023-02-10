package uk.openvk.android.refresh.user_interface.core.fragments.pub_pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.list.adapters.NewsfeedAdapter;
import uk.openvk.android.refresh.user_interface.list.adapters.PublicPageAboutAdapter;
import uk.openvk.android.refresh.user_interface.list.items.PublicPageAboutItem;

public class AboutFragment extends Fragment {
    public View view;
    private Context ctx;
    private ArrayList<PublicPageAboutItem> aboutItems;
    private RecyclerView about_rv;
    private RecyclerView aboutView;
    private LinearLayoutManager llm;
    private PublicPageAboutAdapter aboutAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.about_page_tab, container, false);
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
            aboutView = about_rv;
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            aboutView.setLayoutManager(llm);
            aboutAdapter = new PublicPageAboutAdapter(ctx, this.aboutItems);
            aboutView.setAdapter(aboutAdapter);
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
