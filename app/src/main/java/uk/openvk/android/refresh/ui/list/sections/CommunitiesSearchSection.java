package uk.openvk.android.refresh.ui.list.sections;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Group;
import uk.openvk.android.refresh.ui.core.activities.QuickSearchActivity;

public class CommunitiesSearchSection extends Section {
    private Context ctx;
    private ArrayList<Group> items;

    public CommunitiesSearchSection(Context context, ArrayList<Group> items) {
        super(SectionParameters.builder().headerResourceId(R.layout.list_item_section)
                .itemResourceId(R.layout.list_item_search_result).build());
        this.ctx = context;
        this.items = items;
    }

    @Override
    public int getContentItemsTotal() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new Holder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);
        HeaderHolder headerHolder = (HeaderHolder) holder;
        headerHolder.section_title.setText(ctx.getResources().getString(R.string.communities_section, items.size()));
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        Holder itemHolder = (Holder) holder;
        Group item = items.get(position);
        itemHolder.title.setText(item.name);
        itemHolder.title.setFocusable(false);
        itemHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ctx instanceof QuickSearchActivity) {
                    ((QuickSearchActivity) ctx).openGroup(position);
                }
            }
        });
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public TextView title;
        public View view;
        public Holder(@NonNull View view) {
            super(view);
            this.view = view;
            title = view.findViewById(R.id.search_result_title);
        }
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {
        private final TextView section_title;

        public HeaderHolder(@NonNull View view) {
            super(view);
            section_title = view.findViewById(R.id.section_title);
        }
    }
}
