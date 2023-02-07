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
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.models.Conversation;
import uk.openvk.android.refresh.user_interface.GlideApp;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.Holder>  {
    private final Account account;
    private Context ctx;
    private ArrayList<Conversation> items;

    public ConversationsAdapter(Context context, ArrayList<Conversation> items, Account account) {
        this.ctx = context;
        this.items = items;
        this.account = account;
    }

    @NonNull
    @Override
    public ConversationsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationsAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.conversation_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView conversation_title;
        private final TextView conversation_time;
        private final TextView last_msg_text;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.conversation_title = (TextView) view.findViewById(R.id.conversation_title);
            this.conversation_time = (TextView) view.findViewById(R.id.conversation_time);
            this.last_msg_text = (TextView) view.findViewById(R.id.last_message_text);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Conversation item = getItem(position);
            conversation_title.setText(item.title);
            conversation_title.setTypeface(Global.getFlexibleTypeface(ctx, 500));
            conversation_time.setText("");
            if(account.id == item.lastMsgAuthorId) {
                last_msg_text.setText(ctx.getResources().getString(R.string.your_last_message, item.lastMsgText));
            } else {
                last_msg_text.setText(item.lastMsgText);
            }
            Global.setAvatarShape(ctx, convertView.findViewById(R.id.conv_avatar));
            ((ImageView) convertView.findViewById(R.id.conv_avatar)).setImageTintList(null);
            GlideApp.with(ctx)
                    .load(String.format("%s/photos_cache/conversations_avatars/avatar_%s", ctx.getCacheDir().getAbsolutePath(), item.peer_id))
                    .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                    .centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.conv_avatar));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).openConversation(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private Conversation getItem(int position) {
        return items.get(position);
    }
}
