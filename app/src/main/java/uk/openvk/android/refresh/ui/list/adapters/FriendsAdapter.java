package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.color.MaterialColors;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Friend;
import uk.openvk.android.refresh.ui.util.glide.GlideApp;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.Holder>  {
    private Context ctx;
    private ArrayList<Friend> items;

    public FriendsAdapter(Context context, ArrayList<Friend> items) {
        this.ctx = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FriendsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendsAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_friends, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView friend_title;
        private final ImageView verified_icon;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.friend_title = (TextView) view.findViewById(R.id.friend_title);
            this.verified_icon = (ImageView) view.findViewById(R.id.verified_icon);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Friend item = getItem(position);
            friend_title.setText(String.format("%s %s", item.first_name, item.last_name));
            friend_title.setTypeface(Global.getFlexibleTypeface(ctx, 500));
            Global.setAvatarShape(ctx, convertView.findViewById(R.id.friend_avatar));
            ((ImageView) convertView.findViewById(R.id.friend_avatar)).setImageTintList(null);
            GlideApp.with(ctx)
                    .load(String.format("%s/photos_cache/friend_avatars/avatar_%s",
                            ctx.getCacheDir().getAbsolutePath(), item.id))
                    .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontAnimate().centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.friend_avatar));
            verified_icon.setFocusable(false);
            convertView.findViewById(R.id.friend_avatar).setFocusable(false);
            friend_title.setFocusable(false);
            if(item.verified) {
                verified_icon.setVisibility(View.VISIBLE);
            } else {
                verified_icon.setVisibility(View.GONE);
            }
            View.OnClickListener openProfileListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openProfile(item);
                }
            };
            setTheme(convertView);
            convertView.setOnClickListener(openProfileListener);
        }
    }

    private void setTheme(View view) {
        if(Global.checkMonet(ctx)) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(ctx)
                    .getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((ImageView) view.findViewById(R.id.verified_icon)).setImageTintList(ColorStateList.valueOf(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1()
                                .get(200)).toLinearSrgb().toSrgb().quantize8()));
            } else {
                ((ImageView) view.findViewById(R.id.verified_icon)).setImageTintList(ColorStateList.valueOf(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1()
                                .get(500)).toLinearSrgb().toSrgb().quantize8()));
            }
        } else {
            ((ImageView)view.findViewById(R.id.verified_icon)).setImageTintList(
                    ColorStateList.valueOf(MaterialColors.getColor(ctx,
                            androidx.appcompat.R.attr.colorAccent, Color.BLACK)));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private Friend getItem(int position) {
        return items.get(position);
    }

    public void openProfile(Friend friend) {
        String url = "";
        url = String.format("openvk://profile/id%s", friend.id);
        if(url.length() > 0) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            final PackageManager pm = ctx.getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> activityList =
                    pm.queryIntentActivities(i, 0);
            for (int index = 0; index < activityList.size(); index++) {
                ResolveInfo app = activityList.get(index);
                if (app.activityInfo.name.contains("uk.openvk.android.refresh")) {
                    i.setClassName(app.activityInfo.packageName, app.activityInfo.name);
                }
            }
            ctx.startActivity(i);
        }
    }
}
