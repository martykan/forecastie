package cz.martykan.forecastie.notifications.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.ImmutableWeather;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

/**
 * Observe change in the Shared Preferences and update WeatherPresentation when weather info or
 * settings how to show weather is changed.
 *
 * Implementation Note: Observer pattern is preferable than use of startService with data in
 * Intent when some class updates data because Observer pattern grants us one source of truth.
 */
public class WeatherRepository {
    private String typeKey;
    private String typeDefaultKey;
    private String typeSimpleKey;
    private String typeDefault;

    private final Executor executor;
    private SharedPreferences prefs;
    private final Set<WeakReference<RepositoryListener>> listeners = new HashSet<>();
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
    private final AtomicReference<WeatherPresentation> weatherPresentation = new AtomicReference<>();

    public WeatherRepository(@NonNull Context context, @NonNull Executor executor) {
        this.executor = executor;
        prepareSettingsConstants(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    public WeatherPresentation getWeather() {
        WeatherPresentation result = weatherPresentation.get();
        if (listeners.isEmpty() || result == null) {
            result = readValuesFromStorage();
        }
        return result;
    }

    /**
     * Start to observe weather data and settings. Current data will be emitted immediately.
     * <br/>
     * Repository doesn't hold strong reference on the listener so you should keep it while it is
     * needed by yourself.
     * <br/>
     * NOTE: do NOT use this method after call to {@link #clear()}
     * @param repositoryListener listener to emit data.
     * @throws IllegalStateException if repository already cleared by call of {@link #clear()}
     */
    public void observeWeather(@NonNull RepositoryListener repositoryListener) throws IllegalStateException {
        if (prefs == null)
            throw new IllegalStateException("DO NOT call this method after clear.");

        synchronized (listeners) {
            listeners.add(new WeakReference<>(repositoryListener));
            if (onSharedPreferenceChangeListener == null) {
                onSharedPreferenceChangeListener = new OnChangeListener(weatherPresentation);
                prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
            }
            if (listeners.size() == 1) {
                weatherPresentation.set(readValuesFromStorage());
            }
            repositoryListener.onChange(weatherPresentation.get());
        }
    }

    /**
     * Clear resources.
     *
     * NOTE: do NOT use {@link #observeWeather(RepositoryListener)} after this call.
     */
    public void clear() {
        synchronized (listeners) {
            listeners.clear();
        }
        SharedPreferences prefs = this.prefs;
        if (prefs != null) {
            SharedPreferences.OnSharedPreferenceChangeListener listener = onSharedPreferenceChangeListener;
            if (listener != null) {
                prefs.unregisterOnSharedPreferenceChangeListener(listener);
                onSharedPreferenceChangeListener = null;
            }
            this.prefs = null;
        }
    }

    /**
     * Initialize values from Shared Preferences to show weather info into notification.
     */
    @NonNull
    private WeatherPresentation readValuesFromStorage() {
        WeatherFormatterType type = readNotificationType(prefs);
        String json = prefs.getString("lastToday", "{}");
        long lastUpdate = prefs.getLong("lastUpdate", -1L);
        ImmutableWeather weather = ImmutableWeather.fromJson(json, lastUpdate);

        return new WeatherPresentation(
                prefs.getBoolean("temperatureInteger", WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE),
                prefs.getString("unit", WeatherPresentation.DEFAULT_TEMPERATURE_UNITS),
                prefs.getString("speedUnit", WeatherPresentation.DEFAULT_WIND_SPEED_UNITS),
                prefs.getString("windDirectionFormat", WeatherPresentation.DEFAULT_WIND_DIRECTION_FORMAT),
                prefs.getString("pressureUnit", WeatherPresentation.DEFAULT_PRESSURE_UNITS),
                weather, type
        );
    }

    /** Retrieve notification type from preferences. */
    @NonNull
    private WeatherFormatterType readNotificationType(@NonNull SharedPreferences prefs) {
        WeatherFormatterType result;
        String typePref = prefs.getString(typeKey, typeDefault);
        if (typePref != null && typePref.equalsIgnoreCase(typeDefaultKey)) {
            result = WeatherFormatterType.NOTIFICATION_DEFAULT;
        } else if (typePref != null && typePref.equalsIgnoreCase(typeSimpleKey)) {
            result = WeatherFormatterType.NOTIFICATION_SIMPLE;
        } else {
            if (typeDefault == null || typeDefault.equalsIgnoreCase(typeDefaultKey)) {
                result = WeatherFormatterType.NOTIFICATION_DEFAULT;
            } else {
                result = WeatherFormatterType.NOTIFICATION_SIMPLE;
            }
        }
        return result;
    }

    private void prepareSettingsConstants(@NonNull Context context) {
        typeKey = context.getString(R.string.settings_notification_type_key);
        typeDefaultKey = context.getString(R.string.settings_notification_type_key_default);
        typeSimpleKey = context.getString(R.string.settings_notification_type_key_simple);
        typeDefault = context.getString(R.string.settings_notification_type_default_value);
    }

    /** Callback method to get updated weather data and settings. */
    public interface RepositoryListener {
        /**
         * This method will be invoked immediately at beginning of observation and on every data or
         * settings update.
         * @param newData weather data and settings.
         */
        void onChange(@NonNull WeatherPresentation newData);
    }

    private class OnChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final AtomicReference<WeatherPresentation> weatherPresentation;

        private OnChangeListener(@NonNull AtomicReference<WeatherPresentation> weatherPresentation) {
            this.weatherPresentation = weatherPresentation;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            executor.execute(new UpdateDataFromStorage(weatherPresentation, sharedPreferences, key));
        }

        private class UpdateDataFromStorage implements Runnable {
            private final AtomicReference<WeatherPresentation> weatherPresentation;
            private final SharedPreferences sharedPreferences;
            private final String key;

            private UpdateDataFromStorage(
                    @NonNull AtomicReference<WeatherPresentation> weatherPresentation,
                    SharedPreferences sharedPreferences, String key
            ) {
                this.weatherPresentation = weatherPresentation;
                this.sharedPreferences = sharedPreferences;
                this.key = key;
            }

            @Override
            public void run() {
                if (sharedPreferences == null || key == null)
                    return;

                WeatherPresentation weatherPresentation = this.weatherPresentation.get();
                WeatherPresentation result = null;
                switch (key) {
                    case "lastUpdate":
                    case "lastToday":
                        String json = sharedPreferences.getString("lastToday", "");
                        if (!json.isEmpty()) {
                            long lastUpdate = sharedPreferences.getLong("lastUpdate", -1L);
                            ImmutableWeather weather = ImmutableWeather.fromJson(json, lastUpdate);
                            result = weatherPresentation.copy(weather);
                        }
                        break;
                    case "temperatureInteger":
                        boolean roundTemperature = sharedPreferences.getBoolean(key,
                                WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE);
                        result = weatherPresentation.copy(roundTemperature);
                        break;
                    case "unit":
                        String temperatureUnits = sharedPreferences.getString(key,
                                WeatherPresentation.DEFAULT_TEMPERATURE_UNITS);
                        result = weatherPresentation.copyTemperatureUnits(temperatureUnits);
                        break;
                    case "speedUnit":
                        String windSpeedUnits = sharedPreferences.getString(key,
                                WeatherPresentation.DEFAULT_WIND_SPEED_UNITS);
                        result = weatherPresentation.copyWindSpeedUnits(windSpeedUnits);
                        break;
                    case "windDirectionFormat":
                        String windDirectionFormat = sharedPreferences.getString(key,
                                WeatherPresentation.DEFAULT_WIND_DIRECTION_FORMAT);
                        result = weatherPresentation.copyWindDirectionFormat(windDirectionFormat);
                        break;
                    case "pressureUnit":
                        String pressureUnits = sharedPreferences.getString(key,
                                WeatherPresentation.DEFAULT_PRESSURE_UNITS);
                        result = weatherPresentation.copyPressureUnits(pressureUnits);
                        break;

                    default:
                        if (key.equalsIgnoreCase(typeKey)) {
                            result = weatherPresentation.copy(readNotificationType(sharedPreferences));
                        }
                        break;
                }

                if (result != null && this.weatherPresentation.compareAndSet(weatherPresentation, result)) {
                    new Handler(Looper.getMainLooper()).post(new PostData(result));
                }
            }
        }

        private class PostData implements Runnable {
            private final WeatherPresentation weatherPresentation;

            private PostData(@Nullable WeatherPresentation weatherPresentation) {
                this.weatherPresentation = weatherPresentation;
            }

            @Override
            public void run() {
                if (weatherPresentation != null) {
                    RepositoryListener listener;
                    Iterator<WeakReference<RepositoryListener>> iter = listeners.iterator();
                    while (iter.hasNext()) {
                        listener = iter.next().get();
                        if (listener != null) {
                            listener.onChange(weatherPresentation);
                        } else {
                            iter.remove();
                        }
                    }
                }
                if (listeners.isEmpty()) {
                    if (prefs != null) {
                        prefs.unregisterOnSharedPreferenceChangeListener(OnChangeListener.this);
                        onSharedPreferenceChangeListener = null;
                    }
                }
            }
        }
    }
}