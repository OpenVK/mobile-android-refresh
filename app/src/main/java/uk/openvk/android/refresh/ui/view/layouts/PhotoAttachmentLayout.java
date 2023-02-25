package uk.openvk.android.refresh.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import uk.openvk.android.refresh.R;

public class PhotoAttachmentLayout extends ConstraintLayout {

    private View view;

    public PhotoAttachmentLayout(Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attachment_photo, null);

        this.addView(view);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    public PhotoAttachmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attachment_photo, null);

        this.addView(view);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    public ImageView getImageView() {
        return ((ImageView) view.findViewById(R.id.thumbnail_view));
    }
}
