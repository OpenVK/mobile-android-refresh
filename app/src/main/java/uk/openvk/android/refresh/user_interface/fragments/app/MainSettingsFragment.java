package uk.openvk.android.refresh.user_interface.fragments.app;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.activities.AppActivity;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main_pref);
        setListeners();
    }

    public void setListeners() {
        Preference person = findPreference("personalization");
        assert person != null;
        person.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).switchFragment("personalization");
                }
                return false;
            }
        });

        Preference about_app = findPreference("about_app");
        assert about_app != null;
        about_app.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).switchFragment("about_app");
                }
                return false;
            }
        });
    }
}
