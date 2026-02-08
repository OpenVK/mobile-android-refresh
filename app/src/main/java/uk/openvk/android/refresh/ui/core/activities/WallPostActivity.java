package uk.openvk.android.refresh.ui.core.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.entities.Comment;
import uk.openvk.android.refresh.api.entities.WallPost;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.ui.core.activities.base.BaseNetworkActivity;
import uk.openvk.android.refresh.ui.list.adapters.CommentsAdapter;
import uk.openvk.android.refresh.ui.view.layouts.SendTextBottomPanel;

public class WallPostActivity extends BaseNetworkActivity {
    private boolean isDarkTheme;
    private Toolbar toolbar;
    public ArrayList<Comment> comments;
    private CommentsAdapter commentsAdapter;
    private RecyclerView comments_rv;
    private LinearLayoutManager llm;
    private SendTextBottomPanel bottomPanel;
    private Comment last_sended_comment;
    private WallPost wallPost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (global_prefs.getBoolean("dark_theme", false)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Global.setColorTheme(this, global_prefs.getString("theme_color", "blue"),
                    getWindow());
            Global.setInterfaceFont(this);
            isDarkTheme = global_prefs.getBoolean("dark_theme", false);
            setContentView(R.layout.activity_wall_post_watch);

            if (savedInstanceState != null) {
                wallPost = savedInstanceState.getParcelable("post");
                assert wallPost != null;
                wallPost.counters = savedInstanceState.getParcelable("counters");
            } else if (getIntent().getExtras() != null) {
                ovk_api.account = getIntent().getExtras().getParcelable("account");
                wallPost = getIntent().getExtras().getParcelable("post");
                assert wallPost != null;
                wallPost.counters = getIntent().getExtras().getParcelable("counters");
            }
            setAPIWrapper();
            setAppBar();
            setStatusBarColorAttribute(androidx.appcompat.R.attr.colorPrimaryDark);

            if (wallPost != null)
                openPost(wallPost);
            else
                finish();
        } catch (Exception ignored) {
            finish();
        }
    }

    private void setAPIWrapper() {
        ovk_api.account.getProfileInfo(ovk_api.wrapper);
        ovk_api.wall.getComments(ovk_api.wrapper, wallPost.owner_id, wallPost.post_id);
    }

    public void receiveState(int message, Bundle data) {
        if(data.containsKey("address")) {
            String activityName = data.getString("address");
            if(activityName == null)
                return;
            boolean isCurrentActivity = activityName.equals(getLocalClassName());
            if(!isCurrentActivity)
                return;
        }
        if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
            ovk_api.account.parse(data.getString("response"), ovk_api.wrapper);
            setBottomPanel();
        } else if (message == HandlerMessages.WALL_ALL_COMMENTS)
            createCommentsAdapter(comments);
        else if (message == HandlerMessages.COMMENT_AVATARS)
            loadCommentatorAvatars();
    }

    private void loadCommentatorAvatars() {
    }

    private void createCommentsAdapter(ArrayList<Comment> comments) {
        //TextView no_comments_text = findViewById(R.id.no_comments_text);
        this.comments = comments;
        commentsAdapter = new CommentsAdapter(this, comments);
        comments_rv = findViewById(R.id.comments_rv);
        /* not working yet xdddd
                if(comments.size() > 0) {
                    no_comments_text.setVisibility(GONE);
                    commentsView.setVisibility(VISIBLE);
                } else {
                    no_comments_text.setVisibility(VISIBLE);
                    commentsView.setVisibility(GONE);
                }
        */
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        comments_rv.setLayoutManager(llm);
        comments_rv.setAdapter(commentsAdapter);
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
    }

    private void setBottomPanel() {
        bottomPanel = findViewById(R.id.sendTextBottomPanel);
        bottomPanel.setOnSendButtonClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                if(!bottomPanel.getText().isEmpty()) {
                    try {
                        last_sended_comment = new Comment();
                        ovk_api.wall.createComment(ovk_api.wrapper, wallPost.owner_id,
                                wallPost.post_id, bottomPanel.getText());
                        if(comments == null) {
                            comments = new ArrayList<>();
                        }
                        comments.add(last_sended_comment);
                        if(commentsAdapter == null) {
                            commentsAdapter = new CommentsAdapter(
                                    WallPostActivity.this, comments);
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
        ((TextInputEditText) bottomPanel.findViewById(R.id.send_text))
                .addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                AppCompatImageButton send_btn = bottomPanel.findViewById(R.id.send_btn);
                send_btn.setEnabled(!bottomPanel.getText().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ((TextInputEditText) bottomPanel.findViewById(R.id.send_text))
                        .setLines(
                                Math.min(
                                        ((TextInputEditText) bottomPanel.findViewById(R.id.send_text))
                                                .getLineCount(), 4
                                )
                        );
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

        if(!post.text.isEmpty()) {
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

        setTextViewDrawableColor(post_repost, MaterialColors.getColor(this,
                androidx.appcompat.R.attr.colorControlNormal, Color.BLACK));
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }
        }
    }
}
