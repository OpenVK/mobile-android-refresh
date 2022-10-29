package uk.openvk.android.refresh.user_interface.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import uk.openvk.android.refresh.R;
import uk.openvk.android.refresh.user_interface.fragments.app.NewsfeedFragment;

public class AppActivity extends AppCompatActivity {
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_layout);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NewsfeedFragment selectedFragment = new NewsfeedFragment();
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_screen, selectedFragment).commit();
        }
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    selectedFragment = new NewsfeedFragment();
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_screen, selectedFragment).commit();
                }
                return false;
            }
        });
    }
}
