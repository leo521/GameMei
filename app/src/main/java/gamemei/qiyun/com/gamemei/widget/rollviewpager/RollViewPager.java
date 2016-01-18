package gamemei.qiyun.com.gamemei.widget.rollviewpager;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;

import gamemei.qiyun.com.gamemei.R;

public class RollViewPager extends ViewPager {
    private List<String> titleList;
    private TextView top_news_title;
    private List<String> imgUrlList;
    private List<View> viewList;
    private BitmapUtils bitmapUtils;
    private MyAdapter myAdapter;
    // 记录当前所在页面的索引值
    private int curretPosition = 0;
    private int downX;
    private int downY;
    private RunnableTask runnableTask;
    private OnViewClickListener onViewClickListener;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // 让viewpager按照维护的索引去指向界面
            RollViewPager.this.setCurrentItem(curretPosition);
            // 一直滚动起来
            startRoll();
        }
    };

    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                // 一定不可以让父控件去拦截事件的
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();

                if (Math.abs(moveY - downY) > Math.abs(moveX - downX)) {
                    // 做刷新加载操作
                    // 父控件需要去拦截事件
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // 管理轮播图,跳转模块的逻辑
                    // 由左边向右边滑动 moveX-downX >0
                    // 由右边向左边滑动 moveX-downX <0
                    if ((moveX - downX > 0) && (getCurrentItem() == 0)) {
                        // 父控件拦截事件
                        getParent().requestDisallowInterceptTouchEvent(false);
                    } else if ((moveX - downX > 0)
                            && getCurrentItem() < getAdapter().getCount()) {
                        // 父控件不能拦截事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else if ((moveX - downX < 0)
                            && getCurrentItem() == getAdapter().getCount() - 1) {
                        // 父控件拦截事件
                        getParent().requestDisallowInterceptTouchEvent(false);
                    } else if ((moveX - downX < 0) && getCurrentItem() > 0) {
                        // 父控件不能拦截事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface OnViewClickListener {
        // 为实现的业务逻辑方法
        public void onViewClick(String url);
    }

    public RollViewPager(Context context, final List<View> viewList,
                         OnViewClickListener onViewClickListener) {
        super(context);
        this.viewList = viewList;
        this.onViewClickListener = onViewClickListener;
        bitmapUtils = new BitmapUtils(context);
        runnableTask = new RunnableTask();
        this.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                // 处理文字的逻辑
                top_news_title.setText(titleList.get(arg0));
                // 处理点的逻辑
                for (int i = 0; i < viewList.size(); i++) {
                    if (i == arg0) {
                        viewList.get(arg0).setBackgroundResource(
                                R.drawable.dot_black);
                    } else {
                        viewList.get(i).setBackgroundResource(
                                R.drawable.dot_white);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    public void initTitleList(List<String> titleList, TextView top_news_title) {
        if (titleList != null && top_news_title != null && titleList.size() > 0) {
            // 默认值
            top_news_title.setText(titleList.get(0));
            this.titleList = titleList;
            this.top_news_title = top_news_title;
        }
    }

    // 当移除出界面的时候调用的方法
    @Override
    protected void onDetachedFromWindow() {
        // 移除所有的任务,不发送任何的消息
        handler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }

    // 接受图片链接地址方法
    public void initImgUrlList(List<String> imgUrlList) {
        this.imgUrlList = imgUrlList;
    }

    class RunnableTask implements Runnable {
        @Override
        public void run() {
            curretPosition = (curretPosition + 1) % imgUrlList.size();
            // 发送消息api
            handler.obtainMessage().sendToTarget();
        }
    }

    public void startRoll() {
        // 1,构建数据适配器
        if (myAdapter == null) {
            myAdapter = new MyAdapter();
            setAdapter(myAdapter);
        } else {
            myAdapter.notifyDataSetChanged();
        }
        // 2,滚动起来(1,定时器 2,handler)
        handler.postDelayed(runnableTask, 3000);
    }

    class MyAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return imgUrlList.size();
            // return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            // 加网络下载下来的图片
            View view = View.inflate(getContext(), R.layout.viewpager_item,
                    null);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            // 通过xutils提供的工具类异步下载缓存图片
            bitmapUtils.display(image, imgUrlList.get(position));
            container.addView(view);

            view.setOnTouchListener(new OnTouchListener() {
                private int downX;
                private long downTime;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 轮播图不再滚动,取消维护的任务,发送的消息
                            handler.removeCallbacksAndMessages(null);
                            // 1,按下的坐标点
                            downX = (int) event.getX();
                            downTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_UP:
                            // 让轮播图再次滚动起来
                            startRoll();
                            int upX = (int) event.getX();
                            if (upX == downX
                                    && System.currentTimeMillis() - downTime < 500) {
                                if (onViewClickListener != null) {
                                    onViewClickListener.onViewClick(imgUrlList.get(position));
                                }
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            startRoll();
                            break;
                    }
                    return true;
                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
