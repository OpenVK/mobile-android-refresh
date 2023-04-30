package uk.openvk.android.refresh.ui.util;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.Objects;

import uk.openvk.android.refresh.Global;

public class OvkAlertDialogBuilder extends MaterialAlertDialogBuilder {

    private AlertDialog dialog;

    public OvkAlertDialogBuilder(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        //setFont();
    }

    public OvkAlertDialogBuilder(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public AlertDialog create() {
        dialog = super.create();
        return dialog;
    }

    public void setFont() {
        if(dialog.getWindow().findViewById(com.google.android.material.R.id.alertTitle) != null) {
            ((TextView) dialog.getWindow().findViewById(com.google.android.material.R.id.alertTitle)).setTypeface(
                    Global.getFlexibleTypeface(getContext(), 500));
        }
        // setting medium fonts for buttons
        if(dialog.getButton(BUTTON_NEGATIVE) != null) {
            dialog.getButton(BUTTON_NEGATIVE).setTypeface(Global.getFlexibleTypeface(
                    getContext(), 500));
            if(Global.checkMonet(getContext())) {
                MonetCompat monet = MonetCompat.getInstance();
                boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(
                        getContext()).getBoolean("dark_theme", false);
                int accentColor;
                if(isDarkTheme) {
                    accentColor = Objects.requireNonNull(monet.getMonetColors().getAccent1()
                            .get(200)).toLinearSrgb().toSrgb().quantize8();
                } else {
                    accentColor = Objects.requireNonNull(monet.getMonetColors().getAccent1()
                            .get(500)).toLinearSrgb().toSrgb().quantize8();
                }
                dialog.getButton(BUTTON_NEGATIVE).setTextColor(accentColor);
            }
        }
        if(dialog.getButton(BUTTON_NEUTRAL) != null) {
            dialog.getButton(BUTTON_NEUTRAL).setTypeface(Global.getFlexibleTypeface(
                    getContext(), 500));
            if(Global.checkMonet(getContext())) {
                MonetCompat monet = MonetCompat.getInstance();
                boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(
                        getContext()).getBoolean("dark_theme", false);
                int accentColor;
                if(isDarkTheme) {
                    accentColor = Objects.requireNonNull(monet.getMonetColors()
                            .getAccent1().get(200)).toLinearSrgb().toSrgb().quantize8();
                } else {
                    accentColor = Objects.requireNonNull(monet.getMonetColors()
                            .getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8();
                }
                dialog.getButton(BUTTON_NEUTRAL).setTextColor(accentColor);
            }
        }
        if(dialog.getButton(BUTTON_POSITIVE) != null) {
            dialog.getButton(BUTTON_POSITIVE).setTypeface(Global.getFlexibleTypeface(
                    getContext(), 500));
            if(Global.checkMonet(getContext())) {
                MonetCompat monet = MonetCompat.getInstance();
                boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(
                        getContext()).getBoolean("dark_theme", false);
                int accentColor;
                if(isDarkTheme) {
                    accentColor = Objects.requireNonNull(monet.getMonetColors()
                            .getAccent1().get(200)).toLinearSrgb().toSrgb().quantize8();
                } else {
                    accentColor = Objects.requireNonNull(monet.getMonetColors()
                            .getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8();
                }
                dialog.getButton(BUTTON_POSITIVE).setTextColor(accentColor);
            }
        }
    }

    @Override
    public AlertDialog show() {
        dialog = super.show();
        setFont();
        return dialog;
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    public void clearCheck(int position) {
        try {
            ((CheckedTextView) dialog.getListView().getChildAt(position)
                    .findViewById(android.R.id.text1)).setChecked(false);
        } catch (Exception ignored) {
        }
    }
}
