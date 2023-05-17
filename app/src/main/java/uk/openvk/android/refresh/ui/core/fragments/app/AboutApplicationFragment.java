package uk.openvk.android.refresh.ui.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Objects;

import uk.openvk.android.refresh.BuildConfig;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;

public class AboutApplicationFragment extends Fragment {
    private View view;
    private SharedPreferences global_prefs;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.fragment_about_app, container, false);
        ((ShapeableImageView) view.findViewById(R.id.ovk_logo)).setImageTintList(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((TextView) view.findViewById(R.id.version_subtitle)).setText(
                    Html.fromHtml(String.format(getResources().getString(R.string.version_subtitle),
                            BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT),
                            Html.FROM_HTML_MODE_COMPACT));
        } else {
            ((TextView) view.findViewById(R.id.version_subtitle)).setText(
                    Html.fromHtml(String.format(getResources().getString(R.string.version_subtitle),
                            BuildConfig.VERSION_NAME, BuildConfig.GITHUB_COMMIT)));
        }
        ((TextView) view.findViewById(R.id.app_license)).setMovementMethod(LinkMovementMethod.getInstance());
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((Button) view.findViewById(R.id.source_code_btn)).setTextColor(
                        Global.getMonetIntColor(monet, "accent", 200));
                ((MaterialButton) view.findViewById(R.id.source_code_btn))
                        .setRippleColor(
                                ColorStateList.valueOf(
                                        Global.getMonetIntColor(monet, "accent", 500)
                                )
                        );
            } else {
                ((Button) view.findViewById(R.id.source_code_btn)).setTextColor(
                        Global.getMonetIntColor(monet, "accent", 500)
                );
            }
            ((MaterialButton) view.findViewById(R.id.source_code_btn))
                    .setRippleColor(
                            Global.getMonetRippleColorList(monet, 0, isDarkTheme, 1)
                    );

        }
        ((Button) view.findViewById(R.id.source_code_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/openvk/mobile-android-refresh"));
                startActivity(i);
            }
        });
        return view;
    }
}
