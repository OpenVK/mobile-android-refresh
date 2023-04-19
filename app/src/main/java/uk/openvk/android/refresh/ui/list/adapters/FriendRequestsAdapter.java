package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.refresh.ui.util.glide.GlideApp;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.Holder>  {
    private Context ctx;
    private ArrayList<Friend> items;

    public FriendRequestsAdapter(Context context, ArrayList<Friend> items) {
        this.ctx = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FriendRequestsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestsAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView friend_title;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.friend_title = (TextView) view.findViewById(R.id.conversation_title);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Friend item = getItem(position);
            friend_title.setText(String.format("%s %s", item.first_name, item.last_name));
            friend_title.setTypeface(Global.getFlexibleTypeface(ctx, 500));
            Global.setAvatarShape(ctx, convertView.findViewById(R.id.friend_avatar));
            ((ImageView) convertView.findViewById(R.id.friend_avatar)).setImageTintList(null);
            GlideApp.with(ctx)
                    .load(String.format("%s/photos_cache/friend_avatars/avatar_%s", ctx.getCacheDir().getAbsolutePath(), item.id))
                    .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontAnimate().centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.friend_avatar));
            View.OnClickListener openProfileListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).openProfileFromFriends(position);
                    } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                        ((FriendsIntentActivity) ctx).openProfileFromFriends(position);
                    }
                }
            };
            setTheme(convertView);
            ((ImageView) convertView.findViewById(R.id.friend_avatar)).setOnClickListener(openProfileListener);
            friend_title.setOnClickListener(openProfileListener);
        }
    }

    private void setTheme(View view) {
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private Friend getItem(int position) {
        return items.get(position);
    }
}
