package ru.jkstop.krviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ivsmirnov on 15.06.2016.
 */
public class MainPages extends Fragment {

    private static final String PAGE_NUMBER = "page_number";

    public static final int PAGE_CURRENT_LOAD = 0;
    public static final int PAGE_HISTORY = 1;

    private int page;

    private Context context;

    public static MainPages newInstance (int page){
        MainPages mainPages = new MainPages();
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE_NUMBER, page);
        mainPages.setArguments(bundle);
        return mainPages;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt(PAGE_NUMBER);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView;
        switch (page){
            case PAGE_CURRENT_LOAD:
                fragmentView = inflater.inflate(R.layout.fragment_load_rooms, container, false);
                System.out.println("created load");
                return fragmentView;
            case PAGE_HISTORY:
                fragmentView = inflater.inflate(R.layout.fragment_journal, container, false);
                System.out.println("created history");
                return fragmentView;
            default:
                return super.onCreateView(inflater, container, savedInstanceState);
        }
    }
}
