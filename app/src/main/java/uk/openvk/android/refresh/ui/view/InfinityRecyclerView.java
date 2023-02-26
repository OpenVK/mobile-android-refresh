package uk.openvk.android.refresh.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import androidx.recyclerview.widget.RecyclerView;

import uk.openvk.android.refresh.ui.core.listeners.OnRecyclerScrollListener;
import uk.openvk.android.refresh.ui.core.listeners.OnScrollListener;

public class InfinityRecyclerView extends RecyclerView {

    private OnRecyclerScrollListener onScrollListener;

    public InfinityRecyclerView(Context context) {
        super(context);
    }

    public InfinityRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnScrollListener(OnRecyclerScrollListener scrollListener) {
        this.onScrollListener = scrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(onScrollListener != null) {
            onScrollListener.onScroll(this, l, t, oldl, oldt);
        }
    }
}