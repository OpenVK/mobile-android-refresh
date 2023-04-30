package uk.openvk.android.refresh.ui.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import uk.openvk.android.refresh.R;

public class ErrorLayout extends LinearLayoutCompat {
    public ErrorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("InflateParams") View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_error, null);
        this.addView(view);
        LinearLayoutCompat.LayoutParams layoutParams = (LinearLayoutCompat.LayoutParams)
                view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void setRetryButtonClickListener(OnClickListener listener) {
        findViewById(R.id.retry_btn).setOnClickListener(listener);
    }
}
