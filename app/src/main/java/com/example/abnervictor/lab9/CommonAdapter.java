package com.example.abnervictor.lab9;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by abnervictor on 2017/12/19.
 */


public abstract class CommonAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    protected Context mContext;
    protected int mLayoutId;
    protected List<T> mData;
    private OnItemClickListener mOnItemClickListener = null;

    public CommonAdapter(Context context, int LayoutId, List<T> data){
        mContext = context;
        mLayoutId = LayoutId;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType){
        ViewHolder viewHolder = ViewHolder.get(mContext,parent,mLayoutId);
        return viewHolder;
    }

    public abstract void convert(ViewHolder holder, T t);

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){
        //设置holder内view的内容
        convert(holder, mData.get(position));
        if(mOnItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(holder.getAdapterPosition());

                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnItemClickListener.onLongClick(holder.getAdapterPosition());
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void removeItem(int position){
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

};