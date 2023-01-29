package uk.openvk.android.refresh.user_interface.fragments.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.user_interface.layouts.ProfileHeader;

public class ProfileFragment extends Fragment {
    public ProfileHeader header;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile, container, false);
        header = (ProfileHeader) view.findViewById(R.id.header);
        return view;
    }

    public void setData(User user) {
        header.setProfileName(String.format("%s %s", user.first_name, user.last_name));
        header.setLastSeen(user.sex, user.ls_date, user.ls_platform);
        header.setStatus(user.status);
        header.setOnline(user.online);
    }
}
