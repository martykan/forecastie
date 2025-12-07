package cz.martykan.forecastie.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import cz.martykan.forecastie.Constants;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.utils.Language;
import cz.martykan.forecastie.utils.certificate.CertificateUtils;
import cz.martykan.forecastie.weatherapi.WeatherStorage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class GenericRequestTask extends AsyncTask<String, String, TaskOutput> {

    ProgressDialog progressDialog;
    protected Context context;
    protected MainActivity activity;
    protected WeatherStorage weatherStorage;
    private static CountDownLatch certificateCountDownLatch = new CountDownLatch(0);
    private static boolean certificateTried = false;
    private static boolean certificateFetchTried = false;
    private static SSLContext sslContext;
    
    private OkHttpClient okHttpClient;

    public GenericRequestTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        this.context = context;
        this.activity = activity;
        this.progressDialog = progressDialog;
        this.weatherStorage = new WeatherStorage(activity);
        this.okHttpClient = new OkHttpClient();
    }

    @Override
    protected void onPreExecute() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage(context.getString(R.string.downloading_data));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected TaskOutput doInBackground(String... params) {
        String[] reqParams = new String[]{};

        if (params != null && params.length > 0) {
            final String zeroParam = params[0];
            if ("coords".equals(zeroParam)) {
                String lat = params[1];
                String lon = params[2];
                reqParams = new String[]{"coords", lat, lon};
            } else if ("city".equals(zeroParam)) {
                reqParams = new String[]{"city", params[1]};
            }
        }

        TaskOutput requestOutput = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP
            ? makeRequest(reqParams)
            : makeRequestWithCheckForCertificate(reqParams);

        if (TaskResult.SUCCESS.equals(requestOutput .taskResult)) {
            requestOutput.parseResult = parseResponse(requestOutput.response);
            if (ParseResult.CITY_NOT_FOUND.equals(requestOutput.parseResult)) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity();
            }
        }

        return requestOutput ;
    }

    private TaskOutput makeRequest(String[] reqParams) {
        TaskOutput output = new TaskOutput();
        try {
            URL url = provideURL(reqParams);
            Log.i("URL", url.toString());
            
            Request request = new Request.Builder()
                    .url(url.toString())
                    .build();
            
            Response responseObj = okHttpClient.newCall(request).execute();
            if (responseObj.isSuccessful()) {
                String responseBody = responseObj.body().string();
                output.response = responseBody;
                // Background work finished successfully
                Log.i("Task", "done successfully");
                output.taskResult = TaskResult.SUCCESS;
                // Save date/time for latest successful result
                MainActivity.saveLastUpdateTime(PreferenceManager.getDefaultSharedPreferences(context));
            } else if (responseObj.code() == 401) {
                // Invalid API key
                Log.w("Task", "invalid API key");
                output.taskResult = TaskResult.INVALID_API_KEY;
            } else if (responseObj.code() == 429) {
                // Too many requests
                Log.w("Task", "too many requests");
                output.taskResult = TaskResult.TOO_MANY_REQUESTS;
            } else {
                // Bad response from server
                Log.w("Task", "http error " + responseObj.code());
                output.taskResult = TaskResult.HTTP_ERROR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Exception while reading data from url connection
            output.taskResult = TaskResult.IO_EXCEPTION;
            output.taskError = e;
        }
        
        return output;
    }

    private TaskOutput makeRequestWithCheckForCertificate(String[] reqParams) {
        TaskOutput output = new TaskOutput();
        boolean tryAgain = false;
        String response = "";
        do {
            output = makeRequest(reqParams);
            if (output.taskResult == TaskResult.IO_EXCEPTION && output.taskError instanceof IOException) {
                if (CertificateUtils.isCertificateException((IOException) output.taskError)) {
                    Log.e("Invalid Certificate", output.taskError.getMessage());
                    try {
                        certificateCountDownLatch.await();
                        tryAgain = !certificateTried || !certificateFetchTried;
                        if (tryAgain) {
                            AtomicBoolean doNotRetry = new AtomicBoolean(false);
                            sslContext = CertificateUtils.addCertificate(context, doNotRetry,
                                    certificateTried);
                            certificateTried = true;
                            if (!certificateFetchTried) {
                                certificateFetchTried = doNotRetry.get();
                            }
                            tryAgain = sslContext != null;
                        }
                        certificateCountDownLatch.countDown();
                    } catch (InterruptedException ex) {
                        Log.e("Invalid Certificate", "await had been interrupted");
                        ex.printStackTrace();
                    }
                } else {
                    Log.e("IOException Data", response);
                    tryAgain = false;
                }
            } else {
                tryAgain = false;
            }
        } while (tryAgain);
        return output;
    }

    @Override
    protected void onPostExecute(TaskOutput output) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (output.taskResult == TaskResult.SUCCESS) {
            updateMainUI();
        }

        handleTaskOutput(output);
    }

    protected final void handleTaskOutput(TaskOutput output) {
        switch (output.taskResult) {
            case SUCCESS:
                ParseResult parseResult = output.parseResult;
                if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_city_not_found), Snackbar.LENGTH_LONG).show();
                } else if (ParseResult.JSON_EXCEPTION.equals(parseResult)) {
                    Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_err_parsing_json), Snackbar.LENGTH_LONG).show();
                }
                break;
            case TOO_MANY_REQUESTS:
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_too_many_requests), Snackbar.LENGTH_LONG).show();
                break;
            case INVALID_API_KEY:
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_invalid_api_key), Snackbar.LENGTH_LONG).show();
                break;
            case HTTP_ERROR:
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_http_error), Snackbar.LENGTH_LONG).show();
                break;
            case IO_EXCEPTION:
                Snackbar.make(activity.findViewById(android.R.id.content), context.getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    private URL provideURL(String[] reqParams) throws MalformedURLException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey = sp.getString("apiKey", context.getString(R.string.apiKey));

        Uri.Builder uriBuilder = new Uri.Builder()
            .scheme("https")
            .authority("api.openweathermap.org")
            .path("/data/2.5/" + getAPIName());
        
        if (reqParams.length > 0) {
            final String zeroParam = reqParams[0];
            if ("coords".equals(zeroParam)) {
                uriBuilder.appendQueryParameter("lat", reqParams[1]);
                uriBuilder.appendQueryParameter("lon", reqParams[2]);
            } else if ("city".equals(zeroParam)) {
                uriBuilder.appendQueryParameter("q", reqParams[1]);
            }
        } else {
            final String cityId = sp.getString("cityId", Constants.DEFAULT_CITY_ID);
            uriBuilder.appendQueryParameter("id", cityId);
        }
        
        // Add common parameters
        uriBuilder.appendQueryParameter("lang", Language.getOwmLanguage());
        uriBuilder.appendQueryParameter("mode", "json");
        uriBuilder.appendQueryParameter("appid", apiKey);
        
        return new URL(uriBuilder.build().toString());
    }

    @SuppressLint("ApplySharedPref")
    private void restorePreviousCity() {
        if (activity.recentCityId != null) {
            weatherStorage.setCityId(activity.recentCityId);
            activity.recentCityId = null;
        }
    }

    protected void updateMainUI() {
    }

    protected abstract ParseResult parseResponse(String response);

    protected abstract String getAPIName();
}
