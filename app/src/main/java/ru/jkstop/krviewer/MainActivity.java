package ru.jkstop.krviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import ru.jkstop.krviewer.items.User;

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
    public static final int CLOSE_DRAWER = 103;

    private static final int SEARCH_USER_TASK = 20;

    private static final int COLLECTION_ROOMS = 10;
    private static final int COLLECTION_USERS = 11;

    private Context context;

    private DrawerLayout drawer;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton mainFAB;
    private TextView accountName, accountEmail;
    private ImageView accountImage, accountExit;
    private MenuItem serverConnectStatus;
    private AppCompatSpinner spinner;

    private ViewPagerAdapter viewPagerAdapter;

    private static ServerConnect.Callback serverConnectionCallback;

    public static Handler handler;

    private GoogleApiClient mGoogleApiClient;



    public enum  TabTitle{
        currentLoad (App.getAppContext().getString(R.string.title_tab_current_load)),
        historyLoad (App.getAppContext().getString(R.string.title_tab_history_load)),
        localusers (App.getAppContext().getString(R.string.title_tab_local_users)),
        serverUsers (App.getAppContext().getString(R.string.title_tab_server_users));

        private static final Map<String, TabTitle> map = new HashMap<>();
        static {
            for (TabTitle en : values()) {
                map.put(en.text, en);
            }
        }

        public static TabTitle valueFor(String name) {
            return map.get(name);
        }

        final String text;
        TabTitle(final String title){
            this.text = title;
        }

        @Override
        public String toString() {
            return text;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //JournalDB.addJournalItem(new JournalItem().setRoomName("45").setAccess(Room.ACCESS_CARD).setUserName("dfs").setOpenTime(System.currentTimeMillis()-1000000000));
        //UsersDB.addUser(new User().setInitials("dsfdfs").setRadioLabel("asdfgc111"));

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
                        switch (TabTitle.valueFor(tab.getText().toString())){
                            case currentLoad:
                                getSupportActionBar().setTitle(getString(R.string.menu_navigation_rooms_load));
                                getSupportActionBar().setDisplayShowCustomEnabled(false);
                                eraseTempSearchFiles();
                                break;
                            case historyLoad:
                                getSupportActionBar().setTitle(null);
                                setSpinnerViewEnabled();
                                break;
                            case localusers:
                                getSupportActionBar().setTitle(getString(R.string.menu_navigation_users));
                                getSupportActionBar().setDisplayShowCustomEnabled(false);
                                eraseTempSearchFiles();
                                break;
                            case serverUsers:
                                setSearchViewEnabled();
                                break;
                            default:
                                getSupportActionBar().setDisplayShowCustomEnabled(false);

                                eraseTempSearchFiles();
                                break;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                    }
                });

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.main_swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ServerConnect.getConnection(null, ServerReader.READ_ALL, serverConnectionCallback);
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
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(getCurrentFocus(),"Нет подключения к серверу!", Snackbar.LENGTH_SHORT)
                                .setAction("Настройка", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new DialogSQLSetting().show(getSupportFragmentManager(), "dialog_sql_set");
                                    }
                                }).show();
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
                    case CLOSE_DRAWER:
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    default:
                        break;
                }
            }
        };

        accountName.setOnClickListener(signInClick);
        accountExit.setOnClickListener(signOutClick);

        initActiveUser();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationMenu.setNavigationItemSelectedListener(this);

    }

    private void eraseTempSearchFiles(){
        if (SearchFragment.resultWasDelivered){
            SearchFragment.forceStop();
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


       // searchView.requestFocus();
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
        final ArrayList <String> dates = new ArrayList<String>();
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
        System.out.println("logon result " + resultCode + " request " + requestCode);
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
                viewPagerAdapter.addFragment(LoadRoomFragment.newInstance(), TabTitle.currentLoad.text);
                viewPagerAdapter.addFragment(JournalFragment.newInstance(), TabTitle.historyLoad.text);
                break;
            case COLLECTION_USERS:
                viewPagerAdapter.addFragment(UsersFragment.newInstance(), TabTitle.localusers.text);
                viewPagerAdapter.addFragment(SearchFragment.newInstance(), TabTitle.serverUsers.text);
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
    public void onFileCreated(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_SUBJECT, "Журнал посещений");
        intent.putExtra(Intent.EXTRA_TEXT, "Отправлено из приложения KR Viewer");

        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Выберите приложение..."));
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

        clearDB().start();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);

        SharedPrefs.setActiveAccountID("local");

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

    private Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.main_view_pager + ":" + viewPager.getCurrentItem());
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
            case ServerReader.READ_ALL:
                new ServerReader(ServerReader.READ_ALL, context, this).execute(connection);
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


    @Override
    public void onSuccessServerRead(int task, Object result) {
        System.out.println("SUCCESS READED");
        Snackbar.make(getCurrentFocus(),"Синхронизировано", Snackbar.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
        updateFragments();
    }

    @Override
    public void onErrorServerRead(Exception e) {
        System.out.println("ERROR READED");
        Snackbar.make(getCurrentFocus(),"Ошибка синхронизации", Snackbar.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }
}
