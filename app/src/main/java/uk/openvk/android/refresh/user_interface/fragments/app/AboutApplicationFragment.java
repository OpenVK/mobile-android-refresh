package uk.openvk.android.refresh.user_interface.fragments.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.imageview.ShapeableImageView;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Objects;

import uk.openvk.android.refresh.BuildConfig;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;

public class AboutApplicationFragment extends Fragment {
    private View view;
    private SharedPreferences global_prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.about_app_fragment, container, false);
        ((ShapeableImageView) view.findViewById(R.id.ovk_logo)).setImageTintList(null);
        ((TextView) view.findViewById(R.id.version_subtitle)).setText(getResources().getString(R.string.version_subtitle, BuildConfig.VERSION_NAME));
        ((TextView) view.findViewById(R.id.app_license)).setMovementMethod(LinkMovementMethod.getInstance());
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((Button) view.findViewById(R.id.source_code_btn)).setTextColor(ColorStateList.valueOf(Objects.requireNonNull(monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8()));
            } else {
                ((Button) view.findViewById(R.id.source_code_btn)).setTextColor(ColorStateList.valueOf(Objects.requireNonNull(monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8()));
            }
        }
        ((Button) view.findViewById(R.id.source_code_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/tretdm/openvk-refresh"));
                startActivity(i);
            }
        });
        return view;
    }
}
