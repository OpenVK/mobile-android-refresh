package uk.openvk.android.refresh.ui.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.attachments.VideoAttachment;

public class VideoAttachmentLayout extends ConstraintLayout {

    private View view;
    private VideoAttachment attachment;

    public VideoAttachmentLayout(Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attachment_video, null);

        this.addView(view);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public VideoAttachmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attachment_video, null);

        this.addView(view);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public ImageView getImageView() {
        return ((ImageView) view.findViewById(R.id.thumbnail_view));
    }

    @SuppressLint("DefaultLocale")
    public void setAttachment(VideoAttachment attachment) {
        this.attachment = attachment;
        if(attachment != null) {
            ((TextView) findViewById(R.id.video_title)).setText(attachment.title);
            if (attachment.duration >= 3600) {
                ((TextView) findViewById(R.id.video_duration)).setText(String.format("%d:%02d:%02d", attachment.duration / 3600, (attachment.duration % 3600) / 60, (attachment.duration % 60)));
            } else {
                ((TextView) findViewById(R.id.video_duration)).setText(String.format("%d:%02d", attachment.duration / 60, (attachment.duration % 60)));
            }
        }
    }
}
