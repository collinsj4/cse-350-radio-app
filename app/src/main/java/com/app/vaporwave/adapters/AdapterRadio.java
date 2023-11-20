package com.app.vaporwave.adapters;

import static com.app.vaporwave.utils.Constant.WAITING_TIME_NEXT_ITEM_CLICK;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.models.Radio;
import com.app.vaporwave.utils.AdsManager;
import com.app.vaporwave.utils.Constant;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AdapterRadio extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private ArrayList<Radio> items;
    public Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    public CharSequence charSequence = null;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    public boolean scrolling = false;
    SharedPref sharedPref;
    AdsManager adsManager;
    private boolean clicked;

    public interface OnItemClickListener {
        void onItemClick(View view, Radio obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Radio obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterRadio(Context context, RecyclerView view, ArrayList<Radio> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsManager = new AdsManager((Activity) context);
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgRadio;
        public ImageButton imgOverflow;
        public TextView txtRadio;
        public TextView txtCategory;
        public LinearLayout lytParent;

        public OriginalViewHolder(View view) {
            super(view);
            lytParent = view.findViewById(R.id.lyt_parent);
            txtRadio = view.findViewById(R.id.txt_radio);
            txtCategory = view.findViewById(R.id.txt_category);
            imgRadio = view.findViewById(R.id.img_radio);
            imgOverflow = view.findViewById(R.id.img_overflow);
        }

    }

    public class AdViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgRadio;
        public ImageButton imgOverflow;
        public TextView txtRadio;
        public TextView txtCategory;
        public LinearLayout lytParent;

        public AdViewHolder(View view) {
            super(view);
            lytParent = view.findViewById(R.id.lyt_parent);
            txtRadio = view.findViewById(R.id.txt_radio);
            txtCategory = view.findViewById(R.id.txt_category);
            imgRadio = view.findViewById(R.id.img_radio);
            imgOverflow = view.findViewById(R.id.img_overflow);
            bindNativeAd(view);
        }

        public void bindNativeAd(View view) {
            adsManager.loadNativeAdViewRadio(view, Config.NATIVE_AD_RADIO_LIST);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.load_more);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_radio, parent, false);
            vh = new OriginalViewHolder(v);
        } else if (viewType == VIEW_AD) {
            if (Config.NATIVE_AD_STYLE_ON_RADIO_LIST.equals("radio")) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_radio, parent, false);
                vh = new AdViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad_default, parent, false);
                vh = new AdViewHolder(v);
            }
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Radio c = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.txtRadio.setText(c.radio_name);
            vItem.txtCategory.setText(c.category_name);

            Glide.with(context)
                    .load(sharedPref.getBaseUrl() + "/upload/" + c.radio_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(vItem.imgRadio);

            vItem.imgOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, c, position);
                }
            });

            vItem.lytParent.setOnClickListener(view -> {
                if (clicked) {
                    return;
                }
                clicked = true;
                new Handler().postDelayed(() -> clicked = false, WAITING_TIME_NEXT_ITEM_CLICK);

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, c, position);
                }
            });

        } else if (holder instanceof AdViewHolder) {
            final Radio c = items.get(position);
            final AdViewHolder vItem = (AdViewHolder) holder;

            vItem.txtRadio.setText(c.radio_name);
            vItem.txtCategory.setText(c.category_name);

            Glide.with(context)
                    .load(sharedPref.getBaseUrl() + "/upload/" + c.radio_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(vItem.imgRadio);

            vItem.imgOverflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, c, position);
                }
            });

            vItem.lytParent.setOnClickListener(view -> {
                if (clicked) {
                    return;
                }
                clicked = true;
                new Handler().postDelayed(() -> clicked = false, WAITING_TIME_NEXT_ITEM_CLICK);

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, c, position);
                }
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setFullSpan(getItemViewType(position) == VIEW_PROG);
    }

    public void insertData(ArrayList<Radio> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setItems(ArrayList<Radio> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Radio radio = items.get(position);
        if (radio != null) {
            final AdsPref adsPref = new AdsPref(context);
            int LIMIT_NATIVE_AD = (Constant.MAX_NUMBER_OF_NATIVE_AD_DISPLAYED * adsPref.getNativeAdInterval()) + adsPref.getNativeAdIndex();
            for (int i = adsPref.getNativeAdIndex(); i < LIMIT_NATIVE_AD; i += adsPref.getNativeAdInterval()) {
                if (position == i) {
                    return VIEW_AD;
                }
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / Config.PAGINATION;
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }

}