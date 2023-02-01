package uk.openvk.android.refresh.user_interface.fragments.app;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.user_interface.layouts.ProfileHeader;

public class ProfileFragment extends Fragment {
    public ProfileHeader header;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        header = (ProfileHeader) view.findViewById(R.id.header);
        Global.setAvatarShape(requireContext(), header.findViewById(R.id.profile_avatar));
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
        ((SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_layout)).setColorSchemeColors(typedValue.data);
        return view;
    }

    public void setData(User user) {
        if(user != null && user.first_name != null && user.last_name != null) {
            header.setProfileName(String.format("%s %s", user.first_name, user.last_name));
            header.setLastSeen(user.sex, user.ls_date, user.ls_platform);
            header.setStatus(user.status);
            header.setOnline(user.online);
            Context ctx = requireContext();
            Global.setAvatarShape(getContext(), view.findViewById(R.id.profile_avatar));
            Glide.with(ctx).load(
                    String.format("%s/photos_cache/account_avatar/avatar_%s",
                            ctx.getCacheDir().getAbsolutePath(), user.id))
                    .centerCrop().into((ImageView) view.findViewById(R.id.profile_avatar));
        }
    }
}
