package ru.jkstop.krviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageView;
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

import ru.jkstop.krviewer.adapters.ViewPagerAdapter;
import ru.jkstop.krviewer.databases.DbShare;
import ru.jkstop.krviewer.databases.RoomsDB;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.dialogs.DialogLogOut;
import ru.jkstop.krviewer.dialogs.DialogSQLSetting;
import ru.jkstop.krviewer.items.Room;
import ru.jkstop.krviewer.items.User;

public class MainActivity extends AppCompatActivity implements
        ServerConnect.Callback,
        DialogLogOut.Callback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int SIGN_IN = 100;
    public static final int SERVER_CONNECTED = 101;
    public static final int SERVER_DISCONNECTED = 102;

    private static final int SEARCH_USER_TASK = 20;

    private static final int COLLECTION_ROOMS = 10;
    private static final int COLLECTION_USERS = 11;

    private Context context;

    private DrawerLayout drawer;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton mainFAB;
    private TextView accountName, accountEmail;
    private ImageView accountImage, accountExit;
    private MenuItem serverConnectStatus;

    private ViewPagerAdapter viewPagerAdapter;

    private static ServerConnect.Callback serverConnectionCallback;

    public static Handler handler;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGoogleSingInAPI();

        serverConnectionCallback = this;

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

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        appbar.setExpanded(true, true);
                        System.out.println("tab selected " + tab.getText().toString());
                        switch (tab.getText().toString()){
                            case "Серверные":
                                setSearchViewEnabled();
                                break;
                            default:
                                getSupportActionBar().setDisplayShowCustomEnabled(false);
                                System.out.println("RESULT WAS DELIVERED " + SearchFragment.resultWasDelivered);
                                if (SearchFragment.resultWasDelivered){
                                    SearchFragment.forceStop();
                                }
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                    }
                });


        replaceViewPagerFragments(COLLECTION_ROOMS);

        mainFAB = (FloatingActionButton) findViewById(R.id.main_fab);
        accountName = (TextView) navigationMenu.getHeaderView(0).findViewById(R.id.account_name);
        accountEmail = (TextView) navigationMenu.getHeaderView(0).findViewById(R.id.account_email);
        accountExit = (ImageView)navigationMenu.getHeaderView(0).findViewById(R.id.account_exit);
        accountImage = (ImageView)navigationMenu.getHeaderView(0).findViewById(R.id.account_image);

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case SIGN_IN:
                        initActiveUser();
                        break;
                    case SERVER_CONNECTED:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_done_black_24dp);
                        break;
                    case SERVER_DISCONNECTED:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_off_black_24dp);
                        break;
                    case NetworkUtil.NETWORK_STATUS_NOT_CONNECTED:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_off_black_24dp);
                        break;
                    case NetworkUtil.NETWORK_STATUS_WIFI:
                        //WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
                        //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        //System.out.println("wifi name " + wifiInfo.toString());
                        //System.out.println("wifi ssid " + wifiInfo.getSSID());
                        ServerConnect.getConnection(null, 0, serverConnectionCallback);
                        break;
                    case NetworkUtil.NETWORK_STATUS_MOBILE:
                        serverConnectStatus.setIcon(R.drawable.ic_cloud_off_black_24dp);
                        break;
                    default:
                        break;
                }
            }
        };

        accountName.setOnClickListener(signInClick);
        accountExit.setOnClickListener(signOutClick);

        initActiveUser();

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


        searchView.requestFocus();
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

    private void initGoogleSingInAPI(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

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
            accountImage.setImageResource(R.drawable.ic_account_circle_white_48dp);

            accountExit.setVisibility(View.INVISIBLE);
        }

        accountName.setText(accountNameString);
        accountEmail.setText(accountEmailString);

    }

    private View.OnClickListener signInClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, SIGN_IN);
        }
    };

    private View.OnClickListener signOutClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogLogOut().show(getSupportFragmentManager(), "logout");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case SIGN_IN:
                    final GoogleSignInAccount googleSignInAccount = Auth.GoogleSignInApi.getSignInResultFromIntent(data).getSignInAccount();
                    System.out.println("sign in user " + googleSignInAccount.getDisplayName());

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
                                SharedPrefs.setActiveAccountEmail(googleSignInAccount.getEmail());

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

    private void replaceViewPagerFragments(int fragmentCollection){
        viewPagerAdapter.clearFragments();
        viewPagerAdapter.notifyDataSetChanged();
        switch (fragmentCollection){
            case COLLECTION_ROOMS:
                viewPagerAdapter.addFragment(LoadRoomFragment.newInstance(), "Текущая");
                viewPagerAdapter.addFragment(JournalFragment.newInstance(), "История");
                break;
            case COLLECTION_USERS:
                viewPagerAdapter.addFragment(UsersFragment.newInstance(),"Локальные");
                viewPagerAdapter.addFragment(SearchFragment.newInstance(),"Серверные");
                break;
            default:
                break;
        }
        viewPagerAdapter.notifyDataSetChanged();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_navigation_rooms_load:
                replaceViewPagerFragments(COLLECTION_ROOMS);
                break;
            case R.id.menu_navigation_users:
                replaceViewPagerFragments(COLLECTION_USERS);
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

        ServerConnect.getConnection(null, 0, this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_server_status:
                new DialogSQLSetting().show(getSupportFragmentManager(), "dialog_sql_set");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);

        SharedPrefs.setActiveAccountID("local");

        initActiveUser();
    }

    @Override
    public void onServerConnected(Connection connection, int callingTask) {
        System.out.println("server connected " + connection);
        handler.sendEmptyMessage(SERVER_CONNECTED);
        switch (callingTask){
            case SEARCH_USER_TASK:
                SearchFragment.cancelSearchTask();
                SearchFragment.startSearchTask(connection);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerConnectException(Exception e) {
        System.out.println("connect exception " + e.getLocalizedMessage());
        handler.sendEmptyMessage(SERVER_DISCONNECTED);

    }


}
