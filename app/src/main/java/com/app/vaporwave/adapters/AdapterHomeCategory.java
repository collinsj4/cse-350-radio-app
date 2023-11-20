package com.app.vaporwave.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.models.Category;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AdapterHomeCategory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Category> items;
    public Context context;
    private OnItemClickListener mOnItemClickListener;
    private boolean clicked;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterHomeCategory(Context context, ArrayList<Category> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView txtCategoryName;
        public TextView txtRadioCount;
        public ImageView imgCategory;
        public LinearLayout lytParent;

        public OriginalViewHolder(View view) {
            super(view);
            txtCategoryName = view.findViewById(R.id.txt_category_name);
            txtRadioCount = view.findViewById(R.id.txt_radio_count);
            imgCategory = view.findViewById(R.id.img_category);
            lytParent = view.findViewById(R.id.lyt_parent);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_category, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Category c = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.txtCategoryName.setText(c.category_name);
            vItem.txtRadioCount.setText(c.radio_count);

            vItem.txtRadioCount.setText(c.radio_count + " " + context.getResources().getString(R.string.station));
            if (Config.DISPLAY_RADIO_COUNT_ON_CATEGORY_LIST) {
                vItem.txtRadioCount.setVisibility(View.VISIBLE);
            } else {
                vItem.txtRadioCount.setVisibility(View.GONE);
            }

            Glide.with(context)
                    .load(sharedPref.getBaseUrl() + "/upload/category/" + c.category_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(vItem.imgCategory);

            vItem.lytParent.setOnClickListener(view -> {
                if (clicked) {
                    return;
                }
                clicked = true;
                new Handler().postDelayed(() -> clicked = false, 2000);

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, c, position);
                }
            });

        }
    }

    public void setItems(ArrayList<Category> items) {
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