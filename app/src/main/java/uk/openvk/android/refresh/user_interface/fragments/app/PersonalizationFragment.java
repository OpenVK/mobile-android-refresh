package uk.openvk.android.refresh.user_interface.fragments.app;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;

public class PersonalizationFragment extends PreferenceFragmentCompat {
    private SharedPreferences global_prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.person_perf);
        setListeners();
    }

    public void setGlobalPreferences(SharedPreferences global_prefs) {
        this.global_prefs = global_prefs;
    }

    public void setListeners() {
        Preference accentColor = findPreference("accentColor");
        assert accentColor != null;
        accentColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAccentColorChangeDialog();
                return false;
            }
        });
        setPreferenceSummary(accentColor, "theme_color");

        Preference avatars_shape = findPreference("avatarsShape");
        assert avatars_shape != null;
        avatars_shape.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAvatarsShapeChangeDialog();
                return false;
            }
        });
        setPreferenceSummary(avatars_shape, "avatars_shape");

        SwitchPreferenceCompat darkTheme = (SwitchPreferenceCompat) findPreference("darkTheme");
        if(global_prefs.getBoolean("dark_theme", false)) {
            Objects.requireNonNull(darkTheme).setChecked(true);
        } else {
            Objects.requireNonNull(darkTheme).setChecked(false);
        }
        Objects.requireNonNull(darkTheme).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = global_prefs.edit();
                editor.putBoolean("dark_theme", Objects.requireNonNull(darkTheme).isChecked());
                editor.apply();
                if(Objects.requireNonNull(darkTheme).isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).restart();
                }
                return true;
            }
        });

    }

    private void setPreferenceSummary(Preference pref, String tag) {
        if(tag.equals("theme_color")) {
            String value = global_prefs.getString("theme_color", "blue");
            switch (value) {
                case "blue":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[0]);
                    break;
                case "red":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[1]);
                    break;
                case "green":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[2]);
                    break;
                case "violet":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[3]);
                    break;
                case "orange":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[4]);
                    break;
                case "teal":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[5]);
                    break;
                case "vk5x":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[6]);
                    break;
                case "gray":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[7]);
                    break;
            }
        } else if(tag.equals("avatars_shape")) {
            String value = global_prefs.getString("avatars_shape", "circle");
            switch (value) {
                default:
                    pref.setSummary(getResources().getStringArray(R.array.avatars_shape)[0]);
                    break;
                case "round32px":
                    pref.setSummary(getResources().getStringArray(R.array.avatars_shape)[1]);
                    break;
                case "rond16px":
                    pref.setSummary(getResources().getStringArray(R.array.avatars_shape)[2]);
                    break;
                case "rectangular":
                    pref.setSummary(getResources().getStringArray(R.array.avatars_shape)[3]);
                    break;
            }
        }

    }

    private void showAccentColorChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.pref_accentcolor);
        builder.setSingleChoiceItems(R.array.theme_colors, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = global_prefs.edit();
                if(which == 0) {
                    editor.putString("theme_color", "blue");
                } else if(which == 1) {
                    editor.putString("theme_color", "red");
                } else if(which == 2) {
                    editor.putString("theme_color", "green");
                } else if(which == 3) {
                    editor.putString("theme_color", "violet");
                } else if(which == 4) {
                    editor.putString("theme_color", "orange");
                } else if(which == 5) {
                    editor.putString("theme_color", "teal");
                } else if(which == 6) {
                    editor.putString("theme_color", "vk5x");
                } else if(which == 7) {
                    editor.putString("theme_color", "gray");
                }
                editor.apply();
                setPreferenceSummary(findPreference("accentColor"), "theme_color");
                dialog.dismiss();
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).restart();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        String value = global_prefs.getString("theme_color", "blue");
        switch (value) {
            case "blue":
                dialog.getListView().setItemChecked(0, true);
                break;
            case "red":
                dialog.getListView().setItemChecked(1, true);
                break;
            case "green":
                dialog.getListView().setItemChecked(2, true);
                break;
            case "violet":
                dialog.getListView().setItemChecked(3, true);
                break;
            case "orange":
                dialog.getListView().setItemChecked(4, true);
                break;
            case "teal":
                dialog.getListView().setItemChecked(5, true);
                break;
            case "vk5x":
                dialog.getListView().setItemChecked(6, true);
                break;
            case "gray":
                dialog.getListView().setItemChecked(7, true);
                break;
        }
    }

    private void showAvatarsShapeChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.pref_avatars_shape);
        builder.setSingleChoiceItems(R.array.avatars_shape, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = global_prefs.edit();
                if(which == 0) {
                    editor.putString("avatars_shape", "circle");
                } else if(which == 1) {
                    editor.putString("avatars_shape", "round32px");
                } else if(which == 2) {
                    editor.putString("avatars_shape", "round16px");
                } else if(which == 3) {
                    editor.putString("avatars_shape", "rectangular");
                }
                editor.apply();
                setPreferenceSummary(findPreference("avatarsShape"), "avatars_shape");
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).setAvatarShape();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        String value = global_prefs.getString("avatars_shape", "blue");
        switch (value) {
            default:
                dialog.getListView().setItemChecked(0, true);
                break;
            case "round32px":
                dialog.getListView().setItemChecked(1, true);
                break;
            case "round16px":
                dialog.getListView().setItemChecked(2, true);
                break;
            case "rectangular":
                dialog.getListView().setItemChecked(3, true);
                break;
        }
    }
}
