package ru.jkstop.krviewer.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для вкладок
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        System.out.println("getfragment");
        return mFragmentList.get(position);
    }

   // @Override
   // public void destroyItem(ViewGroup container, int position, Object object) {
   //     super.destroyItem(container, position, object);
        //FragmentManager manager = ((Fragment) object).getFragmentManager();
        //FragmentTransaction trans = manager.beginTransaction();
        //trans.remove((Fragment) object);
        //trans.commit();
   // }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position >= getCount()){
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
            System.out.println("destroyed " + object.toString());
        }

        super.destroyItem(container, position, object);

    }

    @Override
    public int getItemPosition(Object object) {
        System.out.println("try get item position " + object.toString());
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        System.out.println("getCount " + mFragmentList.size());
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
        System.out.println("fragment added " + fragment.toString());
    }

    public void clearFragments(){
        mFragmentList.clear();
        mFragmentTitleList.clear();
        System.out.println("fragments cleared " + mFragmentList.size());
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
