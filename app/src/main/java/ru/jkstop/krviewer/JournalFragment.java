package ru.jkstop.krviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Фрагмент журнал
 */
public class JournalFragment extends Fragment {

    public static JournalFragment newInstance(){
        System.out.println("new journal fragment");
        return new JournalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_journal, container, false);
        return fragmentView;
    }
}
