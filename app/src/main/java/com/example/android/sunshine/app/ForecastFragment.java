package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.android.sunshine.app.data.SunshinePreferences;
import com.example.android.sunshine.app.utilities.NetworkUtils;
import com.example.android.sunshine.app.utilities.OpenWeatherJsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;


public class ForecastFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<String[]>,
        ArrayAdapter.ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int FETCH_WEATHER_LOADER = 22;
    private final String LOG_TAG = this.getClass().getSimpleName();

    private RecyclerView recyclerView;
    private ArrayAdapter adapter = new ArrayAdapter();

    private TextView errorMessageTextView;

    private ProgressBar progressBar;

    private RecyclerView.LayoutManager layoutManager;

    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    public ForecastFragment() {
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Object item = adapter.getItem(clickedItemIndex);
        String forecast = item.toString();
        Intent detailActivityIntent = new Intent(getActivity(),DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT,forecast);
        startActivity(detailActivityIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.listview_forecast);

        errorMessageTextView = (TextView) rootView.findViewById(R.id.error_message);

        progressBar = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        Bundle bundle = null;

        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<String[]> updateWeatherLoader = loaderManager.getLoader(FETCH_WEATHER_LOADER);

        if (updateWeatherLoader != null && updateWeatherLoader.isReset()) {
            loaderManager.restartLoader(FETCH_WEATHER_LOADER, bundle, this);
        } else {
            loaderManager.initLoader(FETCH_WEATHER_LOADER, bundle, this);
        }

        return rootView;
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String[]>(getContext()) {
            String[] queryData = null;

            @Override
            protected void onStartLoading() {

                if (queryData != null) {
                    deliverResult(queryData);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {

                String locationQuery = SunshinePreferences
                        .getPreferredWeatherLocation(getActivity());

                URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery, "metric");

                try {

                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(getActivity(), jsonWeatherResponse);

                    return simpleJsonWeatherData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String[] data) {
                queryData = data;
                super.deliverResult(data);
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        progressBar.setVisibility(View.INVISIBLE);
        if (data != null)
        {
            showData();
            adapter.clear();
            Log.d(LOG_TAG, "onLoadFinished: " + data[0]);
            for (String dayForecastStr : data) {
                adapter.addItem(dayForecastStr, this);
            }
        } else {
            showError();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {
        adapter.swap(null);
    }

    private void invalidateData() {
        adapter.swap(null);
    }

    private void showData() {
        recyclerView.setVisibility(View.VISIBLE);
        errorMessageTextView.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        errorMessageTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(LOG_TAG, "onStart: preferences were updated");
            getActivity().getSupportLoaderManager().restartLoader(FETCH_WEATHER_LOADER, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_refresh) {
            invalidateData();
            getActivity().getSupportLoaderManager().restartLoader(FETCH_WEATHER_LOADER, null, this);
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }
}
