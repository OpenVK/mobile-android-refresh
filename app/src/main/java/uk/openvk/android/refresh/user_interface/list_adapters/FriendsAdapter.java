package uk.openvk.android.refresh.user_interface.list_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Friend;
import uk.openvk.android.refresh.user_interface.GlideApp;

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
        return new FriendsAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.friend_list_item, parent, false));
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

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.friend_title = (TextView) view.findViewById(R.id.friend_title);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Friend item = getItem(position);
            friend_title.setText(String.format("%s %s", item.first_name, item.last_name));
            Global.setAvatarShape(ctx, convertView.findViewById(R.id.friend_avatar));
            ((ImageView) convertView.findViewById(R.id.friend_avatar)).setImageTintList(null);
            GlideApp.with(ctx)
                    .load(String.format("%s/photos_cache/friend_avatars/avatar_%s", ctx.getCacheDir().getAbsolutePath(), item.id))
                    .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                    .centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.friend_avatar));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private Friend getItem(int position) {
        return items.get(position);
    }
}
