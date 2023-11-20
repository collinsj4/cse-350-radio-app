package com.app.vaporwave.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.app.vaporwave.adapters.AdapterCategory;
import com.app.vaporwave.callbacks.CallbackCategory;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Category;
import com.app.vaporwave.rests.ApiInterface;
import com.app.vaporwave.rests.RestAdapter;
import com.app.vaporwave.utils.AdsManager;
import com.app.vaporwave.utils.Constant;
import com.app.vaporwave.utils.ItemOffsetDecoration;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategory extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private AdapterCategory adapterCategory;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackCategory> callbackCall = null;
    ArrayList<Category> items = new ArrayList<>();
    private ShimmerFrameLayout lytShimmer;
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
        rootView = inflater.inflate(R.layout.fragment_category, container, false);
        //setHasOptionsMenu(true);
        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);
        adsPref = new AdsPref(activity);
        adsManager = new AdsManager(activity);
        adsManager.loadInterstitialAd(1, adsPref.getInterstitialAdInterval());

        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(Config.CATEGORY_COLUMN_COUNT, StaggeredGridLayoutManager.VERTICAL));
        if (getActivity() != null) {
            ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset_medium);
            recyclerView.addItemDecoration(itemDecoration);
        }

        //set data and list adapter
        adapterCategory = new AdapterCategory(getActivity(), items);
        recyclerView.setAdapter(adapterCategory);

        // on item list clicked
        adapterCategory.setOnItemClickListener((view, obj, position) -> {
            ((MainActivity) getActivity()).openCategoryDetail(obj.cid, obj.category_name, obj.category_image, obj.radio_count);
            adsManager.showInterstitialAd();
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterCategory.resetListData();
            requestAction();
        });

        requestAction();

        return rootView;
    }

    private void displayApiResult(final ArrayList<Category> categories) {
        adapterCategory.setItems(categories);
        swipeProgress(false);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getCategories(Config.REST_API_KEY);
        callbackCall.enqueue(new Callback<CallbackCategory>() {
            @Override
            public void onResponse(@NonNull Call<CallbackCategory> call, @NonNull Response<CallbackCategory> response) {
                CallbackCategory resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.categories);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackCategory> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
        new Handler().postDelayed(this::requestListPostApi, Constant.DELAY_PROGRESS);
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
        View lyt_failed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_data_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

}

