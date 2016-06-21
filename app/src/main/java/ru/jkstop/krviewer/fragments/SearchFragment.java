package ru.jkstop.krviewer.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import ru.jkstop.krviewer.items.App;
import ru.jkstop.krviewer.R;
import ru.jkstop.krviewer.ServerConnect;
import ru.jkstop.krviewer.adapters.AdapterUsersList;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.dialogs.DialogUserPhoto;
import ru.jkstop.krviewer.interfaces.RecyclerItemClickListener;
import ru.jkstop.krviewer.items.ImageSaver;
import ru.jkstop.krviewer.items.User;

/**
 * Фрагмент серверные пользователи
 */
public class SearchFragment extends Fragment implements RecyclerItemClickListener {

    private Context context;

    public static String searchText;
    public static boolean resultWasDelivered = false;

    private RecyclerView recycler;
    private static ArrayList<User> userList;
    private static AdapterUsersList adapterUsersList;

    private static ProgressBar progressBar;

    private static SearchTask searchTask;

    public static SearchFragment newInstance(){
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        userList = new ArrayList<>();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_search)));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);
        progressBar = (ProgressBar) fragmentView.findViewById(R.id.main_progress_bar);
        recycler = (RecyclerView) fragmentView.findViewById(R.id.main_recycler);
        adapterUsersList = new AdapterUsersList(context, userList, this);

        recycler.setAdapter(adapterUsersList);
        recycler.setLayoutManager(new GridLayoutManager(context, getResources().getInteger(R.integer.columns_search)));

        progressBar.setVisibility(View.INVISIBLE);
        progressBar.requestFocus();

        return fragmentView;

    }

    public static void cancelSearchTask(){
        if (searchTask !=null){
            searchTask.cancel(true);
        }
    }

    public static void startSearchTask(Connection connection){
        searchTask = new SearchTask();
        searchTask.execute(connection);
    }

    private static void clearTempFiles(){
        File filesDir = ImageSaver.getCustomPath();
        if (filesDir.isDirectory()){
            File[]files = filesDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void forceStop(){
        cancelSearchTask();
        clearTempFiles();
        userList.clear();
        adapterUsersList.notifyDataSetChanged();
        resultWasDelivered = false;
    }


    private static class SearchTask extends AsyncTask<Connection,User,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (userList !=null) userList.clear();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Connection... params) {
            try {
                ResultSet resultSet = params[0].prepareStatement("SELECT "
                        + ServerConnect.COLUMN_ALL_STAFF_DIVISION + ","
                        + ServerConnect.COLUMN_ALL_STAFF_LASTNAME + ","
                        + ServerConnect.COLUMN_ALL_STAFF_FIRSTNAME + ","
                        + ServerConnect.COLUMN_ALL_STAFF_MIDNAME + ","
                        + ServerConnect.COLUMN_ALL_STAFF_SEX + ","
                        + ServerConnect.COLUMN_ALL_STAFF_TAG
                        + " FROM " + ServerConnect.ALL_STAFF_TABLE
                        + " WHERE " + ServerConnect.COLUMN_ALL_STAFF_LASTNAME
                        + " LIKE '" + searchText + "%'").executeQuery();

                resultWasDelivered = true;

                while (resultSet.next()){
                    if (isCancelled()) break;
                    User user = new User();
                    if (resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_TAG)!=null){
                        ResultSet resultPhoto = params[0].prepareStatement("SELECT " + ServerConnect.COLUMN_ALL_STAFF_PHOTO
                                + " FROM " + ServerConnect.ALL_STAFF_TABLE
                                + " WHERE " + ServerConnect.COLUMN_ALL_STAFF_TAG
                                + " = '" + resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_TAG) + "'",
                                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                                .executeQuery();
                        resultPhoto.first();
                        if (resultPhoto.getRow()!=0){
                            String photo = resultPhoto.getString(ServerConnect.COLUMN_ALL_STAFF_PHOTO);
                            if (photo == null) photo = UsersDB.getBinaryDefaultPhoto();
                            //Сохраняем фото во временную папку. При выходе папка очищается
                            String photoPath = new ImageSaver(App.getAppContext())
                                    .setFileName(resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_TAG))
                                    .save(photo, ImageSaver.DIRECTORY_TEMP);
                            user.setPhotoPath(photoPath);

                        }
                    }
                    publishProgress(user.setInitials(resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_LASTNAME)+ " "
                            + resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_FIRSTNAME) + " "
                            + resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_MIDNAME))
                            .setDivision(resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_DIVISION))
                            .setRadioLabel(resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_TAG)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(User... values) {
            super.onProgressUpdate(values);
            userList.add(values[0]);
            adapterUsersList.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onItemClick(int position) {
        DialogUserPhoto.newInstance(userList.get(position).getInitials(), userList.get(position).getRadioLabel())
                .show(getChildFragmentManager(), getString(R.string.title_dialog_user_photo));
    }
}
