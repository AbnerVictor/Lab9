package com.example.abnervictor.lab9;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by abnervictor on 2017/12/19.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;//储存List_Item的子View
    private View mConvertView;//储存list_Item

    public ViewHolder(Context context, View itemView, ViewGroup parent){
        super(itemView);
        mConvertView = itemView;
        mViews = new SparseArray<View>();
    }
    public static ViewHolder get(Context context, ViewGroup parent, int layoutID){
        View itemView = LayoutInflater.from(context).inflate(layoutID,parent,false);
        ViewHolder holder = new ViewHolder(context, itemView, parent);
        return holder;
    }

    public <T extends View> T getView(int viewID){
        View view = mViews.get(viewID);
        if (view == null){
            //创建view
            view = mConvertView.findViewById(viewID);
            mViews.put(viewID, view);
        }
        return (T) view;
    }
}

