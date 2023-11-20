package com.app.vaporwave.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterHomeCategory;
import com.app.vaporwave.adapters.AdapterHomeFetaured;
import com.app.vaporwave.adapters.AdapterHomeRadio;
import com.app.vaporwave.callbacks.CallbackHome;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Category;
import com.app.vaporwave.models.Radio;
import com.app.vaporwave.rests.ApiInterface;
import com.app.vaporwave.rests.RestAdapter;
import com.app.vaporwave.utils.AdsManager;
import com.app.vaporwave.utils.Constant;
import com.app.vaporwave.utils.ItemOffsetDecoration;
import com.app.vaporwave.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHome extends Fragment {

    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackHome> callbackCall = null;
    private ShimmerFrameLayout lytShimmer;
    RelativeLayout lytContent;
    LinearLayout lytFeatured, lytCategory, lytRecent, lytRandom;
    ThemePref themePref;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    private MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);
        adsPref = new AdsPref(activity);
        adsManager = new AdsManager(activity);

        lytShimmer = view.findViewById(R.id.shimmer_view_container);
        lytContent = view.findViewById(R.id.lyt_content);
        lytFeatured = view.findViewById(R.id.lyt_featured);
        lytCategory = view.findViewById(R.id.lyt_category);
        lytRecent = view.findViewById(R.id.lyt_recent);
        lytRandom = view.findViewById(R.id.lyt_random);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            requestAction();
        });

        requestAction();

        adsManager.loadNativeAdView(view, Config.NATIVE_AD_HOME);

        return view;
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
        new Handler().postDelayed(this::requestListPostApi, Constant.DELAY_PROGRESS);
    }

    private void requestListPostApi() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getHome(Config.REST_API_KEY);
        callbackCall.enqueue(new Callback<CallbackHome>() {
            @Override
            public void onResponse(@NonNull Call<CallbackHome> call, @NonNull Response<CallbackHome> response) {
                CallbackHome resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayFeatured(resp.featured);
                    displayCategories(resp.categories);
                    displayRecent(resp.recent);
                    displayRandom(resp.random);
                    viewAll();
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackHome> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void viewAll() {
        view.findViewById(R.id.view_all_categories).setOnClickListener(v -> new Handler().postDelayed(() -> ((MainActivity) activity).selectCategory(), 250));
        view.findViewById(R.id.view_all_radios).setOnClickListener(v -> new Handler().postDelayed(() -> ((MainActivity) activity).selectRadio(), 250));
    }

    private void displayFeatured(final ArrayList<Radio> radios) {
        AdapterHomeFetaured mAdapter = new AdapterHomeFetaured(getActivity(), radios);
        RecyclerView recyclerViewFeatured = view.findViewById(R.id.recyclerViewFeatured);
        recyclerViewFeatured.setNestedScrollingEnabled(false);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(activity, R.dimen.item_no_space);
        if (0 == recyclerViewFeatured.getItemDecorationCount()) {
            recyclerViewFeatured.addItemDecoration(itemDecoration);
        }
        recyclerViewFeatured.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerViewFeatured.setAdapter(mAdapter);
        mAdapter.setItems(radios);

        mAdapter.setOnItemClickListener((view, obj, position) -> ((MainActivity) activity).onItemRadioClick(radios, position));

        mAdapter.setOnItemOverflowClickListener((view, obj, position) -> ((MainActivity) activity).showBottomSheet(obj));

        if (radios.size() == 0) {
            lytFeatured.setVisibility(View.GONE);
        }
    }

    private void displayCategories(final ArrayList<Category> categories) {
        AdapterHomeCategory mAdapter = new AdapterHomeCategory(getActivity(), categories);
        RecyclerView recyclerViewCategory = view.findViewById(R.id.recyclerViewCategory);
        recyclerViewCategory.setNestedScrollingEnabled(false);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(activity, R.dimen.item_no_space);
        if (0 == recyclerViewCategory.getItemDecorationCount()) {
            recyclerViewCategory.addItemDecoration(itemDecoration);
        }
        recyclerViewCategory.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerViewCategory.setAdapter(mAdapter);
        mAdapter.setItems(categories);

        mAdapter.setOnItemClickListener((view, obj, position) ->
                new Handler().postDelayed(() -> ((MainActivity) activity)
                        .openCategoryDetail(obj.cid, obj.category_name, obj.category_image, obj.radio_count), 0));

        if (categories.size() == 0) {
            lytCategory.setVisibility(View.GONE);
        }

    }

    private void displayRecent(final ArrayList<Radio> radios) {
        AdapterHomeRadio mAdapter = new AdapterHomeRadio(activity, radios);
        RecyclerView recyclerViewRecent = view.findViewById(R.id.recyclerViewRecent);
        recyclerViewRecent.setNestedScrollingEnabled(false);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(activity, R.dimen.item_no_space);
        if (0 == recyclerViewRecent.getItemDecorationCount()) {
            recyclerViewRecent.addItemDecoration(itemDecoration);
        }
        recyclerViewRecent.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerViewRecent.setAdapter(mAdapter);
        mAdapter.setItems(radios);

        if (Constant.item_radio.size() == 0) {
            Constant.item_radio.addAll(radios);
            ((MainActivity) activity).changeText(Constant.item_radio.get(0));
        }
        swipeProgress(false);
        if (radios.size() == 0) {
            showNoItemView(true);
        }

        mAdapter.setOnItemClickListener((view, obj, position) -> ((MainActivity) activity).onItemRadioClick(radios, position));

        mAdapter.setOnItemOverflowClickListener((view, obj, position) -> ((MainActivity) activity).showBottomSheet(obj));
    }

    private void displayRandom(final ArrayList<Radio> radios) {
        AdapterHomeRadio mAdapter = new AdapterHomeRadio(getActivity(), radios);
        RecyclerView recyclerViewRecent = view.findViewById(R.id.recyclerViewRandom);
        recyclerViewRecent.setNestedScrollingEnabled(false);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(activity, R.dimen.item_no_space);
        if (0 == recyclerViewRecent.getItemDecorationCount()) {
            recyclerViewRecent.addItemDecoration(itemDecoration);
        }
        recyclerViewRecent.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerViewRecent.setAdapter(mAdapter);
        mAdapter.setItems(radios);

        if (Constant.item_radio.size() == 0) {
            Constant.item_radio.addAll(radios);
            ((MainActivity) activity).changeText(Constant.item_radio.get(0));
        }
        swipeProgress(false);
        if (radios.size() == 0) {
            showNoItemView(true);
        }

        mAdapter.setOnItemClickListener((view, obj, position) -> ((MainActivity) activity).onItemRadioClick(radios, position));

        mAdapter.setOnItemOverflowClickListener((view, obj, position) -> ((MainActivity) activity).showBottomSheet(obj));
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (Tools.isConnect(activity)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = view.findViewById(R.id.lyt_failed);
        ((TextView) view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lytContent.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lytContent.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        view.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = view.findViewById(R.id.lyt_no_item);
        ((TextView) view.findViewById(R.id.no_item_message)).setText(R.string.no_data_found);
        if (show) {
            lytContent.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lytContent.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytContent.setVisibility(View.VISIBLE);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        } else {
            swipeRefreshLayout.setRefreshing(show);
            lytContent.setVisibility(View.GONE);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytContent.setVisibility(View.GONE);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

}