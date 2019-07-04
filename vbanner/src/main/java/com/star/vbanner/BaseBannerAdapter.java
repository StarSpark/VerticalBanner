package com.star.vbanner;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @作者 StarSpark
 * @描述
 * @创建日期 2019/6/10 19:09
 */
@SuppressWarnings("unused")
public abstract class BaseBannerAdapter<T> {
    private List<T> mDatas;
    private OnDataChangedListener mOnDataChangedListener;

    public BaseBannerAdapter(@NonNull List<T> datas) {
        if (datas == null) {
            throw new RuntimeException("nothing to show");
        }
        mDatas = datas;
    }

    public BaseBannerAdapter(T[] datas) {
        mDatas = new ArrayList<>(Arrays.asList(datas));
    }

    /**
     * 设置banner填充的数据,并更新界面
     */
    public void setData(List<T> datas) {
        this.mDatas = datas;
        notifyDataChanged();
    }

    void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    void notifyDataChanged() {
        mOnDataChangedListener.onChanged();
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    /**
     * 设置banner的样式
     */
    public abstract View getView(VerticalBannerView parent);

    /**
     * 设置banner的数据
     */
    public abstract void setItem(View view, T data);


    interface OnDataChangedListener {
        void onChanged();
    }
}
