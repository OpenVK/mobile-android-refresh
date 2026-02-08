package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Conversation;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.list.adapters.MessagesAdapter;
import uk.openvk.android.refresh.ui.view.layouts.SendTextBottomPanel;
import uk.openvk.android.refresh.ui.wrappers.LocaleContextWrapper;

public class ConversationActivity extends BaseNetworkActivity {
    public Conversation conversation;
    public long peer_id;
    public String conv_title;
    public int peer_online;
    public ArrayList<uk.openvk.android.refresh.api.entities.Message> history;
    private MessagesAdapter conversation_adapter;
    private RecyclerView messagesView;
    private LinearLayoutManager llm;
    private DownloadManager downloadManager;
    private SendTextBottomPanel bottomPanel;
    private uk.openvk.android.refresh.api.entities.Message last_sended_message;
    private boolean isDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"),
                getWindow());
        Global.setInterfaceFont(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        instance_prefs = getSharedPreferences("instance", 0);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
            } else {
                peer_id = extras.getLong("peer_id");
                conv_title = extras.getString("conv_title");
                peer_online = extras.getInt("online");
            }
        } else {
            peer_id = savedInstanceState.getInt("peer_id");
            conv_title = (String) savedInstanceState.getSerializable("conv_title");
            peer_online = savedInstanceState.getInt("online");
        }
        setContentView(R.layout.activity_conversation);

        history = new ArrayList<>();

        setAPIWrapper();
        setAppBar();
        setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);
        setBottomPanel();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    private void setBottomPanel() {
        bottomPanel = findViewById(R.id.sendTextBottomPanel);
        bottomPanel.setOnSendButtonClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(!bottomPanel.getText().isEmpty()) {
                    try {
                        last_sended_message = new uk.openvk.android.refresh.api.entities
                                .Message(1, false, false,
                                (int)(System.currentTimeMillis() / 1000), bottomPanel.getText(),
                                ConversationActivity.this);
                        last_sended_message.sending = true;
                        last_sended_message.isError = false;
                        conversation.sendMessage(ovk_api.wrapper, bottomPanel.getText());
                        if(history == null) {
                            history = new ArrayList<>();
                        }
                        history.add(last_sended_message);
                        if(conversation_adapter == null) {
                            conversation_adapter = new MessagesAdapter(
                                    ConversationActivity.this, history, peer_id);
                            messagesView.setAdapter(conversation_adapter);
                        } else {
                            conversation_adapter.notifyDataSetChanged();
                        }
                        bottomPanel.clearText();
                        messagesView.smoothScrollToPosition(history.size() -1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        ((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).addTextChangedListener(
                new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                AppCompatImageButton send_btn = bottomPanel.findViewById(R.id.send_btn);
                if(!bottomPanel.getText().isEmpty()) {
                    send_btn.setEnabled(true);
                } else {
                    send_btn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ((TextInputEditText) bottomPanel.findViewById(R.id.send_text))
                        .setLines(
                                Math.min(((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).getLineCount(), 4)
                        );
            }
        });
    }

    private void setAppBar() {
        MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(conv_title);
        if(peer_online == 1) {
            toolbar.setSubtitle(R.string.online);
        } else {
            toolbar.setSubtitle(R.string.offline);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if(!Global.checkMonet(this)) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = getTypedValue();
            window.setStatusBarColor(typedValue.data);
        }
    }

    @NonNull
    private TypedValue getTypedValue() {
        TypedValue typedValue = new TypedValue();
        boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (isDarkThemeEnabled) {
            getTheme().resolveAttribute(androidx.appcompat.R.attr.background, typedValue,
                    true);
        } else {
            getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue,
                    true);
        }
        return typedValue;
    }

    public void setAPIWrapper() {
        conversation = new Conversation();
        conversation.getHistory(ovk_api.wrapper, peer_id);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void receiveState(int message, Bundle data) {
        if(data.containsKey("address")) {
            String activityName = data.getString("address");
            if(activityName == null) {
                return;
            }
            boolean isCurrentActivity = activityName.equals(getLocalClassName());
            if(!isCurrentActivity) {
                return;
            }
        }
        if (message == HandlerMessages.MESSAGES_GET_HISTORY) {
            messagesView = findViewById(R.id.messages_view);
            history = conversation.parseHistory(this, data.getString("response"));
            conversation_adapter = new MessagesAdapter(this, history, peer_id);
            llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            llm.setStackFromEnd(true);
            messagesView.setLayoutManager(llm);
            messagesView.setAdapter(conversation_adapter);
        } else if (message == HandlerMessages.CHAT_DISABLED) {
            last_sended_message.sending = false;
            last_sended_message.isError = true;
            history.set(history.size() - 1, last_sended_message);
            conversation_adapter.notifyDataSetChanged();
        } else if(message == HandlerMessages.MESSAGES_SEND) {
            last_sended_message.sending = false;
            last_sended_message.getSendedId(data.getString("response"));
            history.set(history.size() - 1, last_sended_message);
            conversation_adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void recreate() {

    }
}
