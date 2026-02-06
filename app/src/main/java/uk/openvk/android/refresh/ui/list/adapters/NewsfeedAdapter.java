package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.OpenVKAPI;
import uk.openvk.android.refresh.api.attachments.Attachment;
import uk.openvk.android.refresh.api.attachments.PhotoAttachment;
import uk.openvk.android.refresh.api.attachments.VideoAttachment;
import uk.openvk.android.refresh.api.entities.Account;
import uk.openvk.android.refresh.api.entities.WallPost;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.refresh.ui.core.activities.ProfileIntentActivity;
import uk.openvk.android.refresh.ui.core.activities.VideoPlayerActivity;
import uk.openvk.android.refresh.ui.core.activities.WallPostActivity;
import uk.openvk.android.refresh.ui.view.layouts.PhotoAttachmentLayout;
import uk.openvk.android.refresh.ui.view.layouts.VideoAttachmentLayout;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {
    private final Context ctx;
    private ArrayList<WallPost> items;
    private FragmentManager fragman;
    private boolean photo_loaded = false;
    private boolean avatar_loaded = false;
    private final Account account;

    public NewsfeedAdapter(Context context, ArrayList<WallPost> posts, Account account) {
        this.account = account;
        ctx = context;
        items = posts;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_newsfeed, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        try {
            return items.size();
        } catch (Exception e) {
            return 0;
        }
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
        private final ShapeableImageView poster_avatar;
        private final TextView post_info;
        private final TextView post_text;
        private final TextView post_likes;
        private final TextView post_comments;
        private final TextView post_repost;
        private final ImageView verified_icon;
        private boolean likeAdded = false;
        private boolean likeDeleted = false;
        private int photoViewW;
        private int photoViewH;
        private int attachment_index;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.poster_avatar = view.findViewById(R.id.profile_avatar);
            this.poster_name = view.findViewById(R.id.post_author_label);
            this.post_info = view.findViewById(R.id.post_info);
            this.post_text = view.findViewById(R.id.post_text);
            this.post_likes = view.findViewById(R.id.like_btn);
            this.post_comments = view.findViewById(R.id.comment_btn);
            this.post_repost = view.findViewById(R.id.repost_btn);
            this.verified_icon = view.findViewById(R.id.verified_icon);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final WallPost item = getItem(position);
            poster_name.setText(item.name);
            post_info.setText(item.info);

            if(item.verified_author) {
                verified_icon.setVisibility(View.VISIBLE);
            } else {
                verified_icon.setVisibility(View.GONE);
            }

            if(!item.text.isEmpty()) {
                post_text.setVisibility(View.VISIBLE);
                String text = item.text.replaceAll("&lt;", "<")
                        .replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&")
                        .replaceAll("&quot;", "\"");
                post_text.setText(Global.formatLinksAsHtml(text));
            } else {
                post_text.setVisibility(View.GONE);
            }
            post_text.setMovementMethod(LinkMovementMethod.getInstance());

            poster_name.setTypeface(Global.getFlexibleTypeface(ctx, 500));
            post_likes.setText(String.format("%s", item.counters.likes));
            post_comments.setText(String.format("%s", item.counters.comments));
            post_repost.setText(String.format("%s", item.counters.reposts));

            setTheme(item);

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

            post_likes.setOnClickListener(view -> {
                if (item.counters.isLiked) {
                    if(!likeAdded) {
                        likeDeleted = true;
                    }
                    if (ctx instanceof ProfileIntentActivity) {
                        //((ProfileIntentActivity) ctx).deleteLike(position, "post", view);
                    } else if (ctx instanceof GroupIntentActivity) {
                        //((GroupIntentActivity) ctx).deleteLike(position, "post", view);
                    } else if (ctx instanceof AppActivity) {
                        deleteLike(item, position, "post", view);
                    }
                } else {
                    if(!likeDeleted) {
                        likeAdded = true;
                    }
                    if (ctx instanceof ProfileIntentActivity) {
                        //((ProfileIntentActivity) ctx).addLike(position, "post", view);
                    } else if (ctx instanceof GroupIntentActivity) {
                        //((GroupIntentActivity) ctx).addLike(position, "post", view);
                    } else if (ctx instanceof AppActivity) {
                        addLike(item, position, "post", view);
                    }
                }
            });

            post_comments.setOnClickListener(v -> openPostComments(item, ctx));

            try {
                boolean contains_photos = false;
                boolean contains_video = false;
                VideoAttachment video_attachment = null;
                PhotoAttachment photo_attachment = null;
                if(item.attachments != null && !item.attachments.isEmpty()) {
                    for(int pos = 0; pos < item.attachments.size(); pos++) {
                        Attachment attachment = item.attachments.get(pos);
                        if(attachment.type.equals("photo")) {
                            contains_photos = true;
                            photo_attachment = (PhotoAttachment) attachment.getContent();
                            attachment_index = pos;
                        } if(attachment.type.equals("video")) {
                            contains_video = true;
                            video_attachment = (VideoAttachment) attachment.getContent();
                        }
                    }
                }

                Global.setAvatarShape(ctx, convertView
                        .findViewById(R.id.profile_avatar));
                ((ShapeableImageView) convertView.findViewById(R.id.profile_avatar))
                        .setImageTintList(null);

                if(item.avatar != null)
                    poster_avatar.setImageBitmap(item.avatar);

                String local_avatar_frm;
                String local_photo_frm;

                View.OnClickListener openProfileListener = v -> openProfile(item);
                poster_avatar.setOnClickListener(openProfileListener);
                poster_name.setOnClickListener(openProfileListener);

                if(contains_photos) {
                    if(photo_loaded) {
                        ImageView photoView = ((PhotoAttachmentLayout) convertView
                                .findViewById(R.id.photo_attachment)).getImageView();
                        photoView.setImageTintList(null);
                        ViewTreeObserver viewTreeObserver = photoView.getViewTreeObserver();
                        if (viewTreeObserver.isAlive()) {
                            PhotoAttachment fPhotoAttach = photo_attachment;
                            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    photoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    if(fPhotoAttach.displayW == 0 && fPhotoAttach.displayH == 0) {
                                        fPhotoAttach.displayW = photoView.getLayoutParams().width;
                                        fPhotoAttach.displayH = photoView.getLayoutParams().height;
                                        Attachment attachment = item.attachments.get(attachment_index);
                                        attachment.setContent(fPhotoAttach);
                                        item.attachments.set(attachment_index, attachment);
                                        items.set(position, item);
                                    } else {
                                        photoView.getLayoutParams().width = fPhotoAttach.displayW;
                                        photoView.getLayoutParams().height = fPhotoAttach.displayH;
                                    }
                                }
                            });
                        }
                        (convertView.findViewById(R.id.photo_attachment)).setVisibility(View.VISIBLE);
                        PhotoAttachment finalPhoto_attachment = photo_attachment;
                        (convertView.findViewById(R.id.photo_attachment))
                                .setOnClickListener(view -> {
                                    Intent intent = new Intent(ctx, PhotoViewerActivity.class);
                                    intent.putExtra("attachment", finalPhoto_attachment);
                                    intent.putExtra("author_id", item.author_id);
                                    intent.putExtra("photo_id", finalPhoto_attachment.id);
                                    ctx.startActivity(intent);
                                });
                        ((PhotoAttachmentLayout) convertView.findViewById(R.id.photo_attachment))
                                .getImageView().setAdjustViewBounds(true);
                    }
                } else {
                    (convertView.findViewById(R.id.photo_attachment)).setVisibility(View.GONE);
                }

                if(contains_video) {
                    String video_player = PreferenceManager.getDefaultSharedPreferences(ctx)
                            .getString("video_player", "built_in");
                    ((VideoAttachmentLayout) convertView.findViewById(R.id.video_attachment))
                            .setAttachment(video_attachment);
                    (convertView.findViewById(R.id.video_attachment)).setVisibility(View.VISIBLE);
                    final int posFinal = position;
                    VideoAttachment finalVideo_attachment = video_attachment;
                    (convertView.findViewById(R.id.video_attachment))
                            .setOnClickListener(v -> {
                                Intent intent = new Intent(ctx, VideoPlayerActivity.class);
                                intent.putExtra("attachment", finalVideo_attachment);
                                intent.putExtra("files", finalVideo_attachment.files);
                                ctx.startActivity(intent);
                            });
                } else {
                    (convertView.findViewById(R.id.video_attachment)).setVisibility(View.GONE);
                }

            } catch (Exception ignored) {

            }
        }

        private void setTheme(WallPost item) {
            TypedValue accentColor = new TypedValue();
            ctx.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, accentColor,
                    true);
            int color;
            if(item.counters.isLiked) {
                color = MaterialColors.getColor(ctx, androidx.appcompat.R.attr.colorAccent,
                        Color.BLACK);
                post_likes.setSelected(true);
            } else {
                color = MaterialColors.getColor(ctx, androidx.appcompat.R.attr.colorControlNormal,
                        Color.BLACK);
                post_likes.setSelected(false);
            }
            post_likes.setTextColor(color);
            setTextViewDrawableColor(post_likes, color);

            setTextViewDrawableColor(post_repost, MaterialColors.getColor(ctx,
                    androidx.appcompat.R.attr.colorControlNormal, Color.BLACK));
            if(Global.checkMonet(ctx)) {
                if (Global.checkDarkTheme(ctx)) {
                    verified_icon.setImageTintList(
                            ColorStateList.valueOf(Global.getMonetIntColor(MonetCompat.getInstance(),
                                    "accent", 200)));
                } else {
                    verified_icon.setImageTintList(
                            ColorStateList.valueOf(Global.getMonetIntColor(MonetCompat.getInstance(),
                                    "accent", 500)));
                }
            }

        }
    }

    public void openProfile(WallPost post) {
        String url = "";
        if(post.author_id > 0) {
            url = String.format("openvk://profile/id%s", post.author_id);
        } else if(post.author_id < 0) {
            url = String.format("openvk://group/club%s", post.author_id);
        }
        if(!url.isEmpty()) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            final PackageManager pm = ctx.getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo>
                    activityList = pm.queryIntentActivities(i, 0);
            for (int index = 0; index < activityList.size(); index++) {
                ResolveInfo app = activityList.get(index);
                if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                    i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                }
            }
            ctx.startActivity(i);
        }
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    public void openPostComments(WallPost post, Context ctx) {
        Intent intent = new Intent(ctx, WallPostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("counters", post.counters);
        ctx.startActivity(intent);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void addLike(WallPost item, int position, String post, View view) {
        if(ctx instanceof AppActivity app_a) {
            OpenVKAPI ovk_api = app_a.ovk_api;
            if (app_a.selectedFragment == app_a.profileFragment) {
                app_a.profileFragment.wallSelect(position, "likes", "add");
            } else {
                app_a.newsfeedFragment.select(position, "likes", "add");
            }
            ovk_api.likes.add(ovk_api.wrapper, item.owner_id, item.post_id, position);
        }
    }

    public void deleteLike(WallPost item, int position, String post, View view) {
        if(ctx instanceof AppActivity app_a) {
            OpenVKAPI ovk_api = app_a.ovk_api;
            if (app_a.selectedFragment == app_a.profileFragment) {
                app_a.profileFragment.wallSelect(position, "likes", "delete");
            } else {
                app_a.newsfeedFragment.select(position, "likes", "delete");
            }
            ovk_api.likes.delete(ovk_api.wrapper, item.owner_id, item.post_id, position);
        }
    }
}