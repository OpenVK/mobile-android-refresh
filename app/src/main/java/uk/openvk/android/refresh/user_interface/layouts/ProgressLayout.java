package uk.openvk.android.refresh.user_interface.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.LinearLayoutCompat;

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
    }
}
