package net.neevek.android.lib.lightimagepicker.page;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.neevek.android.lib.lightimagepicker.R;
import net.neevek.android.lib.lightimagepicker.pojo.Bucket;

import java.util.List;

/**
 * Lilith Games
 * Created by JiaminXie on 15/01/2017.
 */

public class ResourceBucketManager {
    private Context mContext;

    private RecyclerView mRbBucketList;
    private TextView mBtnSelectBucket;
    private List<Bucket> mBucketList;
    private int mCurrentCheckedBucketIndex = 0;

    private BucketItemListAdapter mAdapter;

    private OnBucketSelectedListener mOnBucketSelectedListener;

    private int mVisibleItemCountForPortrait = 4;
    private int mVisibleItemCountForLandscape = 2;

    private int mItemHeight;

    public ResourceBucketManager(Context context,
                                 RecyclerView rbBucketList,
                                 TextView tvSelectBucket,
                                 List<Bucket> bucketList) {
        mContext = context;
        mRbBucketList = rbBucketList;
        mBtnSelectBucket = tvSelectBucket;
        mBucketList = bucketList;
        mItemHeight = mContext.getResources ().getDimensionPixelSize(R.dimen.light_image_picker_bucket_item_height);

        mRbBucketList.setLayoutManager(new LinearLayoutManager(context));
        mRbBucketList.addItemDecoration(new DividerItemDecoration(context, RecyclerView.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = 1;
                outRect.left = 0;
                outRect.bottom = 0;
                outRect.right = 0;
            }
        });

        if (bucketList.size() > 0) {
            tvSelectBucket.setText(bucketList.get(0).bucketName);
        }

        mAdapter = new BucketItemListAdapter();
        mRbBucketList.setAdapter(mAdapter);

        resetBucketRecyclerViewHeight();
    }

    private void resetBucketRecyclerViewHeight() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mRbBucketList.getLayoutParams();
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mBucketList.size() <= mVisibleItemCountForPortrait) {
                return;
            }
            lp.height = mItemHeight * mVisibleItemCountForPortrait;
            mRbBucketList.requestLayout();
        } else {
            if (mBucketList.size() <= mVisibleItemCountForLandscape) {
                return;
            }
            lp.height = mItemHeight * mVisibleItemCountForLandscape;
            mRbBucketList.requestLayout();
        }
    }

    public void setVisibleItemCountForPortrait(int visibleItemCountForPortrait) {
        mVisibleItemCountForPortrait = visibleItemCountForPortrait;
    }

    public void setVisibleItemCountForLandscape(int visibleItemCountForLandscape) {
        mVisibleItemCountForLandscape = visibleItemCountForLandscape;
    }

    public void setOnBucketSelectedListener(OnBucketSelectedListener onBucketSelectedListener) {
        mOnBucketSelectedListener = onBucketSelectedListener;
    }

    private class BucketItemListAdapter
            extends RecyclerView.Adapter<BucketItemListAdapter.ViewHolder>
            implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.light_image_picker_bucket_list_item, parent, false));
            holder.viewItem.setOnClickListener(this);
            holder.rbBucketChecked.setOnCheckedChangeListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Bucket bucket = mBucketList.get(position);
            Glide.with(mContext)
                    .load(bucket.lastImagePath)
                    .centerCrop()
//                    .crossFade()
                    .into(holder.ivBucketIcon);
            holder.tvBucketName.setText(bucket.bucketName);
            holder.tvBucketDesc.setText(mContext.getString(R.string.light_image_picker_item_count, bucket.fileCount));
            holder.rbBucketChecked.setChecked(position == mCurrentCheckedBucketIndex);

            holder.viewItem.setTag(position);
            holder.rbBucketChecked.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mBucketList != null ? mBucketList.size() : 0;
        }

        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            if (position == null) {
                return;
            }
            handleSelectBucket(position);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!buttonView.isPressed()) {
                return;
            }

            Integer position = (Integer) buttonView.getTag();
            if (position == null) {
                return;
            }
            handleSelectBucket(position);
        }


        private void handleSelectBucket(Integer position) {
            if (position == mCurrentCheckedBucketIndex) {
                return;
            }
            mCurrentCheckedBucketIndex = position;
            mAdapter.notifyDataSetChanged();

            Bucket bucket = mBucketList.get(mCurrentCheckedBucketIndex);
            mBtnSelectBucket.setText(bucket.bucketName);

            if (mOnBucketSelectedListener != null) {
                mOnBucketSelectedListener.onBucketSelected(bucket);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private View viewItem;
            private ImageView ivBucketIcon;
            private TextView tvBucketName;
            private TextView tvBucketDesc;
            private RadioButton rbBucketChecked;

            public ViewHolder(View itemView) {
                super(itemView);
                viewItem = itemView;
                ivBucketIcon = (ImageView) itemView.findViewById(R.id.light_image_picker_iv_bucket_icon);
                tvBucketName = (TextView) itemView.findViewById(R.id.light_image_picker_tv_bucket_name);
                tvBucketDesc = (TextView) itemView.findViewById(R.id.light_image_picker_tv_bucket_desc);
                rbBucketChecked = (RadioButton) itemView.findViewById(R.id.light_image_picker_rb_bucket_checked);
            }
        }
    }

    interface OnBucketSelectedListener {
        void onBucketSelected(Bucket selectedBucket);
    }
}
