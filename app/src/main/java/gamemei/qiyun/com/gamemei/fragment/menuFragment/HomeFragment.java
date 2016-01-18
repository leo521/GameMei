package gamemei.qiyun.com.gamemei.fragment.menuFragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import gamemei.qiyun.com.gamemei.R;
import gamemei.qiyun.com.gamemei.activity.AttentionActivity;
import gamemei.qiyun.com.gamemei.adapter.NewsFragmentPagerAdapter;
import gamemei.qiyun.com.gamemei.fragment.BaseFragment;
import gamemei.qiyun.com.gamemei.fragment.ExperienceFragment;
import gamemei.qiyun.com.gamemei.fragment.HotFragment;
import gamemei.qiyun.com.gamemei.fragment.InformationFragment;
import gamemei.qiyun.com.gamemei.fragment.MissionFragment;
import gamemei.qiyun.com.gamemei.fragment.QAFragment;
import gamemei.qiyun.com.gamemei.fragment.VideoFragment;
import gamemei.qiyun.com.gamemei.utils.AppUtils;
import gamemei.qiyun.com.gamemei.widget.ColumnHorizontalScrollView;
import gamemei.qiyun.com.gamemei.widget.LineView;

import static gamemei.qiyun.com.gamemei.R.drawable.radio_buttong_bg;

public class HomeFragment extends BaseFragment implements OnClickListener {

    private String TAG = "HomeFragment";
    /**
     * 顶部标签滑动布局
     */
    protected ColumnHorizontalScrollView mColumnHorizontalScrollView;
    /**
     * 顶部切换标签
     */
    protected LinearLayout mRadioGroup_content;
    /**
     * 设置我的关注
     */
    protected LinearLayout ll_subscription;
    protected RelativeLayout rl_column;
    protected ImageView button_subscription;

    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    protected ViewPager mViewPager;
    private View view;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth = 0;
    /**
     * Item宽度
     */
    private int mItemWidth = 0;
    /**
     * 用户选择的新闻分类列表
     */
    protected static ArrayList<String> userChannelList;
    /**
     * 当前选中的栏目
     */
    private int columnSelectIndex = 0;

    Timer timer = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "hfcui =======HomeFragment===onCreateView");
        view = inflater.inflate(R.layout.fragment_home, null);
        initView();
        initData();
        setChangelView();
        showLoading();
        //定时器  1S后关闭loading界面
        if (timer != null) {
            timer.cancel();
        } else {
            timer = new Timer();
            timer.schedule(task, 1000);
        }
        return view;
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                dismissLoading();
            }
            super.handleMessage(msg);
        }
    };

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };


    @Override
    public View initView() {
        mScreenWidth = AppUtils.getWindowsWidth(this.getActivity());
        mItemWidth = mScreenWidth / 6;// 一个Item宽度为屏幕的1/
        mColumnHorizontalScrollView = (ColumnHorizontalScrollView) view.findViewById(R.id.mColumnHorizontalScrollView);
        mRadioGroup_content = (LinearLayout) view.findViewById(R.id.mRadioGroup_content);
        ll_subscription = (LinearLayout) view.findViewById(R.id.ll_subscription);
        button_subscription = (ImageView) view.findViewById(R.id.button_subscription);
        ll_subscription.setOnClickListener(this);
        mViewPager = (ViewPager) view.findViewById(R.id.mViewPager);
        return null;
    }

    /**
     * 当栏目项发生变化时候调用
     */
    private void setChangelView() {
        initColumnData();
    }

    /**
     * 获取Column栏目 数据
     */
    private void initColumnData() {
        //获取到用户关注的条目
        // userChannelList = ((ArrayList<ChannelItem>) ChannelManage.getManage(
        // AppApplication.getApp().getSQLHelper()).getUserChannel());
        initTabColumn();
        initFragment();
    }

    /**
     * 初始化Column栏目项
     */
    private void initTabColumn() {
        mRadioGroup_content.removeAllViews();
        int count = userChannelList.size();
        mColumnHorizontalScrollView.setParam(this.getActivity(), mScreenWidth, mRadioGroup_content, null,
                null, ll_subscription, rl_column);
        for (int i = 0; i < count; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth,
                    ViewPager.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            TextView columnTextView = new TextView(getContext());
            columnTextView.setTextAppearance(getContext(), R.style.top_category_scroll_view_item_text);
            columnTextView.setBackgroundResource(radio_buttong_bg);
            columnTextView.setGravity(Gravity.CENTER);
            columnTextView.setPadding(5, 5, 5, 5);
            columnTextView.setId(i);
            columnTextView.setTextColor(Color.parseColor("#ffffff"));
            columnTextView.setText(userChannelList.get(i));
            Button button = new Button(getContext());
            // columnTextView.setText(userChannelList.get(i).getName());
            if (columnSelectIndex == i) {
                columnTextView.setSelected(true);
            }
            columnTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
                        View localView = mRadioGroup_content.getChildAt(i);
                        if (localView != v)
                            localView.setSelected(false);
                        else {
                            localView.setSelected(true);
                            mViewPager.setCurrentItem(i);
                        }
                    }
                }
            });
            mRadioGroup_content.addView(columnTextView, i, params);

            LineView lineView = new LineView(getContext());
            //  mRadioGroup_content.addView(context,null,null);
        }
    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {
        fragments.clear();// 清空
        int count = userChannelList.size();
        for (int i = 0; i < count; i++) {
            switch (i) {
                case 0:
                    HotFragment hotFragment = new HotFragment();
                    fragments.add(hotFragment);
                    break;
                case 1:
                    InformationFragment informationFragment = new InformationFragment();
                    fragments.add(informationFragment);
                    break;
                case 2:
                    VideoFragment videoFragment = new VideoFragment();
                    fragments.add(videoFragment);
                    break;
                case 3:
                    MissionFragment missionFragment = new MissionFragment();
                    fragments.add(missionFragment);
                    break;
                case 4:
                    QAFragment qaFragment = new QAFragment();
                    fragments.add(qaFragment);
                    break;
                case 5:
                    ExperienceFragment experienceFragment = new ExperienceFragment();
                    fragments.add(experienceFragment);
                    break;
            }
        }
        NewsFragmentPagerAdapter mAdapetr = new NewsFragmentPagerAdapter(
                getChildFragmentManager(), fragments);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mAdapetr);
        mViewPager.setOnPageChangeListener(pageListener);
    }

    /**
     * 选择的Column里面的Tab
     */
    private void selectTab(int tab_postion) {
        columnSelectIndex = tab_postion;
        for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
            View checkView = mRadioGroup_content.getChildAt(tab_postion);
            int k = checkView.getMeasuredWidth();
            int l = checkView.getLeft();
            int i2 = l + k / 2 - mScreenWidth / 2;
            mColumnHorizontalScrollView.smoothScrollTo(i2, 0);
        }
        // 判断是否选中
        for (int j = 0; j < mRadioGroup_content.getChildCount(); j++) {
            View checkView = mRadioGroup_content.getChildAt(j);
            boolean ischeck;
            if (j == tab_postion) {
                ischeck = true;
            } else {
                ischeck = false;
            }
            checkView.setSelected(ischeck);
        }
    }

    /**
     * ViewPager切换监听方法
     */
    public OnPageChangeListener pageListener = new OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            mViewPager.setCurrentItem(position);
            selectTab(position);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initData() {
        //顶部标签添加固定的条目
        userChannelList = new ArrayList<>();
        userChannelList.add("热门");
        userChannelList.add("资讯");
        userChannelList.add("视频");
        userChannelList.add("任务");
        userChannelList.add("问答");
        userChannelList.add("经验");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //跳转到选择我的关注界面
            case R.id.ll_subscription:
                startActivity(new Intent(getActivity(), AttentionActivity.class));
        }
    }
}
