package uk.openvk.android.refresh.user_interface.fragments.auth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.activities.AuthActivity;

public class AuthFragment extends Fragment {
    private View view;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.auth_fragment, container, false);
        Button sign_in_btn = view.findViewById(R.id.sign_in_btn);
        AutoCompleteTextView instance_edit = view.findViewById(R.id.instance_edit);
        TextInputEditText username_edit = view.findViewById(R.id.username_edit);
        TextInputEditText password_edit = view.findViewById(R.id.password_edit);
        instance_edit.setText("openvk.uk");
        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null) {
                    if (getActivity().getClass().getSimpleName().equals("AuthActivity")) {
                        ((AuthActivity) getActivity()).signIn(instance_edit.getText().toString(), username_edit.getText().toString(),
                                password_edit.getText().toString());
                    }
                }
            }
        });

        ((LinearLayoutCompat) view.findViewById(R.id.auth_layout)).setGravity(Gravity.CENTER);
        instance_edit.setAdapter(
                new ArrayAdapter<String>(getContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.avaliable_instances)));
        TextInputLayout instance_layout = (view.findViewById(R.id.instance_input_layout));
        instance_edit.setThreshold(25000000);
        return view;
    }

    public void setAuthorizationData(String instance, String username, String password) {
        AutoCompleteTextView instance_edit = view.findViewById(R.id.instance_edit);
        TextInputEditText username_edit = view.findViewById(R.id.username_edit);
        TextInputEditText password_edit = view.findViewById(R.id.password_edit);
        instance_edit.setText(instance);
        username_edit.setText(username);
        password_edit.setText(password);
    }
}
