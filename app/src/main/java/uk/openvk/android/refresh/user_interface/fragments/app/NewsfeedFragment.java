package uk.openvk.android.refresh.user_interface.fragments.app;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.color.MaterialColors;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.WallPost;

public class NewsfeedFragment extends Fragment {
    private LinearLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newsfeed, container, false);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout))
                .setProgressBackgroundColorSchemeResource(R.color.navbarColor);
        ((SwipeRefreshLayout) view.findViewById(R.id.newsfeed_swipe_layout)).setColorSchemeResources(R.color.primaryColor);
        return view;
    }
}
