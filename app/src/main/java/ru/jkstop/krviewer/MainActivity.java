package ru.jkstop.krviewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private FloatingActionButton mainFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationMenu = (NavigationView) findViewById(R.id.main_navigation_view);
        drawer = (DrawerLayout)findViewById(R.id.main_navigation_drawer);
        mainFAB = (FloatingActionButton) findViewById(R.id.main_fab);

        mainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationMenu.setNavigationItemSelectedListener(this);

        replaceFragment(LoadRoomFragment.newInstance(), getString(R.string.menu_navigation_rooms_load));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void replaceFragment (Fragment fragment, String tag){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_frame, fragment, tag)
                .commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_navigation_rooms_load:
                if (getSupportFragmentManager().findFragmentByTag(getString(R.string.menu_navigation_rooms_load)) != null){
                    break;
                }else{
                    replaceFragment(LoadRoomFragment.newInstance(), getString(R.string.menu_navigation_rooms_load));
                }
                break;
            case R.id.menu_navigation_users:
                if (getSupportFragmentManager().findFragmentByTag(getString(R.string.menu_navigation_users)) != null){
                    break;
                }else{
                    replaceFragment(UsersFragment.newInstance(), getString(R.string.menu_navigation_users));
                }
                break;
            case R.id.menu_navigation_journal:
                if (getSupportFragmentManager().findFragmentByTag(getString(R.string.menu_navigation_journal)) != null){
                    break;
                }else{
                    replaceFragment(JournalFragment.newInstance(), getString(R.string.menu_navigation_journal));
                }
                break;
            case R.id.menu_navigation_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
