package uk.openvk.android.refresh.ui.view.pager.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import uk.openvk.android.refresh.ui.core.fragments.app.friends.FriendRequestsFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.friends.FriendsListFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.pub_pages.AboutFragment;

public class FriendsPagerAdapter extends FragmentStateAdapter {

    private String[] frgData;
    private int pages_count;

    ArrayList<Fragment> fragments;

    public FriendsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        fragments = new ArrayList<>();
    }

    public FriendsPagerAdapter(@NonNull Fragment fragment, String[] frgData, int pages_count) {
        super(fragment);
        fragments = new ArrayList<>();
        this.frgData = frgData;
        this.pages_count = pages_count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("key", frgData[position]);
        Fragment fragment;
        if(position == 0) {
            fragment = new FriendsListFragment();
        } else {
            fragment = new FriendRequestsFragment();
        }
        fragment.setArguments(bundle);
        fragments.add(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
}
