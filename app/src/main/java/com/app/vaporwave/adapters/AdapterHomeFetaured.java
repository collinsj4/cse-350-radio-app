package com.app.vaporwave.adapters;

import static com.app.vaporwave.utils.Constant.WAITING_TIME_NEXT_ITEM_CLICK;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.vaporwave.R;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.models.Radio;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AdapterHomeFetaured extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Radio> items;
    public Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    public CharSequence charSequence = null;
    SharedPref sharedPref;
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

    public AdapterHomeFetaured(Context context, ArrayList<Radio> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgRadio;
        public ImageButton imgOverflow;
        public TextView txtRadio;
        public TextView txtCategory;
        public LinearLayout lytParent;

        public OriginalViewHolder(View view) {
            super(view);
            txtRadio = view.findViewById(R.id.txt_radio);
            txtCategory = view.findViewById(R.id.txt_category);
            imgRadio = view.findViewById(R.id.img_radio);
            imgOverflow = view.findViewById(R.id.img_overflow);
            lytParent = view.findViewById(R.id.lyt_parent);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_featured, parent, false);
        vh = new OriginalViewHolder(v);
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

        }
    }

    public void setItems(ArrayList<Radio> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}