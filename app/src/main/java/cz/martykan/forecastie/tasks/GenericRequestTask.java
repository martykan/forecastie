package cz.martykan.forecastie.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import cz.martykan.forecastie.Constants;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.utils.Language;
import cz.martykan.forecastie.utils.certificate.CertificateUtils;
import cz.martykan.forecastie.weatherapi.WeatherStorage;

public abstract class GenericRequestTask extends AsyncTask<String, String, TaskOutput> {

    ProgressDialog progressDialog;
    protected Context context;
    protected MainActivity activity;
    protected WeatherStorage weatherStorage;
    public int loading = 0;

    private static CountDownLatch certificateCountDownLatch = new CountDownLatch(0);
    private static boolean certificateTried = false;
    private static boolean certificateFetchTried = false;
    private static SSLContext sslContext;

    public GenericRequestTask(Context context, MainActivity activity, ProgressDialog progressDialog) {
        this.context = context;
        this.activity = activity;
        this.progressDialog = progressDialog;
        this.weatherStorage = new WeatherStorage(activity);
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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                response = makeRequest(output, response, reqParams);
            } else {
                response = makeRequestWithCheckForCertificate(output, response, reqParams);
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

    private String makeRequest(TaskOutput output, String response, String[] reqParams) {
        try {
            URL url = provideURL(reqParams);
            Log.i("URL", url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection instanceof HttpsURLConnection) {
                try {
                    certificateCountDownLatch.await();
                    if (sslContext != null) {
                        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                        ((HttpsURLConnection) urlConnection).setSSLSocketFactory(socketFactory);
                    }
                    certificateCountDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
                MainActivity.saveLastUpdateTime(PreferenceManager.getDefaultSharedPreferences(context));
            } else if (urlConnection.getResponseCode() == 401) {
                // Invalid API key
                Log.w("Task", "invalid API key");
                output.taskResult = TaskResult.INVALID_API_KEY;
            } else if (urlConnection.getResponseCode() == 429) {
                // Too many requests
                Log.w("Task", "too many requests");
                output.taskResult = TaskResult.TOO_MANY_REQUESTS;
            } else {
                // Bad response from server
                Log.w("Task", "http error " + urlConnection.getResponseCode());
                output.taskResult = TaskResult.HTTP_ERROR;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Exception while reading data from url connection
            output.taskResult = TaskResult.IO_EXCEPTION;
            output.taskError = e;
        }

        return response;
    }

    private String makeRequestWithCheckForCertificate(TaskOutput output, String response, String[] reqParams) {
        boolean tryAgain = false;
        do {
            response = makeRequest(output, response, reqParams);
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
        return response;
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

    private URL provideURL(String[] reqParams) throws UnsupportedEncodingException, MalformedURLException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String apiKey = sp.getString("apiKey", context.getString(R.string.apiKey));

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
        urlBuilder.append("&lang=").append(Language.getOwmLanguage());
        urlBuilder.append("&mode=json");
        urlBuilder.append("&appid=").append(apiKey);

        return new URL(urlBuilder.toString());
    }

    @SuppressLint("ApplySharedPref")
    private void restorePreviousCity() {
        if (activity.recentCityId != null) {
            weatherStorage.setCityId(activity.recentCityId);
            activity.recentCityId = null;
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
