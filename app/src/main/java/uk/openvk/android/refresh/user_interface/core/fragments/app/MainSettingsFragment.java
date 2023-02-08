package uk.openvk.android.refresh.user_interface.core.fragments.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.util.OvkAlertDialogBuilder;
import uk.openvk.android.refresh.user_interface.core.activities.AppActivity;
import uk.openvk.android.refresh.user_interface.core.activities.MainActivity;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences instance_prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main_pref);
        instance_prefs = requireContext().getSharedPreferences("instance", 0);
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

        Preference logout = findPreference("logout");
        assert logout != null;
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showLogoutConfirmDialog();
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

    private void showLogoutConfirmDialog() {
        OvkAlertDialogBuilder dialog = new OvkAlertDialogBuilder(requireContext(), R.style.ApplicationTheme_AlertDialog);
        dialog.setMessage(R.string.log_out_warning);
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SharedPreferences.Editor editor = instance_prefs.edit();
                        editor.putString("access_token", "");
                        editor.putString("server", "");
                        editor.putString("account_password_sha256", "");
                        editor.apply();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Intent activity = new Intent(requireContext().getApplicationContext(), MainActivity.class);
                                activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(activity);
                                System.exit(0);
                            }
                        };
                        new Handler().postDelayed(runnable, 100);
                    }
                });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }
}
