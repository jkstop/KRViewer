package ru.jkstop.krviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.jkstop.krviewer.adapters.ViewPagerAdapter;
import ru.jkstop.krviewer.databases.DbShare;
import ru.jkstop.krviewer.databases.JournalDB;
import ru.jkstop.krviewer.databases.RoomsDB;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.dialogs.DialogLogOut;
import ru.jkstop.krviewer.dialogs.DialogSQLSetting;
import ru.jkstop.krviewer.fragments.JournalFragment;
import ru.jkstop.krviewer.fragments.LoadRoomFragment;
import ru.jkstop.krviewer.fragments.SearchFragment;
import ru.jkstop.krviewer.fragments.UsersFragment;
import ru.jkstop.krviewer.items.App;
import ru.jkstop.krviewer.items.User;
import ru.jkstop.krviewer.utils.NetworkUtil;

public class MainActivity extends AppCompatActivity implements
        ServerConnect.Callback,
        DialogLogOut.Callback,
        ServerReader.Callback,
        JournalDB.backupJournalToFile.Callback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int SIGN_IN = 100;
    public static final int SERVER_CONNECTED = 101;
    public static final int SERVER_DISCONNECTED = 102;
    private static final int CLOSE_DRAWER = 103;

    private static final int SEARCH_USER_TASK = 20;
    private static final int READ_ALL_TASK = 21;

    private static final int COLLECTION_ROOMS = 10;
    private static final int COLLECTION_USERS = 11;

    private Context context;

    private DrawerLayout drawer;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView accountName, accountEmail;
    private ImageView accountImage, accountExit;
    private MenuItem serverConnectStatus;
    private AppCompatSpinner spinner;

    private ViewPagerAdapter viewPagerAdapter;

    private static ServerConnect.Callback serverConnectionCallback;

    public static Handler handler;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverConnectionCallback = this;

        initGoogleSingInAPI();

        initHandler();

        initUI();

        initActiveUser();

        replaceViewPagerFragments(COLLECTION_ROOMS);
    }

    public enum TabTitles {
        TitleCurrentLoad(App.getAppContext().getString(R.string.title_tab_current_load)),
        TitleHistoryLoad(App.getAppContext().getString(R.string.title_tab_history_load)),
        TitleAddedUsers(App.getAppContext().getString(R.string.title_tab_local_users)),
        TitleAllUsers(App.getAppContext().getString(R.string.title_tab_server_users));

        private static final Map<String, TabTitles> map = new HashMap<>();
        static {
            for (TabTitles en : values()) {
                map.put(en.text, en);
            }
        }

        public static TabTitles valueFor(String name) {
            return map.get(name);
        }

        final String text;
        TabTitles(final String title){
            this.text = title;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private void initUI(){
        final AppBarLayout appbar = (AppBarLayout)findViewById(R.id.main_appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationMenu = (NavigationView) findViewById(R.id.main_navigation_view);

        context = this;
        drawer = (DrawerLayout)findViewById(R.id.main_navigation_drawer);
        viewPager = (ViewPager)findViewById(R.id.main_view_pager);
        tabLayout = (TabLayout)findViewById(R.id.main_tabs);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.main_swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ServerConnect.getConnection(null, READ_ALL_TASK, serverConnectionCallback);
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        appbar.setExpanded(true, true);
                        switch (TabTitles.valueFor(tab.getText().toString())){
                            case TitleCurrentLoad:
                                getSupportActionBar().setTitle(getString(R.string.menu_navigation_rooms_load));
                                getSupportActionBar().setDisplayShowCustomEnabled(false);
                                eraseTempSearchFiles();
                                break;
                            case TitleHistoryLoad:
                                getSupportActionBar().setTitle(null);
                                setSpinnerViewEnabled();
                                break;
                            case TitleAddedUsers:
                                getSupportActionBar().setTitle(getString(R.string.menu_navigation_users));
                                getSupportActionBar().setDisplayShowCustomEnabled(false);
                                eraseTempSearchFiles();
                                break;
                            case TitleAllUsers:
                                setSearchViewEnabled();
                                break;
                            default:
                                getSupportActionBar().setDisplayShowCustomEnabled(false);
                                eraseTempSearchFiles();
                                break;
                        }
                    }
                });


        accountName = (TextView) navigationMenu.getHeaderView(0).findViewById(R.id.account_name);
        accountEmail = (TextView) navigationMenu.getHeaderView(0).findViewById(R.id.account_email);
        accountExit = (ImageView)navigationMenu.getHeaderView(0).findViewById(R.id.account_exit);
        accountImage = (ImageView)navigationMenu.getHeaderView(0).findViewById(R.id.account_image);
        accountName.setOnClickListener(signInClick);
        accountExit.setOnClickListener(signOutClick);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationMenu.setNavigationItemSelectedListener(this);
    }

    private void initHandler(){
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case SIGN_IN:
                        initActiveUser();
                        break;
                    case SERVER_CONNECTED:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_done_24dp);
                        break;
                    case SERVER_DISCONNECTED:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_off_24dp);
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(getCurrentFocus(),getString(R.string.snack_no_server_connect), Snackbar.LENGTH_SHORT)
                                .setAction(getString(R.string.snack_settings), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new DialogSQLSetting().show(getSupportFragmentManager(), getString(R.string.title_dialog_sql_connect));
                                    }
                                }).show();
                        break;
                    case NetworkUtil.NETWORK_STATUS_NOT_CONNECTED:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_off_24dp);
                        break;
                    case NetworkUtil.NETWORK_STATUS_WIFI:
                        ServerConnect.getConnection(null, 0, serverConnectionCallback);
                        break;
                    case NetworkUtil.NETWORK_STATUS_MOBILE:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_off_24dp);
                        break;
                    case CLOSE_DRAWER:
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initGoogleSingInAPI(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private View.OnClickListener signInClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, SIGN_IN);
        }
    };

    private View.OnClickListener signOutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogLogOut().show(getSupportFragmentManager(), getString(R.string.title_dialog_log_out));
        }
    };

    private void initActiveUser(){
        User activeUser = UsersDB.getUser(SharedPrefs.getActiveAccountID());
        String accountNameString, accountEmailString;

        if (activeUser!=null){
            accountNameString = activeUser.getInitials();
            accountEmailString = activeUser.getDivision();

            Picasso.with(context).load(new File(activeUser.getPhotoPath())).into(accountImage);

            accountExit.setVisibility(View.VISIBLE);
        } else {
            accountNameString = getString(R.string.navigation_head_account_log_off);
            accountEmailString = "";
            accountImage.setImageResource(R.drawable.ic_account_circle_48dp);

            accountExit.setVisibility(View.INVISIBLE);
        }

        accountName.setText(accountNameString);
        accountEmail.setText(accountEmailString);
    }

    private void replaceViewPagerFragments(int fragmentCollection){
        viewPagerAdapter.clearFragments();
        viewPagerAdapter.notifyDataSetChanged();
        switch (fragmentCollection){
            case COLLECTION_ROOMS:
                viewPagerAdapter.addFragment(LoadRoomFragment.newInstance(), TabTitles.TitleCurrentLoad.text);
                viewPagerAdapter.addFragment(JournalFragment.newInstance(), TabTitles.TitleHistoryLoad.text);
                break;
            case COLLECTION_USERS:
                viewPagerAdapter.addFragment(UsersFragment.newInstance(), TabTitles.TitleAddedUsers.text);
                viewPagerAdapter.addFragment(SearchFragment.newInstance(), TabTitles.TitleAllUsers.text);
                break;
            default:
                break;
        }
        viewPagerAdapter.notifyDataSetChanged();

        tabLayout.getTabAt(0).select();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment currentFragment = getCurrentFragment();

        switch (item.getItemId()){
            case R.id.menu_navigation_rooms_load:
                if (currentFragment instanceof LoadRoomFragment || currentFragment instanceof JournalFragment){
                    break;
                }else {
                    replaceViewPagerFragments(COLLECTION_ROOMS);
                }
                break;
            case R.id.menu_navigation_users:
                if (currentFragment instanceof UsersFragment || currentFragment instanceof SearchFragment){
                    break;
                }else {
                    replaceViewPagerFragments(COLLECTION_USERS);
                }
                break;
            case R.id.menu_navigation_send:
                new JournalDB.backupJournalToFile(context, this).execute();
                break;
            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        serverConnectStatus = menu.findItem(R.id.menu_main_server_status);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!wifiInfo.getSSID().contains("FA_STAFF")){
            Snackbar.make(getCurrentFocus(),getString(R.string.snack_no_wifi_connect), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.snack_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    }).show();
            handler.sendEmptyMessage(NetworkUtil.NETWORK_STATUS_NOT_CONNECTED);
        } else {
            ServerConnect.getConnection(null, 0, this);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_server_status:
                new DialogSQLSetting().show(getSupportFragmentManager(), getString(R.string.title_dialog_sql_connect));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSearchViewEnabled(){
        getSupportActionBar().setCustomView(R.layout.view_searchbar);
        final SearchView searchView = (SearchView) getSupportActionBar().getCustomView();
        ImageView cancelSearch = (ImageView)searchView.findViewById(R.id.search_close_btn);
        cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment.forceStop();
                searchView.setQuery(null, false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length()>=3 && newText.length()<=6){
                    SearchFragment.searchText = newText;
                    ServerConnect.getConnection(null, SEARCH_USER_TASK, serverConnectionCallback);
                }
                return false;
            }
        });

        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    private void setSpinnerViewEnabled(){
        getSupportActionBar().setCustomView(R.layout.view_spinnerbar);
        spinner = (AppCompatSpinner) getSupportActionBar().getCustomView();
        final ArrayList <String> dates = new ArrayList<>();
        dates.addAll(JournalDB.getDates());

        SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item, dates);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                try {
                    JournalFragment.loadJournalTask(new SimpleDateFormat("dd MMM yyyy", new Locale("RU", "ru")).parse(dates.get(position))).start();
                } catch (ParseException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case SIGN_IN:
                    final GoogleSignInAccount googleSignInAccount = Auth.GoogleSignInApi.getSignInResultFromIntent(data).getSignInAccount();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Bitmap bitmap = BitmapFactory.decodeStream(new URL(String.valueOf(googleSignInAccount.getPhotoUrl())).openConnection().getInputStream());
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                byte[] byteArray = byteArrayOutputStream .toByteArray();

                                UsersDB.addUser(new User()
                                        .setInitials(googleSignInAccount.getDisplayName())
                                        .setDivision(googleSignInAccount.getEmail())
                                        .setRadioLabel(googleSignInAccount.getId())
                                        .setPhotoBinary(Base64.encodeToString(byteArray, Base64.NO_WRAP)));

                                SharedPrefs.setActiveAccountID(googleSignInAccount.getId());

                                handler.sendEmptyMessage(SIGN_IN);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFileCreated(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_mail_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.send_mail_text));

        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, getString(R.string.send_mail_chooser)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbShare.closeDB();
        ServerConnect.closeConnection();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onSigningOut() {

        clearDB().start();

        Auth.GoogleSignInApi.signOut(googleApiClient);
        Auth.GoogleSignInApi.revokeAccess(googleApiClient);

        SharedPrefs.setActiveAccountID(getString(R.string.log_on));

        initActiveUser();
    }

    private Thread clearDB(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                RoomsDB.clear();
                UsersDB.clear();
                JournalDB.clear();
                updateFragments();
            }
        });
    }

    private void updateFragments(){
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof LoadRoomFragment){
            LoadRoomFragment.loadRoomsTask().start();
        } else if (fragment instanceof UsersFragment){
            UsersFragment.loadUsersTask().start();
        } else if (fragment instanceof JournalFragment){
            try {
                JournalFragment.loadJournalTask(new SimpleDateFormat("dd MMM yyyy", new Locale("RU", "ru")).parse(spinner.getSelectedItem().toString())).start();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        handler.sendEmptyMessage(CLOSE_DRAWER);
    }

    private void eraseTempSearchFiles(){
        if (SearchFragment.resultWasDelivered){
            SearchFragment.forceStop();
        }
    }

    private Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.main_view_pager + ":" + viewPager.getCurrentItem());
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        handler.sendEmptyMessage(SERVER_CONNECTED);
        switch (callingTask){
            case SEARCH_USER_TASK:
                SearchFragment.cancelSearchTask();
                SearchFragment.startSearchTask(connection);
                break;
            case READ_ALL_TASK:
                new ServerReader(context, this).execute(connection);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerConnectException(Exception e) {
        handler.sendEmptyMessage(SERVER_DISCONNECTED);
    }

    @Override
    public void onSuccessServerRead() {
        Snackbar.make(getCurrentFocus(), getString(R.string.snack_success_synch), Snackbar.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
        updateFragments();
    }

    @Override
    public void onErrorServerRead(Exception e) {
        Snackbar.make(getCurrentFocus(), getString(R.string.snack_error_synch), Snackbar.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }
}
