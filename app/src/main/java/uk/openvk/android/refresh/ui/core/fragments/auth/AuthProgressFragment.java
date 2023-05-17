package uk.openvk.android.refresh.ui.core.fragments.auth;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;

public class AuthProgressFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.auth_progress, container, false);
        boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(
                requireContext()).getBoolean("dark_theme", false);
        if(Global.checkMonet(requireContext())) {
            int accentColor;
            MonetCompat monet = MonetCompat.getInstance();
            if (isDarkTheme) {
                accentColor = Global.getMonetIntColor(monet, "accent", 200);
            } else {
                accentColor = Global.getMonetIntColor(monet, "accent", 500);
            }
            ((ProgressBar) view.findViewById(R.id.auth_progress)).setProgressTintList(ColorStateList
                    .valueOf(accentColor));
        }
        return view;
    }
}
