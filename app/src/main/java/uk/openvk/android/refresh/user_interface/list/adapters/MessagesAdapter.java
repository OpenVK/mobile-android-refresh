package uk.openvk.android.refresh.user_interface.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.Objects;

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
        private final TextView msg_text_2;
        private final TextView msg_timestamp;
        private final TextView msg_timestamp_2;
        private final LinearLayout horizontal_layout;
        private final LinearLayout vertical_layout;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.horizontal_layout = (LinearLayout) view.findViewById(R.id.text_layout_horizontal);
            this.vertical_layout = (LinearLayout) view.findViewById(R.id.text_layout_vertical);
            this.msg_text = (TextView) view.findViewById(R.id.msg_text);
            this.msg_timestamp = (TextView) view.findViewById(R.id.timestamp);
            this.msg_text_2 = (TextView) view.findViewById(R.id.msg_text_2);
            this.msg_timestamp_2 = (TextView) view.findViewById(R.id.timestamp_vertical);
        }

        @SuppressLint({"SimpleDateFormat", "UseCompatLoadingForDrawables"})
        void bind(final int position) {
            final Message item = getItem(position);
            if(item.type < 2) {
                if(item.text.length() < 20) {
                    vertical_layout.setVisibility(View.GONE);
                    horizontal_layout.setVisibility(View.VISIBLE);
                    msg_text.setText(item.text);
                } else {
                    vertical_layout.setVisibility(View.VISIBLE);
                    horizontal_layout.setVisibility(View.GONE);
                    msg_text_2.setText(item.text);
                }
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
                CardView cardView;
                boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("dark_theme", false);
                if(item.type == 0) {
                    cardView = ((CardView) convertView.findViewById(R.id.incoming_msg_layout));
                    if (Global.checkMonet(ctx)) {
                        MonetCompat monet = MonetCompat.getInstance();
                        cardView.setCardBackgroundColor(
                                Objects.requireNonNull(monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
                    } else {
                        if (isDarkTheme) {
                            cardView.setCardBackgroundColor(
                                    MaterialColors.getColor(convertView, androidx.appcompat.R.attr.colorPrimaryDark));
                        } else {
                            cardView.setCardBackgroundColor(
                                    MaterialColors.getColor(convertView, androidx.appcompat.R.attr.colorAccent));
                        }
                    }
                }
            } else {
                msg_text.setTypeface(Global.getFlexibleTypeface(ctx, 500));
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
