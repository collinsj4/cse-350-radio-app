package com.app.vaporwave.fragments;

import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterRadio;
import com.app.vaporwave.database.dao.AppDatabase;
import com.app.vaporwave.database.dao.DAO;
import com.app.vaporwave.database.dao.RadioEntity;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Radio;
import com.app.vaporwave.services.RadioPlayerService;
import com.app.vaporwave.utils.Constant;
import com.app.vaporwave.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    View rootView;
    AdapterRadio adapterRadio;
    RecyclerView recyclerView;
    private CharSequence charSequence = null;
    ThemePref themePref;
    boolean flagReadLater;
    private DAO db;
    ArrayList<Radio> items = new ArrayList<>();
    SharedPref sharedPref;
    AdsPref adsPref;
    private MainActivity activity;
    private BottomSheetDialog mBottomSheetDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        //setHasOptionsMenu(true);
        db = AppDatabase.getDb(getContext()).get();
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);
        adsPref = new AdsPref(activity);

        //set data and list adapter
        adapterRadio = new AdapterRadio(activity, recyclerView, items);
        recyclerView.setAdapter(adapterRadio);

        // on item list clicked
        adapterRadio.setOnItemClickListener((v, obj, position) -> {
            if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
                Constant.item_radio.clear();
                Constant.item_radio.addAll(items);
                Constant.position = position;
                sharedPref.setCurrentRadioPosition(position);
                ((MainActivity) activity).showAdMobInterstitialAd(position);
            } else {
                Constant.item_radio.clear();
                Constant.item_radio.addAll(items);
                Constant.position = position;
                Intent intent = new Intent(activity, RadioPlayerService.class);
                RadioPlayerService.createInstance().initializeRadio(activity, Constant.item_radio.get(position));
                intent.setAction(RadioPlayerService.ACTION_PLAY);
                activity.startService(intent);
                ((MainActivity) activity).showInterstitialAd();
            }
        });

        adapterRadio.setOnItemOverflowClickListener((view, obj, position) -> {
            showBottomSheetDialog(activity.findViewById(R.id.coordinator_layout), obj);
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData(db.getAllRadio());
    }

    private void displayData(final List<RadioEntity> radios) {
        ArrayList<Radio> items = new ArrayList<>();
        for (RadioEntity radio : radios) items.add(radio.original());
        showNoItemView(false);
        adapterRadio.resetListData();
        adapterRadio.insertData(items);
        if (radios.size() == 0) {
            showNoItemView(true);
        }
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_favorite);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_favorite_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    public void showBottomSheetDialog(View parentView, Radio radio) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.include_bottom_sheet, null);

        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        ImageView radioImage = view.findViewById(R.id.sheet_radio_image);
        TextView radioName = view.findViewById(R.id.sheet_radio_name);
        TextView radioCategory = view.findViewById(R.id.sheet_category_name);

        TextView txtFavorite = view.findViewById(R.id.txt_favorite);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgReport = view.findViewById(R.id.img_report);

        Glide.with(activity)
                .load(sharedPref.getBaseUrl() + "/upload/" + radio.radio_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(radioImage);

        radioName.setText(radio.radio_name);
        radioCategory.setText(radio.category_name);

        if (themePref.getCurrentTheme().equals(THEME_DARK)) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.white));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.white));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.white));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);

        btnFavorite.setOnClickListener(v -> {
            if (Tools.isNetworkAvailable(activity)) {
                flagReadLater = db.getRadio(radio.radio_id) != null;
                if (flagReadLater) {
                    db.deleteRadio(radio.radio_id);
                    imgFavorite.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_star_outline));
                    Snackbar.make(parentView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    db.insertRadio(RadioEntity.entity(radio));
                    imgFavorite.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_star_white));
                    Snackbar.make(parentView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            if (Constant.item_radio.size() > 0) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_radio_text) + " - " + Constant.item_radio.get(Constant.position).radio_name + "\n" + activity.getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                activity.startActivity(share);
            }
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            String str;
            try {
                str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{activity.getString(R.string.report_email)});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Report " + radio.radio_name + " channel issue in " + activity.getResources().getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
                        Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
                        "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Message : ");
                try {
                    activity.startActivity(Intent.createChooser(intent, activity.getResources().getString(R.string.menu_report)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            mBottomSheetDialog.dismiss();
        });

        if (themePref.getCurrentTheme().equals(THEME_DARK)) {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
        } else {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        flagReadLater = db.getRadio(radio.radio_id) != null;
        if (flagReadLater) {
            txtFavorite.setText(activity.getString(R.string.favorite_remove));
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
        } else {
            txtFavorite.setText(activity.getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        }

    }

//    @SuppressLint("NonConstantResourceId")
//    public void addFavorite(View view, Radio radio) {
//        PopupMenu popup = new PopupMenu(activity, view);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.menu_popup, popup.getMenu());
//        popup.setOnMenuItemClickListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.menu_context_favorite:
//                    if (charSequence.equals(getString(R.string.option_set_favorite))) {
//                        db.insertRadio(RadioEntity.entity(radio));
//                        Toast.makeText(activity, getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
//                        updateFav();
//                    } else if (charSequence.equals(getString(R.string.option_unset_favorite))) {
//                        db.deleteRadio(radio.radio_id);
//                        Toast.makeText(activity, getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
//                        updateFav();
//                        refreshFragment();
//                    }
//                    return true;
//
//                case R.id.menu_context_share:
//
//                    String share_title = android.text.Html.fromHtml(radio.radio_name).toString();
//                    String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
//                    Intent sendIntent = new Intent();
//                    sendIntent.setAction(Intent.ACTION_SEND);
//                    sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
//                    sendIntent.setType("text/plain");
//                    startActivity(sendIntent);
//                    return true;
//
//                default:
//            }
//            return false;
//        });
//        popup.show();
//
//        flagReadLater = db.getRadio(radio.radio_id) != null;
//        if (flagReadLater) {
//            popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_unset_favorite);
//            charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
//        } else {
//            popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_set_favorite);
//            charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
//        }
//
//    }

    public void refreshData() {
        displayData(db.getAllRadio());
    }

    private void updateFav() {
            ((MainActivity) activity).changeFav(Constant.item_radio.get(0));
    }

}
