package uk.openvk.android.refresh.user_interface.list_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.OvkAlertDialogBuilder;
import uk.openvk.android.refresh.user_interface.fragments.app.PersonalizationFragment;

public class DialogSingleChoiceAdapter extends BaseAdapter {

    private final Context ctx;
    private final String[] list;
    private final LayoutInflater layoutInflater;
    private final Fragment frg;
    private final ArrayList<ViewHolder> viewHolders;
    private int checkedItem;
    private AlertDialog dialog;
    private OvkAlertDialogBuilder dlgBuilder;

    public DialogSingleChoiceAdapter(Context ctx, Fragment frg, int checkedItem, String[] list) {
        this.checkedItem = checkedItem;
        this.frg = frg;
        this.ctx = ctx;
        this.list = list;
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewHolders = new ArrayList<>();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int position) {
        return list[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    static class ViewHolder {
        View view;
        int position;
        boolean isSelected;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.single_choice_item, null);
            viewHolder = new ViewHolder();
            viewHolder.view = convertView;
            viewHolder.position = position;
            viewHolder.isSelected = position == checkedItem;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        CheckedTextView checkedTv = ((CheckedTextView) convertView.findViewById(android.R.id.text1));
        checkedTv.setChecked(viewHolder.isSelected);
        checkedTv.setText((CharSequence) getItem(position));
        View finalConvertView = convertView;
        checkedTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(frg != null) {
                    if(dlgBuilder != null) {
                        dlgBuilder.clearCheck(checkedItem);
                        checkedItem = position;
                        checkedTv.setChecked(true);
                    }
                    ((PersonalizationFragment) frg).onMenuItemClicked(list, position);
                }
            }
        });
        viewHolder.view = convertView;
        viewHolder.position = position;
        viewHolders.add(position, viewHolder);
        return convertView;
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

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setDialog(AlertDialog dlg) {
        this.dialog = dlg;
    }

    public void setDialogBuilder(OvkAlertDialogBuilder dlgBuilder) {
        this.dlgBuilder = dlgBuilder;
    }
}
