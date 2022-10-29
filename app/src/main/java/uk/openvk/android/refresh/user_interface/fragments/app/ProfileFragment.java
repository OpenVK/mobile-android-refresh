package uk.openvk.android.refresh.user_interface.fragments.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import uk.openvk.android.refresh.R;

public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.auth_progress_fragment, container, false);
        return view;
    }
}
