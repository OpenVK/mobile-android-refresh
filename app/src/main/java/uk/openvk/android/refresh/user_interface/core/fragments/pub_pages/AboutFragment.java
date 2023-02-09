package uk.openvk.android.refresh.user_interface.core.fragments.pub_pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import uk.openvk.android.refresh.R;

public class AboutFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.about_page_tab, container, false);
        LinearLayout loading_layout = view.findViewById(R.id.loading_layout);
        RecyclerView about_rv = view.findViewById(R.id.about_rv);
        loading_layout.setVisibility(View.VISIBLE);
        about_rv.setVisibility(View.GONE);
        return view;
    }
}
