package uk.openvk.android.refresh.ui.core.fragments.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kieronquinn.monetcompat.core.MonetCompat;

import java.util.ArrayList;
import java.util.Objects;

import uk.openvk.android.refresh.Global;
import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.api.Account;
import uk.openvk.android.refresh.api.models.Conversation;
import uk.openvk.android.refresh.ui.core.activities.AppActivity;
import uk.openvk.android.refresh.ui.view.layouts.ErrorLayout;
import uk.openvk.android.refresh.ui.view.layouts.ProgressLayout;
import uk.openvk.android.refresh.ui.list.adapters.ConversationsAdapter;

public class MessagesFragment extends Fragment {
    private View view;
    private ArrayList<Conversation> conversations;
    private RecyclerView conversationsView;
    private ConversationsAdapter conversationsAdapter;
    private LinearLayoutManager llm;
    private SharedPreferences global_prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Global.setInterfaceFont((AppCompatActivity) requireActivity());
        view = inflater.inflate(R.layout.fragment_conversations, container, false);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setVisibility(View.GONE);
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        if(Global.checkMonet(requireContext())) {
            MonetCompat monet = MonetCompat.getInstance();
            boolean isDarkTheme = global_prefs.getBoolean("dark_theme", false);
            if(isDarkTheme) {
                ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setColorSchemeColors(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1().get(100)).toLinearSrgb().toSrgb().quantize8());
            } else {
                ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setColorSchemeColors(
                        Objects.requireNonNull(monet.getMonetColors().getAccent1().get(500)).toLinearSrgb().toSrgb().quantize8());
            }
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setColorSchemeColors(typedValue.data);
        }
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("dark_theme", false)) {
            ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(com.google.android.material.R.color.background_material_dark));
        } else {
            ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setProgressBackgroundColorSchemeColor(getResources().getColor(android.R.color.white));
        }
        ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requireActivity().getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) requireActivity()).refreshConversations(false);
                }
            }
        });
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void createAdapter(Context ctx, ArrayList<Conversation> conversations, Account account) {
        this.conversations = conversations;
        conversationsView = (RecyclerView) view.findViewById(R.id.conversations_rv);
        if(conversationsAdapter == null) {
            conversationsAdapter = new ConversationsAdapter(requireActivity(), this.conversations, account);
            llm = new LinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            conversationsView.setLayoutManager(llm);
            conversationsView.setAdapter(conversationsAdapter);
        } else {
            //conversationsAdapter.setArray(wallPosts);
            conversationsAdapter.notifyDataSetChanged();
        }
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setVisibility(View.VISIBLE);
    }

    public void disableUpdateState() {
        ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setRefreshing(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadAvatars(ArrayList<Conversation> conversations) {
        Context ctx = requireContext();
        conversationsAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshAdapter() {
        if(conversationsAdapter != null) {
            conversationsAdapter.notifyDataSetChanged();
        }
    }

    public void showProgress() {
        ((ErrorLayout) view.findViewById(R.id.error_layout)).setVisibility(View.GONE);
        ((SwipeRefreshLayout) view.findViewById(R.id.messages_swipe_layout)).setVisibility(View.GONE);
        ((ProgressLayout) view.findViewById(R.id.progress_layout)).setVisibility(View.VISIBLE);
    }
}
