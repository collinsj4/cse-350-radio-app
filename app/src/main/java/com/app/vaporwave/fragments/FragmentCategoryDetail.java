package com.app.vaporwave.fragments;

import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.app.vaporwave.utils.Constant.THEME_LIGHT;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterRadio;
import com.app.vaporwave.callbacks.CallbackCategoryDetail;
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

public class FragmentCategoryDetail extends DialogFragment {

    View rootView;
    private Toolbar toolbar;
    private RelativeLayout parentView;
    private ImageButton btnBack;
    private ImageButton btnSearch;
    private TextView toolbarTitle;
    private AdapterRadio adapterRadio;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackCategoryDetail> callbackCall = null;
    private int postTotal = 0;
    private int failedPage = 0;
    ArrayList<Radio> items = new ArrayList<>();
    private RecyclerView recyclerView;
    String cid, categoryName, categoryImage, radioCount;
    private ShimmerFrameLayout lytShimmer;
    ThemePref themePref;
    SharedPref sharedPref;
    AdsPref adsPref;
    private MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_category_detail, container, false);
        if (getArguments() != null) {
            cid = getArguments().getString("category_id");
            categoryName = getArguments().getString("category_name");
            categoryImage = getArguments().getString("category_image");
            radioCount = getArguments().getString("radio_count");
        }
        initView();
        return rootView;
    }

    private void initView() {
        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);
        adsPref = new AdsPref(activity);

        parentView = rootView.findViewById(R.id.parent_view);
        toolbar = rootView.findViewById(R.id.toolbar);
        toolbarTitle = rootView.findViewById(R.id.toolbar_title);
        btnBack = rootView.findViewById(R.id.btn_back);
        btnSearch = rootView.findViewById(R.id.btn_search);

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

        setupToolbar();

    }

    private void displayApiResult(final ArrayList<Radio> posts) {
        adapterRadio.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }

        if (Constant.item_radio.size() == 0) {
            Constant.item_radio.addAll(posts);
            ((MainActivity) requireActivity()).changeText(Constant.item_radio.get(0));
        }

    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getCategoryDetail(cid, Config.PAGINATION, page_no);
        callbackCall.enqueue(new Callback<CallbackCategoryDetail>() {
            @Override
            public void onResponse(@NonNull Call<CallbackCategoryDetail> call, @NonNull Response<CallbackCategoryDetail> response) {
                CallbackCategoryDetail resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackCategoryDetail> call, @NonNull Throwable t) {
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

    private void setupToolbar() {
        toolbarTitle.setText(categoryName);
        btnBack.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FragmentManager fm = activity.getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }
            dismiss();
        }, Constant.DELAY_CLICK));

        btnSearch.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> ((MainActivity) activity).openFragmentSearch(), Constant.DELAY_CLICK));

        themeColor();
    }

    private void themeColor() {
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.grey));
            btnBack.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
            btnSearch.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        } else if (themePref.getCurrentTheme() == THEME_DARK) {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorToolbarDark));
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.white));
        } else {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.white));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

}
