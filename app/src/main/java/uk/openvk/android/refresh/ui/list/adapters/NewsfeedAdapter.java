package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.attachments.Attachment;
import uk.openvk.android.refresh.api.attachments.VideoAttachment;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.VideoPlayerActivity;
import uk.openvk.android.refresh.ui.view.layouts.PhotoAttachmentLayout;
import uk.openvk.android.refresh.ui.view.layouts.VideoAttachmentLayout;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {
    private final Context ctx;
    private ArrayList<WallPost> items;
    private FragmentManager fragman;
    private boolean photo_loaded = false;
    private boolean avatar_loaded = false;
    private Account account;

    public NewsfeedAdapter(Context context, ArrayList<WallPost> posts, Account account) {
        this.account = account;
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

    public void setArray(ArrayList<WallPost> wallPosts) {
        items = wallPosts;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView poster_name;
        private final TextView post_info;
        private final TextView post_text;
        private final TextView post_likes;
        private final TextView post_comments;
        private final TextView post_repost;
        private boolean likeAdded = false;
        private boolean likeDeleted = false;

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

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
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
            if((calendar.getTimeInMillis() - (TimeUnit.SECONDS.toMillis(item.dt_sec))) < 86400000) {
                item.info = String.format(ctx.getResources().getStringArray(R.array.date_differences)[1], new SimpleDateFormat("HH:mm").format(dt));
            } else if((calendar.getTimeInMillis() - (TimeUnit.SECONDS.toMillis(item.dt_sec))) < (86400000 * 2)) {
                item.info = String.format(ctx.getResources().getStringArray(R.array.date_differences)[2], new SimpleDateFormat("HH:mm").format(dt));
            } else if((calendar.getTimeInMillis() - (TimeUnit.SECONDS.toMillis(item.dt_sec))) < 31536000000L) {
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
                post_text.setText(Global.formatLinksAsHtml(text));
            } else {
                post_text.setVisibility(View.GONE);
            }
            post_text.setMovementMethod(LinkMovementMethod.getInstance());

            poster_name.setTypeface(Global.getFlexibleTypeface(ctx, 500));
            post_likes.setText(String.format("%s", item.counters.likes));
            post_comments.setText(String.format("%s", item.counters.comments));
            post_repost.setText(String.format("%s", item.counters.reposts));

            TypedValue accentColor = new TypedValue();
            ctx.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, accentColor, true);
            int color;
            if(item.counters.isLiked) {
                color = MaterialColors.getColor(ctx, androidx.appcompat.R.attr.colorAccent, Color.BLACK);
                post_likes.setSelected(true);
            } else {
                color = MaterialColors.getColor(ctx, androidx.appcompat.R.attr.colorControlNormal, Color.BLACK);
                post_likes.setSelected(false);
            }
            post_likes.setTextColor(color);
            setTextViewDrawableColor(post_likes, color);

            setTextViewDrawableColor(post_repost, MaterialColors.getColor(ctx, androidx.appcompat.R.attr.colorControlNormal, Color.BLACK));

            if(item.counters.enabled) {
                post_likes.setEnabled(true);
                if(item.counters.isLiked && likeAdded) {
                    post_likes.setText(String.format("%s", (item.counters.likes + 1)));
                } else if(!item.counters.isLiked && likeDeleted) {
                    post_likes.setText(String.format("%s", (item.counters.likes - 1)));
                } else {
                    post_likes.setText(String.format("%s", item.counters.likes));
                }
            } else {
                post_likes.setEnabled(false);
            }

            post_likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.counters.isLiked) {
                        if(!likeAdded) {
                            likeDeleted = true;
                        }
                        if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            //((ProfileIntentActivity) ctx).deleteLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            //((GroupIntentActivity) ctx).deleteLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).deleteLike(position, "post", view);
                        }
                    } else {
                        if(!likeDeleted) {
                            likeAdded = true;
                        }
                        if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            //((ProfileIntentActivity) ctx).addLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            //((GroupIntentActivity) ctx).addLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).addLike(position, "post", view);
                        }
                    }
                }
            });

            post_comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Global.openPostComments(account, item, ctx);
                }
            });

            try {
                boolean contains_photos = false;
                boolean contains_video = false;
                VideoAttachment video_attachment = null;
                if(item.attachments != null && item.attachments.size() > 0) {
                    for(int pos = 0; pos < item.attachments.size(); pos++) {
                        Attachment attachment = item.attachments.get(pos);
                        if(attachment.type.equals("photo")) {
                            contains_photos = true;
                        } if(attachment.type.equals("video")) {
                            contains_video = true;
                            video_attachment = (VideoAttachment) attachment.getContent();
                        }
                    }
                }
                Global.setAvatarShape(ctx, ((ShapeableImageView) convertView.findViewById(R.id.profile_avatar)));
                String local_avatar_frm;
                String local_photo_frm;
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    if(((AppActivity) ctx).getSelectedFragment() != null &&
                            ((AppActivity) ctx).getSelectedFragment().getClass().getSimpleName().equals("NewsfeedFragment")) {
                        local_avatar_frm = "%s/photos_cache/newsfeed_avatars/avatar_%s";
                        local_photo_frm = "%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s";
                    } else {
                        local_avatar_frm = "%s/photos_cache/wall_avatars/avatar_%s";
                        local_photo_frm = "%s/photos_cache/wall_photo_attachments/wall_attachment_o%sp%s";
                    }
                } else {
                    local_avatar_frm = "%s/photos_cache/wall_avatars/avatar_%s";
                    local_photo_frm = "%s/photos_cache/wall_photo_attachments/wall_attachment_o%sp%s";
                }
                if(avatar_loaded)
                    Glide.with(ctx).load(String.format(local_avatar_frm, ctx.getCacheDir().getAbsolutePath(), item.author_id))
                            .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                            .dontAnimate().centerCrop().error(R.drawable.circular_avatar).into((ShapeableImageView) convertView.findViewById(R.id.profile_avatar));
                ((ShapeableImageView) convertView.findViewById(R.id.profile_avatar)).setImageTintList(null);
                View.OnClickListener openProfileListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).openProfileFromWall(position);
                        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).openProfileFromWall(position);
                        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).openProfileFromWall(position);
                        }
                    }
                };
                ((ShapeableImageView) convertView.findViewById(R.id.profile_avatar)).setOnClickListener(openProfileListener);
                poster_name.setOnClickListener(openProfileListener);
                if(contains_photos) {
                    if(photo_loaded) {
                        (((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).getImageView()).setImageTintList(null);
                        Glide.with(ctx).load(String.format(local_photo_frm, ctx.getCacheDir().getAbsolutePath(), item.owner_id, item.post_id))
                                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                                .placeholder(ctx.getDrawable(R.drawable.photo_placeholder))
                                .dontAnimate().error(R.drawable.warning)
                                .into(((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).getImageView());
                        (convertView.findViewById(R.id.photo_attachment)).setVisibility(View.VISIBLE);
                        (convertView.findViewById(R.id.photo_attachment)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                        ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment)).getImageView().setAdjustViewBounds(true);
                    }
                } else {
                    (convertView.findViewById(R.id.photo_attachment)).setVisibility(View.GONE);
                }
                if(contains_video) {
                    ((VideoAttachmentLayout) convertView.findViewById(R.id.video_attachment)).setAttachment(video_attachment);
                    (convertView.findViewById(R.id.video_attachment)).setVisibility(View.VISIBLE);
                    final int posFinal = position;
                    VideoAttachment finalVideo_attachment = video_attachment;
                    (convertView.findViewById(R.id.video_attachment)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ctx, VideoPlayerActivity.class);
                            intent.putExtra("attachment", finalVideo_attachment);
                            intent.putExtra("files", finalVideo_attachment.files);
                            ctx.startActivity(intent);
                        }
                    });
                } else {
                    (convertView.findViewById(R.id.video_attachment)).setVisibility(View.GONE);
                }

            } catch (Exception ignored) {

            }
        }
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPhotoLoadState(boolean value) {
        this.photo_loaded = value;
        //notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAvatarLoadState(boolean value) {
        this.avatar_loaded = value;
        //notifyDataSetChanged();
    }
}
