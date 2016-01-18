package gamemei.qiyun.com.gamemei.adapter;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MyFragmentPagerAdapter extends PagerAdapter {
	private FragmentManager fragmentmanager;

	private ArrayList<Fragment> list;

	public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {

		this.list = list;
		this.fragmentmanager = fm;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(list.get(position).getView());
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = list.get(position);
		if (!fragment.isAdded()) {
			FragmentTransaction transaction = fragmentmanager
					.beginTransaction();
			transaction.add(fragment, fragment.getClass().getSimpleName());
			transaction.commit();
			fragmentmanager.executePendingTransactions();
		}
		if (fragment.getView().getParent() == null) {
			container.addView(fragment.getView());
		}
		return fragment.getView();
	}

}
