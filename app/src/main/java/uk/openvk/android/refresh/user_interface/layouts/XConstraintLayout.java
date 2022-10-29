package uk.openvk.android.refresh.user_interface.layouts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.constraintlayout.widget.ConstraintLayout;

import uk.openvk.android.refresh.user_interface.listeners.OnKeyboardStateListener;

public class XConstraintLayout extends ConstraintLayout {
    private OnKeyboardStateListener listener;

    public XConstraintLayout(Context context) {
        super(context);
    }

    public XConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnKeyboardStateListener(final OnKeyboardStateListener listener) {
        this.listener = listener;
        final int MIN_KEYBOARD_HEIGHT_PX = 150;
        final View decorView = ((Activity)getContext()).getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int lastVisibleDecorViewHeight;

            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        onKeyboardStateChanged(true);
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        onKeyboardStateChanged(false);
                    }
                }
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        });
    }

    public void onKeyboardStateChanged(boolean param1Boolean) {
        this.listener.onKeyboardStateChanged(param1Boolean);
    }
}
