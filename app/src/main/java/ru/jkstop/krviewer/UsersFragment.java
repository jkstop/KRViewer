package ru.jkstop.krviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Фрагмент пользователи
 */
public class UsersFragment extends Fragment {

    public static UsersFragment newInstance(){
        System.out.println("new users fragment");
        return new UsersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_users, container, false);
        return fragmentView;
    }
}
