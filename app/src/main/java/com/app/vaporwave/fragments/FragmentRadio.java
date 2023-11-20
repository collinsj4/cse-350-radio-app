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
import com.app.vaporwave.adapters.AdapterRadio;
import com.app.vaporwave.callbacks.CallbackRadio;
import com.app.vaporwave.database.dao.AppDatabase;
import com.app.vaporwave.database.dao.DAO;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Radio;
import com.app.vaporwave.rests.ApiInterface;
import com.app.vaporwave.rests.RestAdapter;
import com.app.vaporwave.utils.Constant;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentRadio extends Fragment {

    private static final String TAG = "FragmentRadio";
    private View rootView;
    private RecyclerView recyclerView;
    private AdapterRadio adapterRadio;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackRadio> callbackCall = null;
    private int postTotal = 0;
    private int failedPage = 0;
    ArrayList<Radio> items = new ArrayList<>();
    private ShimmerFrameLayout lytShimmer;
    private CharSequence charSequence = null;
    boolean flagReadLater;
    private DAO db;
    ThemePref themePref;
    SharedPref sharedPref;
    AdsPref adsPref;
    private MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_radio, container, false);
        //setHasOptionsMenu(true);
        db = AppDatabase.getDb(getContext()).get();

        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);
        adsPref = new AdsPref(activity);

        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        //set data and list adapter
        adapterRadio = new AdapterRadio(activity, recyclerView, items);
        recyclerView.setAdapter(adapterRadio);

        // on item list clicked
        adapterRadio.setOnItemClickListener((view, obj, position) -> ((MainActivity) activity).onItemRadioClick(items, position));

        adapterRadio.setOnItemOverflowClickListener((view, obj, position) -> ((MainActivity) activity).showBottomSheet(obj));

        // detect when scroll reach bottom
        adapterRadio.setOnLoadMoreListener(current_page -> {
            if (postTotal > adapterRadio.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                adapterRadio.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterRadio.resetListData();
            requestAction(1);
        });

        requestAction(1);

        return rootView;
    }

    private void displayApiResult(final ArrayList<Radio> posts) {
        adapterRadio.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }

        if (Constant.item_radio.size() == 0) {
            Constant.item_radio.addAll(posts);
            ((MainActivity) activity).changeText(Constant.item_radio.get(0));
        }
        
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getRadios(Config.PAGINATION, page_no, Config.REST_API_KEY);
        callbackCall.enqueue(new Callback<CallbackRadio>() {
            @Override
            public void onResponse(@NonNull Call<CallbackRadio> call, @NonNull Response<CallbackRadio> response) {
                CallbackRadio resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackRadio> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterRadio.setLoaded();
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterRadio.setLoading();
        }
        new Handler().postDelayed(() -> requestListPostApi(page_no), Constant.DELAY_PROGRESS);
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
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
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

