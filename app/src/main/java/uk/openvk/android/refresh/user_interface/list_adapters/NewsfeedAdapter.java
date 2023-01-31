package uk.openvk.android.refresh.user_interface.list_adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.attachments.Attachment;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.user_interface.layouts.PhotoAttachmentLayout;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {
    private final Context ctx;
    private final ArrayList<WallPost> items;
    private FragmentManager fragman;

    public NewsfeedAdapter(Context context, ArrayList<WallPost> posts) {
        ctx = context;
        items = posts;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.wall_post, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public WallPost getItem(int position) {
        return items.get(position);
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView poster_name;
        private final TextView post_info;
        private final TextView post_text;
        private final TextView post_likes;
        private final TextView post_comments;
        private final TextView post_repost;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.poster_name = (TextView) view.findViewById(R.id.post_author_label);
            this.post_info = (TextView) view.findViewById(R.id.post_info);
            this.post_text = (TextView) view.findViewById(R.id.post_text);
            this.post_likes = (TextView) view.findViewById(R.id.like_btn);
            this.post_comments = (TextView) view.findViewById(R.id.comment_btn);
            this.post_repost = (TextView) view.findViewById(R.id.repost_btn);
        }

        @SuppressLint("SimpleDateFormat")
        void bind(final int position) {
            final WallPost item = getItem(position);
            poster_name.setText(item.name);
            Date dt = new Date(TimeUnit.SECONDS.toMillis(item.dt_sec));
            Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dt_midnight);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(item.dt_sec))) < 86400000) {
                item.info = String.format(ctx.getResources().getStringArray(R.array.date_differences)[1], new SimpleDateFormat("HH:mm").format(dt));
            } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(item.dt_sec))) < (86400000 * 2)) {
                item.info = String.format(ctx.getResources().getStringArray(R.array.date_differences)[2], new SimpleDateFormat("HH:mm").format(dt));
            } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(item.dt_sec))) < 31536000000L) {
                item.info = String.format(ctx.getResources().getStringArray(R.array.date_differences)[3], new SimpleDateFormat("d MMMM").format(dt),
                        new SimpleDateFormat("HH:mm").format(dt));
            } else {
                item.info = String.format(ctx.getResources().getStringArray(R.array.date_differences)[3], new SimpleDateFormat("d MMMM yyyy").format(dt),
                        new SimpleDateFormat("HH:mm").format(dt));
            }
            post_info.setText(item.info);
            if(item.text.length() > 0) {
                post_text.setVisibility(View.VISIBLE);
                String text = item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                post_text.setText(text);
            } else {
                post_text.setVisibility(View.GONE);
            }
            post_likes.setText(String.format("%s", item.counters.likes));
            post_comments.setText(String.format("%s", item.counters.comments));
            post_repost.setText(String.format("%s", item.counters.reposts));
            try {
                boolean contains_photos = false;
                if(item.attachments != null && item.attachments.size() > 0) {
                    for(int pos = 0; pos < item.attachments.size(); pos++) {
                        Attachment attachment = item.attachments.get(pos);
                        if(attachment.type.equals("photo")) {
                            contains_photos = true;
                        }
                    }
                }
                Glide.with(ctx).load(String.format("%s/photos_cache/newsfeed_avatars/avatar_%s", ctx.getCacheDir().getAbsolutePath(), item.owner_id))
                        .dontAnimate().diskCacheStrategy(DiskCacheStrategy.DATA).centerCrop().error(R.drawable.circular_avatar).into((ShapeableImageView) convertView.findViewById(R.id.profile_avatar));
                ((ShapeableImageView) convertView.findViewById(R.id.profile_avatar)).setImageTintList(null);
                if(contains_photos) {
                    ((ImageView) ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).getImageView()).setImageTintList(null);
                    Glide.with(ctx).load(String.format("%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s", ctx.getCacheDir().getAbsolutePath(), item.owner_id, item.post_id))
                            .dontAnimate().diskCacheStrategy(DiskCacheStrategy.DATA).error(R.drawable.warning).into((ImageView) ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).getImageView());
                    ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).setVisibility(View.VISIBLE);
                    ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                } else {
                    ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).setVisibility(View.GONE);
                }

            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
