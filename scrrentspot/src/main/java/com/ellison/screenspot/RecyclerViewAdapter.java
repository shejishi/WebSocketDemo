package com.ellison.screenspot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author ellison
 * @date 2019年01月17日
 * @desc 用一句话描述这个类的作用
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VH> {
    private List<RecyclerBean.ResultBean.DataBean> listData;

    public RecyclerViewAdapter(List<RecyclerBean.ResultBean.DataBean> data) {
        this.listData = data;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new VH(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recycler_view, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.VH holder, int i) {
        RecyclerBean.ResultBean.DataBean dataBean = listData.get(i);
        holder.mTvTitle.setText(dataBean.getTitle());
        Glide.with(holder.mIvLeft.getContext())
                .load(dataBean.getThumbnail_pic_s())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.common_spot).placeholder(R.drawable.common_spot))
                .into(holder.mIvLeft);
        Glide.with(holder.mIvLeft.getContext())
                .load(dataBean.getThumbnail_pic_s02())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.common_spot).placeholder(R.drawable.common_spot))
                .into(holder.mIvCenter);
        Glide.with(holder.mIvLeft.getContext())
                .load(dataBean.getThumbnail_pic_s03())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.common_spot).placeholder(R.drawable.common_spot))
                .into(holder.mIvRight);
        holder.mTvBottom.setText(String.format("%s   %s   %s", dataBean.getCategory(), dataBean.getAuthor_name(), dataBean.getDate()));
    }

    public void onBindViewSync(@NonNull VH holder, int i) {
        RecyclerBean.ResultBean.DataBean dataBean = listData.get(i);
        holder.mTvTitle.setText(dataBean.getTitle());
        try {
            File file = Glide.with(holder.mIvLeft.getContext())
                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.common_spot).error(R.drawable.common_spot))
                    .load(dataBean.getThumbnail_pic_s())
                    .downloadOnly(0, 0)
                    .get();
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            holder.mIvLeft.setImageBitmap(bitmap);


            File fileCenter = Glide.with(holder.mIvCenter.getContext())
                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.common_spot).error(R.drawable.common_spot))
                    .load(dataBean.getThumbnail_pic_s02())
                    .downloadOnly(0, 0)
                    .get();
            Bitmap bitmapCenter = BitmapFactory.decodeFile(fileCenter.getAbsolutePath());
            holder.mIvCenter.setImageBitmap(bitmapCenter);

            File fileRight = Glide.with(holder.mIvRight.getContext())
                    .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.common_spot).error(R.drawable.common_spot))
                    .load(dataBean.getThumbnail_pic_s03())
                    .downloadOnly(0, 0)
                    .get();
            Bitmap bitmapRight = BitmapFactory.decodeFile(fileRight.getAbsolutePath());
            holder.mIvRight.setImageBitmap(bitmapRight);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    class VH extends RecyclerView.ViewHolder {

        private final TextView mTvTitle;
        private final ImageView mIvLeft;
        private final ImageView mIvCenter;
        private final ImageView mIvRight;
        private final TextView mTvBottom;
        private final View mViewLine;

        public VH(@NonNull View itemView) {
            super(itemView);

            mTvTitle = itemView.findViewById(R.id.recycler_view_tv_title);
            mIvLeft = itemView.findViewById(R.id.recycler_view_iv_left);
            mIvCenter = itemView.findViewById(R.id.recycler_view_iv_center);
            mIvRight = itemView.findViewById(R.id.recycler_view_iv_right);
            mTvBottom = itemView.findViewById(R.id.recycler_view_tv_bottom);
            mViewLine = itemView.findViewById(R.id.recycler_view_line);
        }
    }
}