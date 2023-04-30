package uk.openvk.android.refresh.ui.core.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.ui.core.activities.AuthActivity;

public class AuthTwoFactorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_2fa, container, false);
        TextView twofactor_edit = view.findViewById(R.id.twofactor_edit);
        ((MaterialButton) view.findViewById(R.id.twofactor_confirm_btn)).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    if (getActivity().getClass().getSimpleName().equals("AuthActivity")) {
                        ((AuthActivity) getActivity()).signIn(twofactor_edit.getText().toString());
                    }
                }
            }
        });
        ((MaterialButton) view.findViewById(R.id.twofactor_cancel_btn)).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null) {
                    if (getActivity().getClass().getSimpleName().equals("AuthActivity")) {
                        ((AuthActivity) getActivity()).changeFragment("auth_form");
                    }
                }
            }
        });
        return view;
    }
}
