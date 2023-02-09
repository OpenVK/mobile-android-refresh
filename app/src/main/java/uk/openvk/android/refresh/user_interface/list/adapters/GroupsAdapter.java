package uk.openvk.android.refresh.user_interface.list.adapters;

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
import uk.openvk.android.refresh.api.models.Group;
import uk.openvk.android.refresh.user_interface.util.glide.GlideApp;
import uk.openvk.android.refresh.user_interface.core.activities.AppActivity;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.Holder>  {
    private Context ctx;
    private ArrayList<Group> items;

    public GroupsAdapter(Context context, ArrayList<Group> items) {
        this.ctx = context;
        this.items = items;
    }

    @NonNull
    @Override
    public GroupsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupsAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.group_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView group_title;
        private final TextView group_summary;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.group_title = (TextView) view.findViewById(R.id.group_title);
            this.group_summary = (TextView) view.findViewById(R.id.group_summary);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Group item = getItem(position);
            group_title.setText(item.name);
            group_title.setTypeface(Global.getFlexibleTypeface(ctx, 500));
            if(item.members_count > 0) {
                group_summary.setText(String.format(ctx.getResources().getStringArray(R.array.members_count)[2], item.members_count));
            }
            Global.setAvatarShape(ctx, convertView.findViewById(R.id.group_avatar));
            ((ImageView) convertView.findViewById(R.id.group_avatar)).setImageTintList(null);
            GlideApp.with(ctx)
                    .load(String.format("%s/photos_cache/group_avatars/avatar_%s", ctx.getCacheDir().getAbsolutePath(), item.id))
                    .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontAnimate().centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.group_avatar));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).openCommunityPage(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private Group getItem(int position) {
        return items.get(position);
    }
}
