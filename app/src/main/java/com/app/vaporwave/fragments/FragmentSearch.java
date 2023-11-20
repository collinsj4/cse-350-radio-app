package com.app.vaporwave.fragments;

import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.app.vaporwave.utils.Constant.THEME_LIGHT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterRadio;
import com.app.vaporwave.adapters.AdapterSearch;
import com.app.vaporwave.callbacks.CallbackRadio;
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

public class FragmentSearch extends DialogFragment {

    View rootView;
    private RelativeLayout parentView;
    private ImageButton btnBack;
    Toolbar toolbar;
    RecyclerView recyclerView;
    RecyclerView recyclerViewSuggestion;
    AdapterRadio adapterRadio;
    private AdapterSearch adapterSearch;
    ArrayList<Radio> items = new ArrayList<>();
    Call<CallbackRadio> callbackPostCall = null;
    private LinearLayout lytSuggestion;
    SharedPref sharedPref;
    private EditText edtSearch;
    private ImageButton btClear;
    ShimmerFrameLayout lytShimmer;
    ThemePref themePref;
    AdsPref adsPref;
    private int postTotal = 0;
    private int failedPage = 0;
    TextView txtClearHistory;
    private MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        initView();
        return rootView;
    }

    private void initView() {
        sharedPref = new SharedPref(activity);
        themePref = new ThemePref(activity);
        adsPref = new AdsPref(activity);

        parentView = rootView.findViewById(R.id.parent_view);
        toolbar = rootView.findViewById(R.id.toolbar);
        btnBack = rootView.findViewById(R.id.btn_back);

        initComponent();
        setupToolbar();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initComponent() {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerViewSuggestion = rootView.findViewById(R.id.recyclerSuggestion);
        lytSuggestion = rootView.findViewById(R.id.lyt_suggestion);
        edtSearch = rootView.findViewById(R.id.et_search);
        btClear = rootView.findViewById(R.id.bt_clear);
        btClear.setVisibility(View.GONE);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        txtClearHistory = rootView.findViewById(R.id.txt_clear_history);

        edtSearch.addTextChangedListener(textWatcher);
        edtSearch.requestFocus();

        new Handler().postDelayed(()-> {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        }, 100);

        swipeProgress(false);

        recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(activity));

        //set data and list adapter suggestion
        adapterSearch = new AdapterSearch(requireActivity());
        recyclerViewSuggestion.setAdapter(adapterSearch);
        showSuggestionSearch();
        adapterSearch.setOnItemClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
            lytSuggestion.setVisibility(View.GONE);
            adapterRadio.resetListData();
            hideKeyboard(activity);
            searchAction(1);
        });

        adapterSearch.setOnItemActionClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
        });

        if (adapterSearch.getItemCount() <= 0) {
            txtClearHistory.setVisibility(View.GONE);
        } else {
            txtClearHistory.setVisibility(View.GONE);
        }
        txtClearHistory.setOnClickListener(v -> new Handler().postDelayed(() -> {
            adapterSearch.clearSearchHistory();
            adapterSearch.refreshItems();
            txtClearHistory.setVisibility(View.GONE);
        }, 250));

        btClear.setOnClickListener(view -> new Handler().postDelayed(() -> edtSearch.setText(""), 200));

        edtSearch.setOnTouchListener((view, motionEvent) -> {
            showSuggestionSearch();
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        //set data and list adapter
        adapterRadio = new AdapterRadio(activity, recyclerView, items);
        recyclerView.setAdapter(adapterRadio);

        adapterRadio.setOnItemClickListener((view, obj, position) -> ((MainActivity) activity).onItemRadioClick(items, position));

        adapterRadio.setOnItemOverflowClickListener((view, obj, position) -> ((MainActivity) activity).showBottomSheet(obj));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        // detect when scroll reach bottom
        adapterRadio.setOnLoadMoreListener(this::setLoadMore);

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (edtSearch.getText().toString().equals("")) {
                    Toast.makeText(activity, getString(R.string.msg_search_input), Toast.LENGTH_SHORT).show();
                    hideKeyboard(activity);
                    swipeProgress(false);
                } else {
                    adapterRadio.resetListData();
                    hideKeyboard(activity);
                    searchAction(1);
                    txtClearHistory.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        });

    }

    public void setLoadMore(int current_page) {
        if (postTotal > adapterRadio.getItemCount() && current_page != 0) {
            int next_page = current_page + 1;
            searchAction(next_page);
        } else {
            adapterRadio.setLoaded();
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                btClear.setVisibility(View.GONE);
            } else {
                btClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final int page_no, final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());

        if (Config.ENABLE_RTL_MODE) {
            callbackPostCall = apiInterface.getSearchRtl(query, Config.PAGINATION, page_no, Config.REST_API_KEY);
        } else {
            callbackPostCall = apiInterface.getSearch(query, Config.PAGINATION, page_no, Config.REST_API_KEY);
        }

        callbackPostCall.enqueue(new Callback<CallbackRadio>() {
            @Override
            public void onResponse(@NonNull Call<CallbackRadio> call, @NonNull Response<CallbackRadio> response) {
                CallbackRadio resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    adapterRadio.insertData(resp.posts);
                    if (resp.posts.size() == 0) showNotFoundView(true);
                } else {
                    onFailRequest(page_no);
                }
                swipeProgress(false);
            }

            @Override
            public void onFailure(@NonNull Call<CallbackRadio> call, @NonNull Throwable t) {
                onFailRequest(page_no);
                swipeProgress(false);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterRadio.setLoaded();
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    private void searchAction(final int page_no) {
        lytSuggestion.setVisibility(View.GONE);
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = edtSearch.getText().toString().trim();
        if (!query.equals("")) {
            if (page_no == 1) {
                swipeProgress(true);
            } else {
                adapterRadio.setLoading();
            }
            adapterSearch.addSearchHistory(query);
            new Handler().postDelayed(() -> requestSearchApi(page_no, query), Constant.DELAY_PROGRESS);
        } else {
            Toast.makeText(activity, getString(R.string.msg_search_input), Toast.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void showSuggestionSearch() {
        adapterSearch.refreshItems();
        lytSuggestion.setVisibility(View.VISIBLE);
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
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction(failedPage));
    }

    private void showNotFoundView(boolean show) {
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
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
        } else {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        }
    }

    public static void hideKeyboard(Context context) {
        try {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if ((((Activity) context).getCurrentFocus() != null) && (((Activity) context).getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showKeyboard(Context context) {
        ((InputMethodManager) (context).getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FragmentManager fm = activity.getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }
            dismiss();
            hideKeyboard(activity);
        }, Constant.DELAY_CLICK));

        themeColor();
    }

    private void themeColor() {
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            btnBack.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);

            lytSuggestion.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            edtSearch.setTextColor(ContextCompat.getColor(activity, R.color.grey));
            edtSearch.setHintTextColor(ContextCompat.getColor(activity, R.color.grey));
            btClear.setColorFilter(ContextCompat.getColor(activity, R.color.grey));
        } else if (themePref.getCurrentTheme() == THEME_DARK) {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorToolbarDark));

            lytSuggestion.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
            edtSearch.setTextColor(ContextCompat.getColor(activity, R.color.white));
            edtSearch.setHintTextColor(ContextCompat.getColor(activity, R.color.white));
            btClear.setColorFilter(ContextCompat.getColor(activity, R.color.white));
        } else {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
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
