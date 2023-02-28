package uk.openvk.android.refresh.ui.core.fragments.app.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.list.adapters.DialogSingleChoiceAdapter;
import uk.openvk.android.refresh.ui.util.OvkAlertDialogBuilder;

public class VideoSettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.app_video_pref);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        instance_prefs = requireContext().getSharedPreferences("instance", 0);
        setListeners();
        Preference restart_required = Objects.requireNonNull(findPreference("restart_required"));
        restart_required.setVisible(false);
    }

    private void setListeners() {
        Preference video_player = findPreference("video_player");
        assert video_player != null;
        video_player.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                showVideoPlayerChooseDialog();
                return false;
            }
        });
        setPreferenceSummary(video_player, "video_player");
    }

    private void showVideoPlayerChooseDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("video_player", "built_in");
        if (value.equals("3rd_party")) {
            valuePos = 1;
        }
        DialogSingleChoiceAdapter singleChoiceAdapter = new DialogSingleChoiceAdapter(requireContext(), this, valuePos,
                getResources().getStringArray(R.array.video_player));
        OvkAlertDialogBuilder builder = new OvkAlertDialogBuilder(requireContext(), R.style.ApplicationTheme_AlertDialog);
        builder.setTitle(R.string.pref_video_player);
        builder.setSingleChoiceItems(singleChoiceAdapter, 0, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        AlertDialog dialog = builder.getDialog();
        singleChoiceAdapter.setDialogBuilder(builder);
    }

    private void setPreferenceSummary(Preference pref, String tag) {
        if(global_prefs == null) {
            global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        }
        if(tag.equals("video_player")) {
            String value = global_prefs.getString("video_player", "built_in");
            switch (value) {
                case "built_in":
                    pref.setSummary(getResources().getStringArray(R.array.video_player)[0]);
                    break;
                case "3rd_party":
                    pref.setSummary(getResources().getStringArray(R.array.video_player)[1]);
                    break;
            }
        }
    }

    public void onMenuItemClicked(String[] list, int which) {
        if (Arrays.equals(list, getResources().getStringArray(R.array.video_player))) {
            SharedPreferences.Editor editor = global_prefs.edit();
            if(which == 0) {
                editor.putString("video_player", "built_in");
            } else if(which == 1) {
                editor.putString("video_player", "3rd_party");
            }
            editor.apply();
            setPreferenceSummary(findPreference("video_player"), "video_player");
        }
    }


    public void setGlobalPreferences(SharedPreferences global_prefs) {
        this.global_prefs = global_prefs;
    }
}
