package uk.openvk.android.refresh.ui.list.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.list.items.PublicPageAboutItem;

public class PublicPageAboutAdapter extends RecyclerView.Adapter<PublicPageAboutAdapter.Holder>  {
    private Context ctx;
    private ArrayList<PublicPageAboutItem> items;

    public PublicPageAboutAdapter(Context context, ArrayList<PublicPageAboutItem> items) {
        this.ctx = context;
        this.items = items;
    }

    @NonNull
    @Override
    public PublicPageAboutAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PublicPageAboutAdapter.Holder(LayoutInflater.from(ctx)
                .inflate(R.layout.list_item_pubpage_about, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PublicPageAboutAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View convertView;
        private final TextView title;
        private final TextView subtitle;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.title = (TextView) view.findViewById(R.id.about_item_title);
            this.subtitle = (TextView) view.findViewById(R.id.about_item_subtitle);
        }

        void bind(final int position) {
            final PublicPageAboutItem item = getItem(position);
            title.setText(item.title);
            if(item.title.equals(ctx.getResources().getString(R.string.group_site))) {
                subtitle.setText(Global.formatLinksAsHtml(item.subtitle));
                subtitle.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                subtitle.setText(item.subtitle);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private PublicPageAboutItem getItem(int position) {
        return items.get(position);
    }
}
