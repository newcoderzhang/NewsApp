package com.news.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.news.app.Constants;
import com.news.model.NewsListEntity;
import com.news.util.imageloader.ImageLoaderFactory;
import com.news.util.imageloader.ImageLoaderWrapper;
import com.news.util.net.HttpDataCallBack;
import com.news.util.net.NetUtils;
import com.news.net.R;
import com.news.util.base.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/1/4.
 * 新闻列表
 */
public class NewsFragment extends Fragment{

    private String TAG="NewsFragment";
    @Bind(R.id.mlist)
    public RecyclerView mlist;
    @Bind(R.id.swipe_refresh_layout)
    public  SwipeRefreshLayout swipe_refresh_layout;
    private SimpleAdapter adapter;
    public int page=1;
    private List<NewsListEntity.NewsListBody.Pagebean.Contentlist> contentlists=new ArrayList<>();

    private int lastVisibleItem;
    private Toolbar mMainToolbar;
    private String name;
    private String channelId;
    private Activity activity;
    private boolean isFirstLoad=true;

    private ImageLoaderWrapper mImageLoaderWrapper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.i(TAG, name + "------onCreateView()---------");
        View view=inflater.inflate(R.layout.fragment_news,container,false);
        ButterKnife.bind(this, view);
        activity=getActivity();
        initView();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.name= getArguments().getString("name");
        this.channelId= getArguments().getString("channelId");
        LogUtils.i(TAG, name + ":onCreate()");
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            LogUtils.i(TAG, name + ":setUserVisibleHint()");
            if (isFirstLoad){
               initData(page);
             }
        }
    }

    public void initView(){
        mImageLoaderWrapper= ImageLoaderFactory.getLoader();
        mlist.setLayoutManager(new LinearLayoutManager(mlist.getContext()));
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contentlists.clear();//下拉刷新，清空列表
               // adapter.notifyDataSetChanged(); //clear this is create a bug for
                //或者禁止滑動
                loadData(1);
            }
        });

        mlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter == null) return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == adapter.getItemCount()) {
                    swipe_refresh_layout.setRefreshing(true);
                    initData(++page);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = ((LinearLayoutManager) mlist.getLayoutManager()).findLastVisibleItemPosition();
            }
        });

        mlist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (swipe_refresh_layout.isRefreshing()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        LogUtils.i(TAG, name + ":onResume()");

    }


    @Override
    public void onPause() {
        super.onPause();
        LogUtils.i(TAG, name+":onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.i(TAG, name + ":onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, name + ":onDestroy()");
    }

    /**
    * @desc:initdata
    * @author：Administrator on 2016/1/4 16:02
    */
   public void initData(int count){
       if(swipe_refresh_layout!=null){
           swipe_refresh_layout.setRefreshing(true);
       }
       loadData(count);
   }


    public void  loadData(int count){
        String url=Constants.API_NEWS;
        String datetime=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        final Map<String,Object> param=new HashMap<>();
        param.put("showapi_appid", "12041");
        param.put("showapi_sign", "67f7892db890407f95cdf39f870b1234");
        param.put("showapi_timestamp", datetime);
        param.put("channelId", channelId);
        param.put("page",count);
        //param.put("channelName", "国内最新");
            NetUtils.httpResquest(activity, url, param, Constants.HTTP_GET, new HttpDataCallBack() {
                @Override
                public void onStart() {
                    LogUtils.i(TAG,"开始加载数据："+name);
                    //Log.i(TAG, "http start...");
                }

                @Override
                public void processData(Object paramObject, boolean paramBoolean) {
                    Log.i(TAG, "json:" + paramObject.toString());
                    NewsListEntity mNext= JSON.parseObject(paramObject.toString(), NewsListEntity.class);
                    if(adapter==null){
                        contentlists=mNext.getShowapi_res_body().getPagebean().getContentlist();
                        adapter=new SimpleAdapter(getActivity(), contentlists);
                        LogUtils.i(TAG, "initdata() mlist:" + mlist);
                        if (mlist==null)return;
                        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
                        dividerLine.setSize(1);
                        dividerLine.setColor(0x00000000);
                        dividerLine.setSpace(10);
                        mlist.addItemDecoration(dividerLine);
                        mlist.setAdapter(adapter);
                    }else{
                        contentlists.addAll(mNext.getShowapi_res_body().getPagebean().getContentlist());
                        adapter.notifyDataSetChanged();
                    }
                    swipe_refresh_layout.setRefreshing(false);
                }

                @Override
                public void onFinish() {
                    // Log.i(TAG, "http end...");
                    isFirstLoad=false;
                }

                @Override
                public void onFailed() {
                    Log.i(TAG, "http onFail...");
                }
            });
    }

    /**
     * @desc:RecyclerView adapter
     * @author：Administrator on 2016/1/5 15:30
     */
    public class SimpleAdapter extends  RecyclerView.Adapter<SimpleAdapter.ViewHolder>{

        List<NewsListEntity.NewsListBody.Pagebean.Contentlist> contentlists;
        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        SimpleAdapter(Context context,
                      List<NewsListEntity.NewsListBody.Pagebean.Contentlist> items){
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            this.contentlists=items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_news,parent,false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.tv_news_title.setText(contentlists.get(position).getTitle());
            holder.tv_news_source.setText(contentlists.get(position).getSource());
            holder.tv_news_time.setText(contentlists.get(position).getPubDate());
            holder.tv_news_desc.setText(contentlists.get(position).getDesc());
            int size=contentlists.get(position).getImageurls().size();
            if (size==1){
                ImageLoaderWrapper.DisplayOption displayOption = new ImageLoaderWrapper.DisplayOption();
                displayOption.loadingResId = R.mipmap.img_default;
                displayOption.loadErrorResId = R.mipmap.img_error;
                mImageLoaderWrapper.displayImage(holder.iv_news_bigimage, contentlists.get(position).getImageurls().get(0).getUrl(), displayOption);
                holder.ll_image_third.setVisibility(View.GONE);
                holder.ll_image_one.setVisibility(View.VISIBLE);
                holder.iv_news_leftimage.setVisibility(View.GONE);
            }else if(size==3){
                ImageLoaderWrapper.DisplayOption displayOption = new ImageLoaderWrapper.DisplayOption();
                displayOption.loadingResId = R.mipmap.img_default;
                displayOption.loadErrorResId = R.mipmap.img_error;
                mImageLoaderWrapper.displayImage(holder.iv_news_oneimage, contentlists.get(position).getImageurls().get(0).getUrl(), displayOption);
                mImageLoaderWrapper.displayImage(holder.iv_news_twoimage, contentlists.get(position).getImageurls().get(1).getUrl(), displayOption);
                mImageLoaderWrapper.displayImage(holder.iv_news_thirdimage, contentlists.get(position).getImageurls().get(2).getUrl(), displayOption);
                holder.ll_image_third.setVisibility(View.VISIBLE);
                holder.ll_image_one.setVisibility(View.GONE);
                holder.iv_news_leftimage.setVisibility(View.GONE);
            }else if (size==2){
                ImageLoaderWrapper.DisplayOption displayOption = new ImageLoaderWrapper.DisplayOption();
                displayOption.loadingResId = R.mipmap.img_default;
                displayOption.loadErrorResId = R.mipmap.img_error;
                mImageLoaderWrapper.displayImage(holder.iv_news_bigimage, contentlists.get(position).getImageurls().get(0).getUrl(), displayOption);
                holder.ll_image_third.setVisibility(View.GONE);
                holder.ll_image_one.setVisibility(View.VISIBLE);
                holder.iv_news_leftimage.setVisibility(View.GONE);
            }else{
                //清空图片
                holder.ll_image_third.setVisibility(View.GONE);
                holder.ll_image_one.setVisibility(View.GONE);
                holder.iv_news_leftimage.setVisibility(View.GONE);
            }

            holder.tv_news_imageNum.setText("("+size+")");

            //设置背景
            holder.mView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return contentlists==null?0:contentlists.size();
        }

        public class ViewHolder extends  RecyclerView.ViewHolder {
            public final View mView;
            public final TextView tv_news_title;
            public final TextView tv_news_desc;
            public final TextView tv_news_time;
            public final TextView tv_news_source;
            public final TextView tv_news_imageNum;
            //third image
            public final ImageView iv_news_oneimage;
            public final ImageView iv_news_twoimage;
            public final ImageView iv_news_thirdimage;
            public final ImageView iv_news_bigimage;
            public final ImageView iv_news_leftimage;

            public final LinearLayout ll_image_third;
            public final LinearLayout ll_image_one;


            public ViewHolder(View itemView) {
                super(itemView);
                mView=itemView;
                tv_news_title= (TextView) itemView.findViewById(R.id.tv_news_title);
                tv_news_desc= (TextView) itemView.findViewById(R.id.tv_news_desc);
                tv_news_time= (TextView) itemView.findViewById(R.id.tv_news_time);
                tv_news_source= (TextView) itemView.findViewById(R.id.tv_news_source);
                tv_news_imageNum= (TextView) itemView.findViewById(R.id.tv_news_imageNum);

                iv_news_bigimage= (ImageView) itemView.findViewById(R.id.iv_big_one);
                iv_news_leftimage= (ImageView) itemView.findViewById(R.id.iv_left_image);
                iv_news_oneimage= (ImageView) itemView.findViewById(R.id.iv_third_one);
                iv_news_twoimage= (ImageView) itemView.findViewById(R.id.iv_third_two);
                iv_news_thirdimage= (ImageView) itemView.findViewById(R.id.iv_third_third);

                ll_image_third= (LinearLayout) itemView.findViewById(R.id.ll_image_third);
                ll_image_one= (LinearLayout) itemView.findViewById(R.id.ll_image_one);
            }
        }
    }


    public class DividerLine extends RecyclerView.ItemDecoration{

        /**
         * 水平方向
         */
        public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;

        /**
         * 垂直方向
         */
        public static final int VERTICAL = LinearLayoutManager.VERTICAL;

        // 画笔
        private Paint paint;

        // 布局方向
        private int orientation;
        // 分割线颜色
        private int color;
        // 分割线尺寸
        private int size;

        public DividerLine() {
            this(VERTICAL);
        }
       /* public DividerLine(int space) {
            this.space = space;
        }*/
        public DividerLine(int orientation) {
            this.orientation = orientation;

            paint = new Paint();
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            if (orientation == VERTICAL) {
                drawHorizontal(c, parent);
            } else {
                drawVertical(c, parent);
            }
        }

        /**
         * 设置分割线颜色
         *
         * @param color 颜色
         */
        public void setColor(int color) {
            this.color = color;
            paint.setColor(color);
        }

        /**
         * 设置分割线尺寸
         *
         * @param size 尺寸
         */
        public void setSize(int size) {
            this.size = size;
        }

        /**
         * 设置分割线尺寸
         *
         * @param size 尺寸
         */
        public void setSpace(int height) {
            this.space = height;
        }

        // 绘制垂直分割线
        protected void drawVertical(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + size;

                c.drawRect(left, top, right, bottom, paint);
            }
        }

        // 绘制水平分割线
        protected void drawHorizontal(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + size;

                c.drawRect(left, top, right, bottom, paint);
            }
        }

        private int space;

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if(parent.getChildPosition(view) != 0)
                outRect.top = space;
        }
    }

}