package uk.openvk.android.refresh.user_interface.layouts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.kieronquinn.monetcompat.core.MonetCompat;

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
            ((CircularProgressIndicator) findViewById(R.id.progressBar)).setIndicatorColor(monet.getAccentColor(getContext(), isDarkTheme));
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
            ((CircularProgressIndicator) findViewById(R.id.progressBar)).setIndicatorColor(monet.getAccentColor(getContext(), isDarkTheme));
        }
    }
}
