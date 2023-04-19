package uk.openvk.android.refresh.ui.core.fragments.app.pub_pages;

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

import java.util.ArrayList;

import uk.openvk.android.refresh.R;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            about_rv.setLayoutManager(llm);
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
