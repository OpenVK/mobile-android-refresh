package uk.openvk.android.refresh.user_interface.view.pager.adapters;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import uk.openvk.android.refresh.user_interface.core.fragments.pub_pages.AboutFragment;
import uk.openvk.android.refresh.user_interface.core.fragments.pub_pages.WallFragment;

public class PublicPagerAdapter extends FragmentStateAdapter {

    ArrayList<Fragment> fragments;

    public PublicPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        fragments = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        if(position == 0) {
            fragment = new WallFragment();
        } else {
            fragment = new AboutFragment();
        }
        fragments.add(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
}
