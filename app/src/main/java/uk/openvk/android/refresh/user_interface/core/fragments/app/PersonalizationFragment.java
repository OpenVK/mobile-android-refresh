package uk.openvk.android.refresh.user_interface.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.OvkAlertDialogBuilder;
import uk.openvk.android.refresh.user_interface.core.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.list.adapters.DialogSingleChoiceAdapter;

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

        Preference interface_font = findPreference("interfaceFont");
        assert interface_font != null;
        interface_font.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showInterfaceFontsDialog();
                return false;
            }
        });
        setPreferenceSummary(interface_font, "interface_font");

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
        if(global_prefs == null) {
            global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        }
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
                case "monet":
                    pref.setSummary(getResources().getStringArray(R.array.theme_colors)[8]);
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
        } else if(tag.equals("interface_font")) {
            String value = global_prefs.getString("interface_font", "system");
            switch (value) {
                default:
                    pref.setSummary(getResources().getStringArray(R.array.fonts)[0]);
                    break;
                case "inter":
                    pref.setSummary(getResources().getStringArray(R.array.fonts)[1]);
                    break;
                case "open_sans":
                    pref.setSummary(getResources().getStringArray(R.array.fonts)[2]);
                    break;
                case "raleway":
                    pref.setSummary(getResources().getStringArray(R.array.fonts)[3]);
                    break;
                case "roboto":
                    pref.setSummary(getResources().getStringArray(R.array.fonts)[4]);
                    break;
                case "rubik":
                    pref.setSummary(getResources().getStringArray(R.array.fonts)[5]);
                    break;
            }
        }

    }

    private void showAccentColorChangeDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("theme_color", "blue");
        switch (value) {
            case "red":
                valuePos = 1;
                break;
            case "green":
                valuePos = 2;
                break;
            case "violet":
                valuePos = 3;
                break;
            case "orange":
                valuePos = 4;
                break;
            case "teal":
                valuePos = 5;
                break;
            case "vk5x":
                valuePos = 6;
                break;
            case "gray":
                valuePos = 7;
                break;
            case "monet":
                valuePos = 8;
                break;
        }
        DialogSingleChoiceAdapter singleChoiceAdapter = new DialogSingleChoiceAdapter(requireContext(), this, valuePos, getResources().getStringArray(R.array.theme_colors));
        OvkAlertDialogBuilder builder = new OvkAlertDialogBuilder(requireContext(), R.style.ApplicationTheme_AlertDialog);
        builder.setTitle(R.string.pref_accentcolor);
        builder.setSingleChoiceItems(singleChoiceAdapter, 0, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        AlertDialog dialog = builder.getDialog();
        singleChoiceAdapter.setDialogBuilder(builder);
    }

    private void showAvatarsShapeChangeDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("avatars_shape", "circle");
        switch (value) {
            case "round32px":
                valuePos = 1;
                break;
            case "round16px":
                valuePos = 2;
                break;
            case "rectangular":
                valuePos = 3;
                break;
        }
        DialogSingleChoiceAdapter singleChoiceAdapter = new DialogSingleChoiceAdapter(requireContext(), this, valuePos, getResources().getStringArray(R.array.avatars_shape));
        OvkAlertDialogBuilder builder = new OvkAlertDialogBuilder(requireContext(), R.style.ApplicationTheme_AlertDialog);
        builder.setTitle(R.string.pref_avatars_shape);
        builder.setSingleChoiceItems(singleChoiceAdapter, 0, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        AlertDialog dialog = builder.getDialog();;
        singleChoiceAdapter.setDialogBuilder(builder);
    }

    public void showInterfaceFontsDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("interface_font", "system");
        switch (value) {
            default:
                valuePos = 0;
                break;
            case "inter":
                valuePos = 1;
                break;
            case "open_sans":
                valuePos = 2;
                break;
            case "raleway":
                valuePos = 3;
                break;
            case "roboto":
                valuePos = 4;
                break;
            case "rubik":
                valuePos = 5;
                break;
        }
        DialogSingleChoiceAdapter singleChoiceAdapter = new DialogSingleChoiceAdapter(requireContext(), this, valuePos, getResources().getStringArray(R.array.fonts));
        OvkAlertDialogBuilder builder = new OvkAlertDialogBuilder(requireContext(), R.style.ApplicationTheme_AlertDialog);
        builder.setTitle(R.string.pref_font);
        AlertDialog dialog = null;
        builder.setSingleChoiceItems(singleChoiceAdapter, 0, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        dialog = builder.getDialog();
        singleChoiceAdapter.setDialogBuilder(builder);
    }

    public void onMenuItemClicked(String[] list, int which) {
        if(Arrays.equals(list, getResources().getStringArray(R.array.theme_colors))) {
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
            } else if(which == 8) {
                editor.putString("theme_color", "monet");
            }
            editor.apply();
            setPreferenceSummary(findPreference("accentColor"), "theme_color");
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) requireActivity()).restart();
            }
        } else if(Arrays.equals(list, getResources().getStringArray(R.array.fonts))) {
            SharedPreferences.Editor editor = global_prefs.edit();
            if(which == 0) {
                editor.putString("interface_font", "system");
            } else if(which == 1) {
                editor.putString("interface_font", "inter");
            } else if(which == 2) {
                editor.putString("interface_font", "open_sans");
            } else if(which == 3) {
                editor.putString("interface_font", "raleway");
            } else if(which == 4) {
                editor.putString("interface_font", "roboto");
            } else if(which == 5) {
                editor.putString("interface_font", "rubik");
            }
            editor.apply();
            setPreferenceSummary(findPreference("interfaceFont"), "interface_font");
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) requireActivity()).restart();
            }
        } else if(Arrays.equals(list, getResources().getStringArray(R.array.avatars_shape))) {
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
        }
    }
}
