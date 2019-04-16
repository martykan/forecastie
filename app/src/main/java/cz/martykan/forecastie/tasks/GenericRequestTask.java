package cz.martykan.forecastie.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import cz.martykan.forecastie.Constants;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;

public abstract class GenericRequestTask extends AsyncTask<String, String, TaskOutput> {

    ProgressDialog progressDialog;
    Context context;
    MainActivity activity;
    public int loading = 0;

    public GenericRequestTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        this.context = context;
        this.activity = activity;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        incLoadingCounter();
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage(context.getString(R.string.downloading_data));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected TaskOutput doInBackground(String... params) {
        TaskOutput output = new TaskOutput();

        String response = "";
        String[] reqParams = new String[]{};

        if (params != null && params.length > 0) {
            final String zeroParam = params[0];
            if ("cachedResponse".equals(zeroParam)) {
                response = params[1];
                // Actually we did nothing in this case :)
                output.taskResult = TaskResult.SUCCESS;
            } else if ("coords".equals(zeroParam)) {
                String lat = params[1];
                String lon = params[2];
                reqParams = new String[]{"coords", lat, lon};
            } else if ("city".equals(zeroParam)) {
                reqParams = new String[]{"city", params[1]};
            }
        }

        if (response.isEmpty()) {
            try {
                URL url = provideURL(reqParams);
                Log.i("URL", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader r = new BufferedReader(inputStreamReader);

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }
                    response += stringBuilder.toString();
                    close(r);
                    urlConnection.disconnect();
                    // Background work finished successfully
                    Log.i("Task", "done successfully");
                    output.taskResult = TaskResult.SUCCESS;
                    // Save date/time for latest successful result
                    activity.saveLastUpdateTime(PreferenceManager.getDefaultSharedPreferences(context));
                } else if (urlConnection.getResponseCode() == 429) {
                    // Too many requests
                    Log.i("Task", "too many requests");
                    output.taskResult = TaskResult.TOO_MANY_REQUESTS;
                } else {
                    // Bad response from server
                    Log.i("Task", "bad response " + urlConnection.getResponseCode());
                    output.taskResult = TaskResult.BAD_RESPONSE;
                }
            } catch (IOException e) {
                Log.e("IOException Data", response);
                e.printStackTrace();
                // Exception while reading data from url connection
                output.taskResult = TaskResult.IO_EXCEPTION;
            }
        }

        if (TaskResult.SUCCESS.equals(output.taskResult)) {
            // Parse JSON data
            ParseResult parseResult = parseResponse(response);
            if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity();
            }
            output.parseResult = parseResult;
        }

        return output;
    }

    @Override
    protected void onPostExecute(TaskOutput output) {
        if (loading == 1) {
            progressDialog.dismiss();
        }
        decLoadingCounter();

        updateMainUI();

        handleTaskOutput(output);
    }

    protected final void handleTaskOutput(TaskOutput output) {
        switch (output.taskResult) {
            case SUCCESS: {
                ParseResult parseResult = output.parseResult;
                if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_city_not_found), Snackbar.LENGTH_LONG).show();
                } else if (ParseResult.JSON_EXCEPTION.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_err_parsing_json), Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case TOO_MANY_REQUESTS: {
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_too_many_requests), Snackbar.LENGTH_LONG).show();
                break;
            }
            case BAD_RESPONSE: {
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
                break;
            }
            case IO_EXCEPTION: {
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
                break;
            }
        }
    }

    private String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("cs")) {
            language = "cz";
        }
        return language;
    }

    private URL provideURL(String[] reqParams) throws UnsupportedEncodingException, MalformedURLException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey = sp.getString("apiKey", activity.getResources().getString(R.string.apiKey));

        StringBuilder urlBuilder = new StringBuilder("https://api.openweathermap.org/data/2.5/");
        urlBuilder.append(getAPIName()).append("?");
        if (reqParams.length > 0) {
            final String zeroParam = reqParams[0];
            if ("coords".equals(zeroParam)) {
                urlBuilder.append("lat=").append(reqParams[1]).append("&lon=").append(reqParams[2]);
            } else if ("city".equals(zeroParam)) {
                urlBuilder.append("q=").append(reqParams[1]);
            }
        } else {
            final String cityId = sp.getString("cityId", Constants.DEFAULT_CITY_ID);
            urlBuilder.append("id=").append(URLEncoder.encode(cityId, "UTF-8"));
        }
        urlBuilder.append("&lang=").append(getLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&appid=").append(apiKey);

        return new URL(urlBuilder.toString());
    }

    private void restorePreviousCity() {
        if (!TextUtils.isEmpty(activity.recentCityId)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("cityId", activity.recentCityId);
            editor.commit();
            activity.recentCityId = "";
        }
    }

    private static void close(Closeable x) {
        try {
            if (x != null) {
                x.close();
            }
        } catch (IOException e) {
            Log.e("IOException Data", "Error occurred while closing stream");
        }
    }

    private void incLoadingCounter() {
        loading++;
    }

    private void decLoadingCounter() {
        loading--;
    }

    protected void updateMainUI() {
    }

    protected abstract ParseResult parseResponse(String response);

    protected abstract String getAPIName();
}
