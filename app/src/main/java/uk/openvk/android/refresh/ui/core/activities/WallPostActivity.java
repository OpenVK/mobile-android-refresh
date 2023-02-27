package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.Friends;
import uk.openvk.android.refresh.api.Groups;
import uk.openvk.android.refresh.api.Likes;
import uk.openvk.android.refresh.api.Messages;
import uk.openvk.android.refresh.api.Newsfeed;
import uk.openvk.android.refresh.api.Users;
import uk.openvk.android.refresh.api.Wall;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.models.Comment;
import uk.openvk.android.refresh.api.models.User;
import uk.openvk.android.refresh.api.models.WallPost;
import uk.openvk.android.refresh.api.wrappers.DownloadManager;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.refresh.ui.list.adapters.CommentsAdapter;
import uk.openvk.android.refresh.ui.list.adapters.MessagesAdapter;
import uk.openvk.android.refresh.ui.view.layouts.SendTextBottomPanel;

public class WallPostActivity extends MonetCompatActivity {
    private SharedPreferences global_prefs;
    private boolean isDarkTheme;
    private WallPost wallPost;
    private Toolbar toolbar;
    private Wall wall;
    private Account account;
    private SharedPreferences instance_prefs;
    private OvkAPIWrapper ovk_api;
    private DownloadManager downloadManager;
    public Handler handler;
    private ArrayList<Comment> comments;
    private CommentsAdapter commentsAdapter;
    private RecyclerView comments_rv;
    private LinearLayoutManager llm;
    private SendTextBottomPanel bottomPanel;
    private Comment last_sended_comment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(global_prefs.getBoolean("dark_theme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        instance_prefs = getSharedPreferences("instance", 0);
        Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"), getWindow());
        Global.setInterfaceFont(this);
        isDarkTheme = global_prefs.getBoolean("dark_theme", false);
        setContentView(R.layout.wall_post_watch);
        if(savedInstanceState != null) {
            wallPost = savedInstanceState.getParcelable("post");
            wallPost.counters = savedInstanceState.getParcelable("counters");
        } else {
            if(getIntent().getExtras() != null) {account = getIntent().getExtras().getParcelable("account");
               wallPost = getIntent().getExtras().getParcelable("post");
               wallPost.counters = getIntent().getExtras().getParcelable("counters");
            }
        }
        setMonetTheme();
        setAPIWrapper();
        setAppBar();
        if(wallPost != null) {
            openPost(wallPost);
        } else {
            finish();
        }
    }

    private void setAPIWrapper() {
        ovk_api = new OvkAPIWrapper(this);
        downloadManager = new DownloadManager(this);
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk_api.setAccessToken(instance_prefs.getString("access_token", ""));
        handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("OpenVK", String.format("Handling API message: %s", msg.what));
                receiveState(msg.what, msg.getData());
            }
        };
        account = new Account(this);
        account.getProfileInfo(ovk_api);
        wall = new Wall();
        wall.getComments(ovk_api, wallPost.owner_id, wallPost.post_id);
    }

    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
            account.parse(data.getString("response"), ovk_api);
            setBottomPanel();
        } else if (message == HandlerMessages.WALL_ALL_COMMENTS) {
            comments = wall.parseComments(this, downloadManager, data.getString("response"));
            createCommentsAdapter(comments);
        } else if (message == HandlerMessages.COMMENT_AVATARS) {
            loadCommentatorAvatars();
        }
    }

    private void loadCommentatorAvatars() {
    }

    private void createCommentsAdapter(ArrayList<Comment> comments) {
        //TextView no_comments_text = findViewById(R.id.no_comments_text);
        this.comments = comments;
        commentsAdapter = new CommentsAdapter(this, comments);
        comments_rv = (RecyclerView) findViewById(R.id.comments_rv);
//        if(comments.size() > 0) {
//            no_comments_text.setVisibility(GONE);
//            commentsView.setVisibility(VISIBLE);
//        } else {
//            no_comments_text.setVisibility(VISIBLE);
//            commentsView.setVisibility(GONE);
//        }
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        comments_rv.setLayoutManager(llm);
        comments_rv.setAdapter(commentsAdapter);
    }

    private void setMonetTheme() {
        if(Global.checkMonet(this)) {
            MaterialToolbar toolbar = findViewById(R.id.app_toolbar);
            if (!isDarkTheme) {
                toolbar.setBackgroundColor(Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(600)).toLinearSrgb().toSrgb().quantize8());
                getWindow().setStatusBarColor(Objects.requireNonNull(getMonet().getMonetColors().getAccent1().get(700)).toLinearSrgb().toSrgb().quantize8());
            }
        }
    }

    private void setAppBar() {
        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle(getResources().getString(R.string.post_title));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if(!Global.checkMonet(this)) {
            TypedValue typedValue = new TypedValue();
            boolean isDarkThemeEnabled = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkThemeEnabled) {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.background, typedValue, true);
            } else {
                getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
            }
            getWindow().setStatusBarColor(typedValue.data);
        }
    }

    private void setBottomPanel() {
        bottomPanel = (SendTextBottomPanel) findViewById(R.id.sendTextBottomPanel);
        bottomPanel.setOnSendButtonClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(bottomPanel.getText().length() > 0) {
                    try {
                        last_sended_comment = new Comment(0, account.id, String.format("%s %s", account.first_name, account.last_name),
                                (int) (System.currentTimeMillis() / 1000), bottomPanel.getText());
                        wall.createComment(ovk_api, wallPost.owner_id, wallPost.post_id, bottomPanel.getText());
                        if(comments == null) {
                            comments = new ArrayList<>();
                        }
                        comments.add(last_sended_comment);
                        if(commentsAdapter == null) {
                            commentsAdapter = new CommentsAdapter(WallPostActivity.this, comments);
                            comments_rv.setAdapter(commentsAdapter);
                        } else {
                            commentsAdapter.notifyDataSetChanged();
                        }
                        bottomPanel.clearText();
                        comments_rv.smoothScrollToPosition(comments.size() -1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        ((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                AppCompatImageButton send_btn = bottomPanel.findViewById(R.id.send_btn);
                if(bottomPanel.getText().length() > 0) {
                    send_btn.setEnabled(true);
                } else {
                    send_btn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).getLineCount() > 4) {
                    ((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).setLines(4);
                } else {
                    ((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).setLines(((TextInputEditText) bottomPanel.findViewById(R.id.send_text)).getLineCount());
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    public void openPost(WallPost post) {
        TextView poster_name = findViewById(R.id.post_author_label);
        TextView post_text = findViewById(R.id.post_text);
        TextView post_likes = findViewById(R.id.like_btn);
        TextView post_repost = findViewById(R.id.repost_btn);
        poster_name.setText(post.name);
        post_text.setText(post.text);
        Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dt_midnight);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        ((TextView) findViewById(R.id.post_info)).setText(post.info);
        if(post.text.length() > 0) {
            post_text.setVisibility(View.VISIBLE);
            String text = post.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                    .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
            post_text.setText(Global.formatLinksAsHtml(text));
        } else {
            post_text.setVisibility(View.GONE);
        }
        post_text.setMovementMethod(LinkMovementMethod.getInstance());

        poster_name.setTypeface(Global.getFlexibleTypeface(this, 500));
        post_likes.setText(String.format("%s", post.counters.likes));
        post_repost.setText(String.format("%s", post.counters.reposts));

        TypedValue accentColor = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, accentColor, true);
        int color;
        if(post.counters.isLiked) {
            color = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorAccent, Color.BLACK);
            post_likes.setSelected(true);
        } else {
            color = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorControlNormal, Color.BLACK);
            post_likes.setSelected(false);
        }
        post_likes.setTextColor(color);
        setTextViewDrawableColor(post_likes, color);

        setTextViewDrawableColor(post_repost, MaterialColors.getColor(this, androidx.appcompat.R.attr.colorControlNormal, Color.BLACK));
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    public void addAuthorMention(int position) {
    }
}
