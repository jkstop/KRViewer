package ru.jkstop.krviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.MenuInflater;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.sql.Connection;

import ru.jkstop.krviewer.databases.DbShare;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.dialogs.DialogLogOut;
import ru.jkstop.krviewer.items.User;

public class MainActivity extends AppCompatActivity implements
        ServerConnect.Callback,
        DialogLogOut.Callback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int SIGN_IN = 100;
    private static final int SERVER_CONNECTED = 101;
    private static final int SERVER_DISCONNECTED = 102;

    private Context context;

    private DrawerLayout drawer;
    private FloatingActionButton mainFAB;
    private TextView accountName, accountEmail;
    private ImageView accountImage, accountExit;
    private MenuItem serverConnectStatus;

    private static ServerConnect.Callback serverConnectionCallback;

    public static Handler handler;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGoogleSingInAPI();

        serverConnectionCallback = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationMenu = (NavigationView) findViewById(R.id.main_navigation_view);

        context = this;
        drawer = (DrawerLayout)findViewById(R.id.main_navigation_drawer);
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
                        ServerConnect.getConnection("10.38.2.6", 0, serverConnectionCallback);
                        break;
                    case NetworkUtil.NETWORK_STATUS_MOBILE:
                        serverConnectStatus.setIcon(R.drawable.ic_info_black_24dp);
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

        replaceFragment(LoadRoomFragment.newInstance(), getString(R.string.menu_navigation_rooms_load));
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

        ServerConnect.getConnection("10.38.2.6", 0, this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_server_status:
                Toast.makeText(MainActivity.this, "SQL", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onServerConnectException(Exception e) {
        System.out.println("connect exception " + e.getLocalizedMessage());
        handler.sendEmptyMessage(SERVER_DISCONNECTED);

    }
}
