package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.refresh.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.refresh.ui.util.glide.GlideApp;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.Holder>  {
    private final Fragment parent;
    private Context ctx;
    private ArrayList<Friend> items;

    public FriendRequestsAdapter(Context context, ArrayList<Friend> items, Fragment parent) {
        this.parent = parent;
        this.ctx = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FriendRequestsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestsAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_friend_requests, parent, false));
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
        private final MaterialButton add_btn;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.friend_title = (TextView) view.findViewById(R.id.friend_title);
            this.add_btn = (MaterialButton) view.findViewById(R.id.add_button);
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

            convertView.findViewById(R.id.friend_avatar).setFocusable(false);
            friend_title.setFocusable(false);
            convertView.findViewById(R.id.friend_avatar).setFocusable(false);
            View.OnClickListener openProfileListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).openProfileFromFriends(position, true);
                    } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                        ((FriendsIntentActivity) ctx).openProfileFromFriends(position);
                    }
                }
            };
            add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((FriendsFragment) parent).requests_cursor_index = position;
                        ((AppActivity) ctx).addToFriends(item.id);
                    }
                }
            });
            setTheme(convertView);
            convertView.findViewById(R.id.card).setOnClickListener(openProfileListener);
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
