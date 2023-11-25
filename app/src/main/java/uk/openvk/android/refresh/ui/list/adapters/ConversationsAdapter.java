package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Account;
import uk.openvk.android.refresh.api.entities.Conversation;
import uk.openvk.android.refresh.ui.core.activities.ConversationActivity;
import uk.openvk.android.refresh.ui.util.glide.GlideApp;

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
        return new ConversationsAdapter.Holder(LayoutInflater.from(ctx).inflate(
                R.layout.list_item_conversations, parent, false));
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
                    .load(String.format("%s/photos_cache/conversations_avatars/avatar_%s",
                            ctx.getCacheDir().getAbsolutePath(), item.peer_id))
                    .error(ctx.getResources().getDrawable(R.drawable.circular_avatar))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontAnimate().centerCrop()
                    .into((ImageView) convertView.findViewById(R.id.conv_avatar));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        openConversation(item);
                    }
                }
            });
        }
    }

    public void openConversation(Conversation conv) {
        Intent intent = new Intent(ctx, ConversationActivity.class);
        try {
            intent.putExtra("peer_id", conv.peer_id);
            intent.putExtra("conv_title", conv.title);
            intent.putExtra("online", conv.online);
            ctx.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
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
