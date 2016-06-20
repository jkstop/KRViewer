package ru.jkstop.krviewer;

import android.content.Context;
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
import java.sql.SQLException;
import java.util.ArrayList;

import ru.jkstop.krviewer.adapters.AdapterUsersList;
import ru.jkstop.krviewer.databases.UsersDB;
import ru.jkstop.krviewer.dialogs.DialogUserPhoto;
import ru.jkstop.krviewer.items.ImageSaver;
import ru.jkstop.krviewer.items.User;

/**
 * Фрагмент серверные пользователи
 */
public class SearchFragment extends Fragment implements RecyclerItemClickListener{

    private Context context;

    public static String searchText;
    public static boolean resultWasDelivered = false;

    public static ArrayList<User> mUsersList;
    public static AdapterUsersList mAdapter;

    private static ProgressBar mProgressBar;

    public static SearchTask searchTask;

    public static SearchFragment newInstance(){
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        mUsersList = new ArrayList<>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.recycler_main, container, false);
        mProgressBar = (ProgressBar) fragmentView.findViewById(R.id.main_progress_bar);
        RecyclerView mRecycler = (RecyclerView) fragmentView.findViewById(R.id.main_recycler);
        mAdapter = new AdapterUsersList(context, mUsersList, 0, this);

        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new GridLayoutManager(context, 1));

        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBar.requestFocus();

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

    public static void clearTempFiles(){
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
        mUsersList.clear();
        mAdapter.notifyDataSetChanged();
        resultWasDelivered = false;
    }


    public static class SearchTask extends AsyncTask<Connection,User,Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mUsersList!=null) mUsersList.clear();
            mProgressBar.setVisibility(View.VISIBLE);
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
                        ResultSet resultPhoto = params[0].createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
                                .executeQuery("SELECT " + ServerConnect.COLUMN_ALL_STAFF_PHOTO
                                        + " FROM " + ServerConnect.ALL_STAFF_TABLE
                                        + " WHERE " + ServerConnect.COLUMN_ALL_STAFF_TAG
                                        + " = '" + resultSet.getString(ServerConnect.COLUMN_ALL_STAFF_TAG) + "'");
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
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(User... values) {
            super.onProgressUpdate(values);
            mUsersList.add(values[0]);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        DialogUserPhoto.newInstance(mUsersList.get(position).getInitials(), mUsersList.get(position).getRadioLabel())
                .show(getChildFragmentManager(), "user_photo");
    }
}
