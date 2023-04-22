package uk.openvk.android.refresh.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import uk.openvk.android.refresh.R;

public class SendTextBottomPanel extends ConstraintLayout {
    public SendTextBottomPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.panel_sendtext_bottom, null);

        this.addView(view);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    public void setOnSendButtonClickListener(OnClickListener listener) {
        findViewById(R.id.send_btn).setOnClickListener(listener);
    }

    public String getText() {
        return Objects.requireNonNull(((TextInputEditText) findViewById(R.id.send_text)).getText()).toString();
    }

    public void clearText() {
        Objects.requireNonNull(((TextInputEditText) findViewById(R.id.send_text))).setText("");
    }
}
