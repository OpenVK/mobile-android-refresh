package uk.openvk.android.refresh.ui.core.listeners;

import uk.openvk.android.refresh.ui.view.InfinityRecyclerView;
import uk.openvk.android.refresh.ui.view.InfinityScrollView;

public interface OnRecyclerScrollListener {
    void onScroll(InfinityRecyclerView infinityRecyclerView, int x, int y, int old_x, int old_y);
}
