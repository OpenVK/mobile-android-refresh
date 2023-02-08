package uk.openvk.android.refresh.user_interface.list.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.list.items.ToolbarSpinnerItem;

public class NewsfeedToolbarSpinnerAdapter implements SpinnerAdapter {

    private LayoutInflater inflater;
    private ArrayList<ToolbarSpinnerItem> items;
    private Context ctx;

    public NewsfeedToolbarSpinnerAdapter(Context ctx, ArrayList<ToolbarSpinnerItem> items) {
        this.ctx = ctx;
        this.items = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ToolbarSpinnerItem item = getItem(position);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.actionbar_spinner_dropdown_item, null);
        }
        ((TextView) view.findViewById(R.id.appbar_subtitle)).setText(item.name);
        return view;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ToolbarSpinnerItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.actionbar_spinner_item, parent, false);
        }
        ToolbarSpinnerItem item = getItem(position);
        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((TextView) view.findViewById(R.id.appbar_title)).setText(item.category);
        } else {
            ((TextView) view.findViewById(R.id.appbar_subtitle)).setText(item.name);
        }
        ((TextView) view.findViewById(R.id.appbar_title)).setTypeface(Global.getFlexibleTypeface(ctx, 500));
        ((TextView) view.findViewById(R.id.appbar_title)).setTextColor(
                ctx.getResources().getColor(R.color.onPrimaryTextColor));
        ((TextView) view.findViewById(R.id.appbar_subtitle)).setText(item.name);
        ((TextView) view.findViewById(R.id.appbar_subtitle)).setTypeface(Global.getFlexibleTypeface(ctx, 400));
        ((TextView) view.findViewById(R.id.appbar_subtitle)).setTextColor(
                ctx.getResources().getColor(R.color.onSecondaryTextColor));
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
