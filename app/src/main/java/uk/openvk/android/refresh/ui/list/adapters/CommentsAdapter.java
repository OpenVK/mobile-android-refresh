package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.collection.LruCache;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.models.Comment;

public class CommentsAdapter  extends RecyclerView.Adapter<CommentsAdapter.Holder> {

    private ArrayList<Comment> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;

    public CommentsAdapter(Context context, ArrayList<Comment> comments) {
        ctx = context;
        items = comments;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
    }

    public Comment getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final TextView comment_author;
        public final TextView comment_info;
        public final TextView comment_text;
        public final View convertView;
        public final ImageView comment_author_avatar;
        public final View divider;
        //private final TextView expand_text_btn;
        private final TextView reply_btn;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.comment_author = view.findViewById(R.id.comment_author);
            this.comment_info = view.findViewById(R.id.comment_time);
            this.comment_text = view.findViewById(R.id.comment_text);
            this.comment_author_avatar = view.findViewById(R.id.comment_avatar);
            this.divider = view.findViewById(R.id.divider);
            //this.expand_text_btn = view.findViewById(R.id.expand_text_btn);
            this.reply_btn = view.findViewById(R.id.reply_btn);
        }

        @SuppressLint("SimpleDateFormat")
        void bind(final int position) {
            final Comment item = getItem(position);
            comment_author.setText(item.author);
            Date date = new Date(TimeUnit.SECONDS.toMillis(item.date));
            comment_info.setText(String.format(
                    ctx.getResources().getStringArray(R.array.date_differences)[3], new SimpleDateFormat("dd.MM.yyyy")
                            .format(TimeUnit.SECONDS.toMillis(item.date)),
                    new SimpleDateFormat("HH:mm").format(TimeUnit.SECONDS.toMillis(item.date))));
            reply_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
                        //((WallPostActivity) ctx).addAuthorMention(position);
                        Toast.makeText(ctx, R.string.not_implemented, Toast.LENGTH_LONG).show();
                    }
                }
            });
            if(item.text.length() > 0) {
                comment_text.setVisibility(View.VISIBLE);
                comment_text.setText(Global.formatLinksAsHtml(item.text));
                comment_text.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public void setArray(ArrayList<Comment> array) {
        items = array;
    }
}