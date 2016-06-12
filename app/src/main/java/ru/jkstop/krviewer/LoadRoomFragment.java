package ru.jkstop.krviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Фрагмент загруженность помещений
 */
public class LoadRoomFragment extends Fragment {

    public static LoadRoomFragment newInstance(){
        System.out.println("new rooms fragment");
        return new LoadRoomFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_load_rooms, container, false);
        return fragmentView;
    }
}
