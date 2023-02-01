package uk.openvk.android.refresh.user_interface.list_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Message;
import uk.openvk.android.refresh.user_interface.GlideApp;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.Holder>  {
    private Context ctx;
    private ArrayList<Message> items;

    public MessagesAdapter(Context context, ArrayList<Message> items, long peer_id) {
        this.ctx = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MessagesAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            return new MessagesAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.incoming_msg, parent, false));
        } else if(viewType == 1) {
            return new MessagesAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.outcoming_msg, parent, false));
        } else {
            return new MessagesAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.messages_history_datestamp, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView msg_text;
        private final TextView msg_timestamp;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.msg_text = (TextView) view.findViewById(R.id.msg_text);
            this.msg_timestamp = (TextView) view.findViewById(R.id.timestamp);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Message item = getItem(position);
            if(item.type < 2) {
                msg_text.setText(item.text);
                msg_timestamp.setText(item.timestamp);
                if(item.type == 1) {
                    if(item.isError) {
                        ((ImageView) convertView.findViewById(R.id.error_image)).setVisibility(View.VISIBLE);
                    } else {
                        ((ImageView) convertView.findViewById(R.id.error_image)).setVisibility(View.GONE);
                    }
                    if(item.sending) {
                        ((ProgressBar) convertView.findViewById(R.id.sending_progress)).setVisibility(View.VISIBLE);
                    } else {
                        ((ProgressBar) convertView.findViewById(R.id.sending_progress)).setVisibility(View.GONE);
                    }
                } else {
                    Global.setAvatarShape(ctx, convertView.findViewById(R.id.companion_avatar));
                    ((ImageView) convertView.findViewById(R.id.companion_avatar)).setImageTintList(null);
                    GlideApp.with(ctx)
                            .load(String.format("%s/photos_cache/friend_avatars/avatar_%s", ctx.getCacheDir().getAbsolutePath(), item.id))
                            .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                            .centerCrop()
                            .into((ImageView) convertView.findViewById(R.id.companion_avatar));
                }
            } else {
                msg_text.setText(item.text);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ((Message) getItem(position)).type;
    }

    private Message getItem(int position) {
        return items.get(position);
    }
}
