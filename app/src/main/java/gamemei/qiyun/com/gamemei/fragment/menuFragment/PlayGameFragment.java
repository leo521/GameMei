package gamemei.qiyun.com.gamemei.fragment.menuFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.qiyun.sdk.GMSdk;

import gamemei.qiyun.com.gamemei.R;
import gamemei.qiyun.com.gamemei.bean.PlayGameInfoBean;
import gamemei.qiyun.com.gamemei.fragment.BaseFragment;
import gamemei.qiyun.com.gamemei.utils.AppUtils;
import gamemei.qiyun.com.gamemei.utils.MyHttpUtils;
import gamemei.qiyun.com.gamemei.utils.SharedPreferencesUitl;
import gamemei.qiyun.com.gamemei.widget.rollviewpager.RollViewPager;
import gamemei.qiyun.com.gamemei.widget.xlistview.XListView;

public class PlayGameFragment extends BaseFragment implements XListView.IXListViewListener {
    /**
     * 日志标记
     */
    private String TAG = "PlayGameFragment";
    private View view;
    /**
     * 下拉刷新的ListView
     */
    private XListView xListView;
    /**
     * 放置顶部轮播图所在的线性布局
     */
    @ViewInject(R.id.top_news_viewpager)
    private LinearLayout top_news_viewpager;
    /**
     * 放置轮播图文字所在的textView
     */
    @ViewInject(R.id.top_game_title)
    private TextView top_game_title;
    /**
     * 放置轮播点所在的线性布局
     */
    @ViewInject(R.id.dots_ll)
    private LinearLayout dots_ll;
    /**
     * 存放轮播图文字的集合
     */
    private List<String> titleList = new ArrayList<String>();
    /**
     * 存放轮播图图片链接地址的集合
     */
    private List<String> imgUrlList = new ArrayList<String>();
    /**
     * 放置点所在的集合
     */
    private List<View> viewList = new ArrayList<View>();
    /**
     * 顶部轮播图的View
     */
    private View layout_roll_view;
    /**
     * 游戏列表
     */
    private List<PlayGameInfoBean> gameList = new ArrayList<PlayGameInfoBean>();
    /**
     * Xutils的Bitmap工具类
     */
    private BitmapUtils bitmapUtils;
    private MyAdapter madapter;
    private HttpHandler handler;
    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playgame, null);
        // 初始化gameMeiSDK
        GMSdk.share().init(getActivity());
        initView();
        initData();// 获取数据
        SetXListView();// 设置XListView
        showLoading();
        return view;
    }

    @Override
    public void onResume() {
        initData();// 获取数据
        super.onResume();
    }

    @Override
    public View initView() {
        mHandler = new Handler();
        madapter = new MyAdapter(this);
        bitmapUtils = new BitmapUtils(context);
        // 关联顶部轮播图所在的布局文件
        layout_roll_view = View.inflate(context, R.layout.layout_roll_view, null);
        ViewUtils.inject(this, layout_roll_view);
        //关联顶部热推手游布局文件
        return null;
    }

    /**
     * 设置XListView
     */
    public void SetXListView() {
        xListView = (XListView) view.findViewById(R.id.game_listview);
        xListView.setAdapter(madapter);
        xListView.setPullRefreshEnable(true); // 设置可以下拉刷新和上拉加载
        //如果数据太少则关闭上拉加载
        if (gameList.size() <= 4) {
            xListView.setPullLoadEnable(false);
        }
        xListView.setXListViewListener(this); // 设置监听事件
        xListView.addHeaderView(layout_roll_view); // listView头的添加过程
        // 设置条目可以被点击
        xListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "点击的是" + position,
                        Toast.LENGTH_SHORT).show();
                // TODO 点击跳转到游戏明细页面
            }
        });
    }

    /**
     * 获取网络数据填充UI
     */
    @Override
    public void initData() {
        dismissLoading();
        // 首先获取服务器端的数据缓存正在本地，再去服务器端拿取最新的数据。
        String result = SharedPreferencesUitl.getStringData(context,
                MyHttpUtils.BASE_URL, "");
        if (!TextUtils.isEmpty(result)) {
            processData(result, true);// 解析数据
        } else {
            // 如何本地没有缓存的数据则从服务器获取数据
            getDate();
        }
    }

    /**
     * xUtil获取网络数据
     */
    private void getDate() {
        showLoading();
        requestData(HttpMethod.GET, MyHttpUtils.PLAY_GAME_LIST, null,
                new RequestCallBack<String>() {

                    @Override
                    public void onFailure(HttpException arg0,
                                          String responseInfo) {
                        Toast.makeText(context, "网络出错，请检查网络",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        // 获取成功返回的json串
                        String result = responseInfo.result;
                        // 本地化存储
                        SharedPreferencesUitl.saveStringData(getActivity(),
                                MyHttpUtils.BASE_URL, result);
                        // 解析数据
                        processData(result, true);
                        dismissLoading();//关闭loading界面
                    }
                });
    }

    /**
     * 解析数据 ----GSON
     */
    private void processData(String result, boolean a) {
        // 对照bean解析json
        Gson gson = new Gson();
        PlayGameInfoBean infoBean = gson.fromJson(result,
                PlayGameInfoBean.class);
        gameList.clear();
        for (int i = 0; i < infoBean.games.size(); i++) {
            gameList.add(i, infoBean);
        }
        // 轮播图逻辑代码块
        if (infoBean.games.size() > 0) {
            if (a) {
                titleList.clear();
                imgUrlList.clear();
                for (int i = 0; i < infoBean.games.size(); i++) {
                    titleList.add(infoBean.games.get(i).game_name);
                    imgUrlList.add(MyHttpUtils.PHOTOS_URL
                            + infoBean.games.get(i).game_image_url);
                }
                // 初始化轮播图配套点的方法
                initDot();
                RollViewPager rollViewPager = new RollViewPager(context,
                        viewList,

                        new RollViewPager.OnViewClickListener() {
                            @Override
                            public void onViewClick(String url) {
                                // 点击事件的具体的业务逻辑在此处处理
                                Toast.makeText(context, url, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                // 1,显示文字数据的集合2,显示这段文字的控件
                rollViewPager.initTitleList(titleList, top_game_title);
                // 图片的链接地址所在集合传递给可滚动的viewpager
                rollViewPager.initImgUrlList(imgUrlList);
                rollViewPager.startRoll();// 轮播图开始滚动
                // 轮播图添加至top_news_viewpager
                top_news_viewpager.removeAllViews();
                top_news_viewpager.addView(rollViewPager);
            }
        }
        madapter.notifyDataSetChanged();
    }

    /**
     * 创建轮播图中的点
     */
    private void initDot() {
        dots_ll.removeAllViews();
        viewList.clear();
        for (int i = 0; i < imgUrlList.size(); i++) {
            View view = new View(context);
            if (i == 0) {
                view.setBackgroundResource(R.drawable.dot_black);
            } else {
                view.setBackgroundResource(R.drawable.dot_white);
            }
            LayoutParams layoutParams = new LayoutParams(10, 10);// 1,定义点的宽高
            layoutParams.setMargins(0, 0, 10, 0); // 2,给点设置间距
            // 3,作用当前规则给子控件
            dots_ll.addView(view, layoutParams);
            viewList.add(view);
        }
    }

    /**
     * 下载游戏
     */
    private void DownLoadFiles(String url, String path) {
        HttpUtils httpUtils = new HttpUtils();
        handler = httpUtils.download(url, path, true, true,
                new RequestCallBack<File>() {
                    ProgressDialog progressDialog = new ProgressDialog(
                            getActivity());

                    @Override
                    public void onSuccess(ResponseInfo<File> arg0) {

                    }

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        Toast.makeText(context, "下载失败，请检查网络",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLoading(long total, long current,
                                          boolean isUploading) {
                        super.onLoading(total, current, isUploading);
                        progressDialog.setMax(100); // 设置最大值为100
                        // 设置进度条风格STYLE_HORIZONTAL
                        progressDialog
                                .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setTitle("下载进度");// 设置title文字
                        progressDialog.show();
                        if (progressDialog.getProgress() >= 100) {
                            progressDialog.dismiss();
                        } else {
                            progressDialog
                                    .incrementProgressBy((int) ((double) current
                                            / (double) total * 100));
                        }
                    }
                });
    }

    /**
     * 下拉刷新界面
     */
    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameList.clear();
                getDate();
                madapter.notifyDataSetChanged();
                onLoad();
            }
        }, 2000);
    }

    /**
     * 上拉加载更多
     */
    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                madapter.notifyDataSetChanged();
                onLoad();
            }
        }, 2000);
    }

    /**
     * 加载
     */
    private void onLoad() {
        xListView.stopRefresh();
        xListView.stopLoadMore();
        xListView.setRefreshTime("刚刚");
    }

    /**
     * ListView 适配器
     *
     * @author hfcui
     */
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;

        private MyAdapter(PlayGameFragment fragmentPage2) {
            this.mInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return gameList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // 获取一个在数据集中指定索引的视图来显示数据
        @Override
        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHolder holder = null;
            // 如果缓存convertView为空，则需要创建View
            if (convertView == null) {
                holder = new ViewHolder();
                // 根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.item_playgame_list, null);
                holder.game_image = (ImageView) convertView
                        .findViewById(R.id.game_image);
                holder.game_name = (TextView) convertView
                        .findViewById(R.id.game_name);
                holder.game_desc = (TextView) convertView
                        .findViewById(R.id.game_desc);
                holder.game_heat = (RatingBar) convertView
                        .findViewById(R.id.game_heat);
                holder.download = (LinearLayout) convertView
                        .findViewById(R.id.download_ll);
                // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final PlayGameInfoBean info = gameList.get(position);
            if (info != null) {
                info.games.get(position);

                holder.game_name.setText(info.games.get(position).game_name);
                holder.game_desc.setText(info.games.get(position).game_desc);

                bitmapUtils.display(holder.game_image, MyHttpUtils.PHOTOS_URL
                        + info.games.get(position).getGame_image_url());

            }
            // 下载游戏
            holder.download.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        String gameName = AppUtils.getSDPath() + "/gameMei/"
                                + info.games.get(position).game_name + ".zip";
                        File file = new File(gameName);
                        Log.i("hfcui----file", file.toString());
                        if (!file.exists()) {
                            DownLoadFiles(
                                    MyHttpUtils.DOWNLOAD_URL
                                            + info.games.get(position).game_download_url,
                                    gameName);
                        } else {
                            Toast.makeText(context, "正在加载游戏",
                                    Toast.LENGTH_SHORT).show();
                            if (GMSdk.share().initRuntime()
                                    && GMSdk.share().installGameZip(
                                    info.games.get(position).game_name,
                                    gameName)) {
                                GMSdk.share().runGame(
                                        info.games.get(position).game_name);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
            return convertView;
        }
    }

    // ViewHolder静态类
    static class ViewHolder {
        public ImageView game_image;
        public TextView game_name;
        public TextView game_desc;
        public TextView game_author;
        public RatingBar game_heat;
        public LinearLayout download;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("hfcui", "PlayGameView------onDestroyView");
    }

}