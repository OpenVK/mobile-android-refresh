package uk.openvk.android.refresh.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;

public class ProgressLayout extends LinearLayoutCompat {

    public ProgressLayout(Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.progress, null);

        this.addView(view);
        LinearLayoutCompat.LayoutParams layoutParams = (LinearLayoutCompat.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        if(Global.checkMonet(getContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((CircularProgressIndicator) findViewById(R.id.progressBar)).setIndicatorColor(Objects.requireNonNull(
                        monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
            } else {
                ((CircularProgressIndicator) findViewById(R.id.progressBar)).setIndicatorColor(Objects.requireNonNull(
                        monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
            }
        }
    }

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.progress, null);

        this.addView(view);
        LinearLayoutCompat.LayoutParams layoutParams = (LinearLayoutCompat.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        if(Global.checkMonet(getContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((CircularProgressIndicator) findViewById(R.id.progressBar)).setIndicatorColor(Objects.requireNonNull(
                        monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
            } else {
                ((CircularProgressIndicator) findViewById(R.id.progressBar)).setIndicatorColor(Objects.requireNonNull(
                        monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
            }
        }
    }
}
