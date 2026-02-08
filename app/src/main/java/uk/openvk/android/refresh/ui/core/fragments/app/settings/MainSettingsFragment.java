package uk.openvk.android.refresh.ui.core.fragments.app.settings;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.InstanceLink;
import uk.openvk.android.refresh.api.entities.Ovk;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.core.activities.MainActivity;
import uk.openvk.android.refresh.ui.core.activities.MainSettingsActivity;
import uk.openvk.android.refresh.ui.list.adapters.DialogSingleChoiceAdapter;
import uk.openvk.android.refresh.ui.util.OvkAlertDialogBuilder;

public class MainSettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private View about_instance_view;
    private Ovk ovk;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main_pref);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        instance_prefs = requireContext().getSharedPreferences("instance", 0);
        setListeners();
        if(requireActivity().getClass().getSimpleName().equals("MainSettingsActivity")) {
            hideInstanceSettings();
        } else {
            ovk = new Ovk();
        }
        Preference restart_required = Objects.requireNonNull(findPreference("restart_required"));
        restart_required.setVisible(false);
    }

    public void setListeners() {
        Preference video_settings = findPreference("video_settings");
        assert video_settings != null;
        video_settings.setOnPreferenceClickListener(preference -> {
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) requireActivity()).fn.navigateTo("video_settings");
            }
            return false;
        });

        Preference person = findPreference("personalization");
        assert person != null;
        person.setOnPreferenceClickListener(preference -> {
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) requireActivity()).fn.navigateTo("personalization");
            } else if(requireActivity().getClass().getSimpleName().equals("MainSettingsActivity")) {
                ((MainSettingsActivity) requireActivity()).switchFragment("personalization");
            }
            return false;
        });

        Preference ui_language = findPreference("ui_language");
        assert ui_language != null;
        ui_language.setOnPreferenceClickListener(preference -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                showUiLanguageSelectionDialog();
            }
            return false;
        });

        Preference logout = findPreference("logout");
        assert logout != null;
        logout.setOnPreferenceClickListener(preference -> {
            showLogoutConfirmDialog();
            return false;
        });

        Preference about_instance = findPreference("about_instance");
        assert about_instance != null;
        about_instance.setOnPreferenceClickListener(preference -> {
            showAboutInstanceDialog();
            return false;
        });

        Preference about_app = findPreference("about_app");
        assert about_app != null;
        about_app.setOnPreferenceClickListener(preference -> {
            if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) requireActivity()).fn.navigateTo("about_app");
            } else if(requireActivity().getClass().getSimpleName().equals("MainSettingsActivity")) {
                ((MainSettingsActivity) requireActivity()).switchFragment("about_app");
            }
            return false;
        });
    }

    private void showUiLanguageSelectionDialog() {
        int valuePos = 0;
        String value = global_prefs.getString("ui_language", "blue");
        switch (value) {
            default:
                break;
            case "English":
                valuePos = 1;
                break;
            case "Русский":
                valuePos = 2;
                break;
            case "Украïнська":
                valuePos = 3;
                break;
        }
        DialogSingleChoiceAdapter singleChoiceAdapter = new DialogSingleChoiceAdapter(
                requireContext(), this,
                valuePos, getResources().getStringArray(R.array.ui_languages));
        OvkAlertDialogBuilder builder = new OvkAlertDialogBuilder(requireContext(),
                R.style.ApplicationTheme_AlertDialog);
        builder.setTitle(R.string.pref_language);
        builder.setSingleChoiceItems(singleChoiceAdapter, 0, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        AlertDialog dialog = builder.getDialog();
        singleChoiceAdapter.setDialogBuilder(builder);
    }

    public void onMenuItemClicked(String[] list, int which) {
        if (Arrays.equals(list, getResources().getStringArray(R.array.ui_languages))) {
            SharedPreferences.Editor editor = global_prefs.edit();
            if(which == 0) {
                editor.putString("ui_language", "System");
            } else if(which == 1) {
                editor.putString("ui_language", "English");
            } else if(which == 2) {
                editor.putString("ui_language", "Русский");
            } else if(which == 3) {
                editor.putString("ui_language", "Украïнська");
            }
            editor.apply();
        }
        if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
            ((AppActivity) requireActivity()).restart();
        } else {
            Preference restart_required = Objects.requireNonNull(findPreference("restart_required"));
            restart_required.setVisible(true);
        }
    }

    @SuppressLint("InflateParams")
    private void showAboutInstanceDialog() {
        OvkAlertDialogBuilder dialog = new OvkAlertDialogBuilder(requireContext(),
                R.style.ApplicationTheme_AlertDialog);
        about_instance_view = getLayoutInflater().inflate(R.layout.dialog_about_instance, null);
        dialog.setTitle(getResources().getString(R.string.pref_about_instance));
        dialog.setView(about_instance_view);
        dialog.setPositiveButton(android.R.string.ok, null);
        TextView server_name = (TextView) about_instance_view.findViewById(R.id.server_addr_label2);
        server_name.setText(instance_prefs.getString("server", ""));
        ((TextView) about_instance_view.findViewById(R.id.connection_type_label2))
                .setText(getResources().getString(R.string.loading));
        ((TextView) about_instance_view.findViewById(R.id.instance_version_label2))
                .setText(getResources().getString(R.string.loading));
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll))
                .setVisibility(View.GONE);
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_statistics_ll))
                .setVisibility(View.GONE);
        ((LinearLayout) about_instance_view.findViewById(R.id.instance_links_ll))
                .setVisibility(View.GONE);
        ((TextView) about_instance_view.findViewById(R.id.rules_link))
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebAddress(String.format("http://%s/terms",
                        instance_prefs.getString("server", "")));
            }
        });
        ((TextView) about_instance_view.findViewById(R.id.privacy_link))
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebAddress(String.format("http://%s/privacy",
                        instance_prefs.getString("server", "")));
            }
        });

        ((TextView) about_instance_view.findViewById(R.id.server_addr_label))
                .setTypeface(Global.getFlexibleTypeface(requireActivity(), 500));
        ((TextView) about_instance_view.findViewById(R.id.connection_type_label))
                .setTypeface(Global.getFlexibleTypeface(requireActivity(), 500));
        ((TextView) about_instance_view.findViewById(R.id.instance_version_label))
                .setTypeface(Global.getFlexibleTypeface(requireActivity(), 500));
        ((TextView) about_instance_view.findViewById(R.id.instance_statistics_label))
                .setTypeface(Global.getFlexibleTypeface(requireActivity(), 500));
        ((TextView) about_instance_view.findViewById(R.id.instance_links_label))
                .setTypeface(Global.getFlexibleTypeface(requireActivity(), 500));

        dialog.show();
        if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
            OvkAPIWrapper ovk_api = ((AppActivity) requireActivity()).ovk_api.wrapper;
            ovk_api.checkHTTPS();
        }
    }

    private void openWebAddress(String address) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(address));
        startActivity(i);
    }

    private void showLogoutConfirmDialog() {
        OvkAlertDialogBuilder dialog = new OvkAlertDialogBuilder(requireContext(),
                R.style.ApplicationTheme_AlertDialog);
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
                                Intent activity = new Intent(requireContext().getApplicationContext(),
                                        MainActivity.class);
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

    public void getInstanceInfo(String where, String response) {
        if(where.equals("stats")) {
            try {
                ovk.parseAboutInstance(response);
                TextView users_counter = (TextView) about_instance_view
                        .findViewById(R.id.instance_users_count);
                users_counter.setText(getResources().getString(R.string.instance_users_count, ovk.instance_stats.users_count));
                TextView online_users_counter = (TextView) about_instance_view
                        .findViewById(R.id.instance_online_users_count);
                online_users_counter.setText(getResources().getString(R.string.instance_online_users_count, ovk.instance_stats.online_users_count));
                TextView active_users_counter = (TextView) about_instance_view.
                        findViewById(R.id.instance_active_users_count);
                active_users_counter.setText(getResources().getString(R.string.instance_active_users_count, ovk.instance_stats.active_users_count));
                TextView groups_counter = (TextView) about_instance_view.
                        findViewById(R.id.instance_groups_count);
                groups_counter.setText(getResources().getString(R.string.instance_groups_count, ovk.instance_stats.groups_count));
                TextView wall_posts_counter = (TextView) about_instance_view.
                        findViewById(R.id.instance_wall_posts_count);
                wall_posts_counter.setText(getResources().getString(R.string.instance_wall_posts_count, ovk.instance_stats.wall_posts_count));
                TextView admins_counter = (TextView) about_instance_view.
                        findViewById(R.id.instance_admins_count);
                admins_counter.setText(getResources().getString(R.string.instance_admins_count, ovk.instance_admins.size()));
                ((LinearLayout) about_instance_view.
                        findViewById(R.id.instance_statistics_ll)).setVisibility(View.VISIBLE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label2)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label3)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label4)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label5)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label6)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label7)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label8)).setVisibility(View.GONE);
                ((TextView) about_instance_view.
                        findViewById(R.id.instance_links_label9)).setVisibility(View.GONE);
                for (int i = 0; i < ovk.instance_links.size(); i++) {
                    InstanceLink link = ovk.instance_links.get(i);
                    TextView textView = null;
                    if (i == 0) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label2));
                    } else if (i == 1) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label3));
                    } else if (i == 2) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label4));
                    } else if (i == 3) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label5));
                    } else if (i == 4) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label6));
                    } else if (i == 5) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label7));
                    } else if (i == 6) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label8));
                    } else if (i == 7) {
                        textView = ((TextView) about_instance_view.
                                findViewById(R.id.instance_links_label9));
                    }
                    if (textView != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            textView.setText(Html.fromHtml(
                                    String.format("<a href=\"%s\">%s</a>", link.url, link.name),
                                    Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            textView.setText(Html.fromHtml(String.format("<a href=\"%s\">%s</a>",
                                    link.url, link.name)));
                        }
                        textView.setVisibility(View.VISIBLE);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
                ((LinearLayout) about_instance_view.findViewById(R.id.instance_links_ll)).setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if(where.equals("checkHTTP")) {
            TextView connection_type = (TextView) about_instance_view
                    .findViewById(R.id.connection_type_label2);
            connection_type.setText(getResources().getString(R.string.secured_connection));
        } else if(where.equals("instanceVersion")) {
            ovk.parseVersion(response);
            TextView openvk_version_tv = (TextView) about_instance_view
                    .findViewById(R.id.instance_version_label2);
            if(ovk.version.startsWith("OpenVK")) {
                openvk_version_tv.setText(ovk.version);
            } else {
                openvk_version_tv.setText(String.format("OpenVK %s", ovk.version));
            }
            ((LinearLayout) about_instance_view.findViewById(R.id.instance_version_ll))
                    .setVisibility(View.VISIBLE);
        }
    }

    public void hideInstanceSettings() {
        Preference about_instance = findPreference("about_instance");
        assert about_instance != null;
        about_instance.setVisible(false);
        PreferenceCategory account_category = findPreference("account_category");
        assert account_category != null;
        account_category.setVisible(false);
        PreferenceCategory app_category = findPreference("application_category");
        assert app_category != null;
        app_category.setVisible(false);
    }
}
