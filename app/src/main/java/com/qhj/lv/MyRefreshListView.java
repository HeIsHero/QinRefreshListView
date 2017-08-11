package com.qhj.lv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

/**
 * When I wrote this, only God and I understood what I was doing
 * Now, God only knows
 * 写这段代码的时候，只有上帝和我知道它是干嘛的
 * 现在，只有上帝知道
 * Created by Coder·Qin on 2017/7/5.
 */
public class MyRefreshListView extends ListView implements AbsListView.OnScrollListener {

    private View headerView;
    private View footerView;
    private ImageView ivHeaderArrow;
    private ImageView ivHeaderRotate;
    private ImageView ivFooter;
    private TextView tvHeaderState;
    private TextView tvHeaderDate;
    private TextView tvFooterDate;
    private int headerViewHeight;
    private int footerViewHeight;
    private RotateAnimation upAnimation;
    private RotateAnimation downAnimation;
    private RotateAnimation rotateAnimation;
    private boolean isFirstChild;//当前显示的第一个item是否是ListView的第一个子View
    private boolean isScrollToTop;//是否滑动到了顶部
    private boolean isScrollToBottom;//是否滑动到了底部
    private boolean isRefresh;//是否正在刷新
    private boolean isLoadMore;//是否正在加载更多
    private boolean isScroll;//是否正在滚动,此标识符是为了解决上拉加载一条数据后，点击最后一个item又会加载一次
    private boolean isStartUpAnimation=true;//是否开启箭头转向上动画
    private boolean isStartDownAnimation;//是否开启箭头转向下动画
    private int startY;
    private int moveY;
    private int offset;
    private int paddingTop;
    private OnRefreshListener refreshListener;

    public MyRefreshListView(Context context) {
        super(context, null);
        init();
    }

    public MyRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public MyRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
        this.setOnScrollListener(this);
    }

    private void initView() {
        headerView = View.inflate(getContext(), R.layout.header_view_my_refresh_lv, null);
        footerView = View.inflate(getContext(), R.layout.footer_view_my_refresh_lv, null);
        ivHeaderArrow = (ImageView) headerView.findViewById(R.id.iv_qin_lv_header_view_arrow);
        ivHeaderRotate = (ImageView) headerView.findViewById(R.id.iv_qin_lv_header_view_rotate);
        tvHeaderState = (TextView) headerView.findViewById(R.id.tv_qin_lv_header_view_state);
        tvHeaderDate = (TextView) headerView.findViewById(R.id.tv_qin_lv_header_view_date);
        ivFooter = (ImageView) footerView.findViewById(R.id.iv_qin_lv_footer_view);
        tvFooterDate = (TextView) footerView.findViewById(R.id.tv_qin_lv_footer_view_date);

        tvHeaderDate.setText("最后更新时间：" + getLastUpdateTime());
        headerView.measure(0, 0);
        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        this.addHeaderView(headerView);

        tvFooterDate.setText("最后更新时间：" + getLastUpdateTime());
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        this.addFooterView(footerView);

        upAnimation = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);//动画结束后停留在结束的位置
        downAnimation = new RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
        rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setRepeatCount(-1);//循环
        rotateAnimation.setInterpolator(new LinearInterpolator());//匀速
    }

    private String getLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(System.currentTimeMillis());
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case SCROLL_STATE_IDLE:
                isScroll=false;
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
            case SCROLL_STATE_FLING:
                isScroll=true;
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            View topItem = view.getChildAt(0);
            if (topItem != null && topItem.getTop() == 0) {
                isScrollToTop = true;
            }
        } else {
            isScrollToTop = false;
        }

        if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
            View bottomItem = view.getChildAt(view.getChildCount() - 1);
            if (bottomItem != null && bottomItem.getBottom() == view.getHeight()) {
                isScrollToBottom=true;
            }
        }else {
            isScrollToBottom=false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) ev.getY();
                if (isScrollToTop) {
                    isFirstChild = true;
                } else {
                    isFirstChild = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = (int) ev.getY();
                offset = moveY - startY;
                paddingTop = -headerViewHeight + offset/2;
                if (isFirstChild && isScrollToTop && !isRefresh&&-headerViewHeight < paddingTop) {
                    headerView.setPadding(0, paddingTop, 0, 0);
                    if (paddingTop >= 0) {
                        tvHeaderState.setText("松开刷新");
                        if (isStartUpAnimation){
                            isStartUpAnimation=false;
                            isStartDownAnimation=true;
                            ivHeaderArrow.startAnimation(upAnimation);
                        }
                    } else {
                        tvHeaderState.setText("下拉刷新");
                        isStartUpAnimation=true;
                        if (isStartDownAnimation){
                            isStartDownAnimation=false;
                            ivHeaderArrow.startAnimation(downAnimation);
                        }
                    }
                    clearContentViewEvents();
                    return true;
                }
                if (isRefresh||isLoadMore){
                    clearContentViewEvents();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isRefresh){
                    if (isStartDownAnimation){
                        isRefresh=true;
                        tvHeaderState.setText("正在刷新");
                        headerView.setPadding(0, 0, 0, 0);
                        ivHeaderArrow.setVisibility(GONE);
                        ivHeaderRotate.setVisibility(VISIBLE);
                        ivHeaderArrow.clearAnimation();
                        ivHeaderRotate.startAnimation(rotateAnimation);
                        refresh();
                    }else {
                        tvHeaderState.setText("下拉刷新");
                        headerView.setPadding(0, -headerViewHeight, 0, 0);
                        ivHeaderArrow.setVisibility(VISIBLE);
                        ivHeaderRotate.setVisibility(GONE);
                        ivHeaderArrow.clearAnimation();
                        ivHeaderRotate.clearAnimation();
                        isStartUpAnimation=true;
                        isStartDownAnimation=false;
                        isRefresh=false;
                    }
                }

                if (isScrollToBottom&&isScroll&&!isLoadMore){
                    isLoadMore=true;
                    footerView.setPadding(0,0,0,0);
                    ivFooter.startAnimation(rotateAnimation);
                    this.setSelection(this.getCount());
                    loadMore();
                }
                break;

        }
        return super.onTouchEvent(ev);
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener){
        this.refreshListener=refreshListener;
    }

    public interface OnRefreshListener{
        void onRefresh();
        void onLoadMore();
    }

    private void refresh(){
        if (refreshListener!=null){
            refreshListener.onRefresh();
        }
    }

    private void loadMore(){
        if (refreshListener!=null){
            refreshListener.onLoadMore();
        }
    }

    public void refreshComplete(){
        tvHeaderState.setText("下拉刷新");
        tvHeaderDate.setText("最后更新时间：" + getLastUpdateTime());
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        ivHeaderArrow.setVisibility(VISIBLE);
        ivHeaderRotate.setVisibility(GONE);
        ivHeaderArrow.clearAnimation();
        ivHeaderRotate.clearAnimation();
        isStartUpAnimation=true;
        isStartDownAnimation=false;
        isRefresh=false;
    }

    public void loadMoreComplete(){
        tvFooterDate.setText("最后更新时间：" + getLastUpdateTime());
        footerView.setPadding(0,-footerViewHeight,0,0);
        ivFooter.clearAnimation();
        isLoadMore=false;
    }

    private void clearContentViewEvents() {
        try {
            Field[] fields = AbsListView.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++)
                if (fields[i].getName().equals("mPendingCheckForLongPress")) {
                    // mPendingCheckForLongPress是AbsListView中的字段，通过反射获取并从消息列表删除，去掉长按事件
                    fields[i].setAccessible(true);
                    this.getHandler().removeCallbacks((Runnable) fields[i].get(this));
                } else if (fields[i].getName().equals("mTouchMode")) {
                    // TOUCH_MODE_REST = -1， 这个可以去除点击事件
                    fields[i].setAccessible(true);
                    fields[i].set(this, -1);
                }
            // 去掉焦点
            this.getSelector().setState(new int[]{0});
        } catch (Exception e) {
        }
    }
}
