package uk.openvk.android.refresh.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.MainSettingsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.VideoSettingsFragment;
import uk.openvk.android.refresh.ui.util.OvkAlertDialogBuilder;
import uk.openvk.android.refresh.ui.core.fragments.app.settings.PersonalizationFragment;

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

    @SuppressLint({"ViewHolder", "UseCompatTextViewDrawableApis"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_single_choice, null);
            viewHolder = new ViewHolder();
            viewHolder.view = convertView;
            viewHolder.position = position;
            viewHolder.isSelected = position == checkedItem;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        CheckedTextView checkedTv = ((CheckedTextView) convertView.findViewById(android.R.id.text1));
        if(Global.checkMonet(ctx)) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(ctx)
                    .getBoolean("dark_theme", false);
            int accentColor;
            if (isDarkTheme) {
                accentColor = Objects.requireNonNull(monet.getMonetColors().getAccent1()
                        .get(200)).toLinearSrgb().toSrgb().quantize8();
            } else {
                accentColor = Objects.requireNonNull(monet.getMonetColors().getAccent1()
                        .get(500)).toLinearSrgb().toSrgb().quantize8();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkedTv.setCompoundDrawableTintList(ColorStateList.valueOf(accentColor));
            }
        }
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
                    if(frg.getClass().getSimpleName().equals("PersonalizationFragment")) {
                        ((PersonalizationFragment) frg).onMenuItemClicked(list, position);
                    } else if(frg.getClass().getSimpleName().equals("VideoSettingsFragment")) {
                        ((VideoSettingsFragment) frg).onMenuItemClicked(list, position);
                    } else {
                        ((MainSettingsFragment) frg).onMenuItemClicked(list, position);
                    }
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
