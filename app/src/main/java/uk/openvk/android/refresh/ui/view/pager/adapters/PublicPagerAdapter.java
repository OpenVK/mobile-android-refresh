package uk.openvk.android.refresh.ui.view.pager.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import uk.openvk.android.refresh.ui.core.fragments.app.pub_pages.AboutFragment;
import uk.openvk.android.refresh.ui.core.fragments.app.pub_pages.WallFragment;

public class PublicPagerAdapter extends FragmentStateAdapter {

    private String[] frgData;
    private int pages_count;

    ArrayList<Fragment> fragments;

    public PublicPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        fragments = new ArrayList<>();
    }

    public PublicPagerAdapter(@NonNull Fragment fragment, String[] frgData, int pages_count) {
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
            fragment = new WallFragment();
        } else {
            fragment = new AboutFragment();
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
        if(position == 1 && fragments.get(position) instanceof WallFragment) {
            fragments.set(position, AboutFragment.createInstance(position));
        }
        return fragments.get(position);
    }
}
