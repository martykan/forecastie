package cz.martykan.forecastie.utils.formatters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.ImmutableWeather;

/**
 * Converter from raw {@link ImmutableWeather wather info} into strings to show to a user.
 * <br/>
 * NOTE: default implementation for all methods is to throw {@link UnsupportedOperationException}.
 */
@SuppressWarnings("unused")
public abstract class WeatherFormatter {
    public static final int DEFAULT_ICON_TEXT_SIZE = 24;
    public static final int MIN_ICON_TEXT_SIZE = 14;

    /**
     * Check is {@code weather} has enough valid data to show all necessary weather information to
     * a user or {@code no data} should be shown.
     * <br/>
     * NOTE: Derived class could have more specified checks and this implementation should return
     * {@code false} if any of data that can be displayed is not valid.
     * @param weather weather information
     * @return {@code true} if weather information valid and {@code false} if {@code no data}
     * should be shown
     */
    public abstract boolean isEnoughValidData(@NonNull ImmutableWeather weather);

    /**
     * Returns temperature with units if needed.
     * @param weather weather information
     * @param temperatureUnit temperature units
     * @param roundedTemperature if {@code true} round temperature and show as integer
     * @return temperature with units if needed
     */
    @NonNull
    public String getTemperature(@NonNull ImmutableWeather weather,
                                 @NonNull String temperatureUnit,
                                 boolean roundedTemperature) {
        throw new UnsupportedOperationException("getTemperature hasn't been implemented");
    }

    /**
     * Returns formatted weather description.
     * @param weather weather information
     * @return formatted weather description
     */
    @NonNull
    public String getDescription(@NonNull ImmutableWeather weather) {
        throw new UnsupportedOperationException("getDescription hasn't been implemented");
    }

    /**
     * Returns weather wind title, wind speed in specified units and wind direction in specified
     * format.
     * @param weather weather information
     * @param units wind speed units
     * @param directionFormat wind direction format
     * @param context android context
     * @return formatted wind
     */
    // TODO rewrite with enums instead of Strings
    @NonNull
    public String getWind(@NonNull ImmutableWeather weather, @NonNull String units,
                          @NonNull String directionFormat, @NonNull Context context) {
        throw new UnsupportedOperationException("getWind hasn't been implemented");
    }

    /**
     * Returns pressure title, pressure in specified units and units.
     * @param weather weather information
     * @param units pressure units
     * @param context android context
     * @return formatted pressure
     */
    // TODO rewrite units with enum
    @NonNull
    public String getPressure(@NonNull ImmutableWeather weather, @NonNull String units,
                              @NonNull Context context) {
        throw new UnsupportedOperationException("getPressure hasn't been implemented");
    }

    /**
     * Returns humidity title, humidity value and per cent symbol.
     * @param weather weather information
     * @param context android context
     * @return formatted humidity
     */
    @NonNull
    public String getHumidity(@NonNull ImmutableWeather weather, @NonNull Context context) {
        throw new UnsupportedOperationException("getHumidity hasn't been implemented");
    }

    /**
     * Returns weather icon as {@link Bitmap}.
     * @param weather weather information
     * @param context android context
     * @return weather icon as {@link Bitmap}
     */
    @NonNull
    public Bitmap getWeatherIconAsBitmap(@NonNull ImmutableWeather weather,
                                         @NonNull Context context) {
        throw new UnsupportedOperationException("getWeatherIconAsBitmap hasn't been implemented");
    }

    /**
     * Returns weather icon as {@link String}.
     * @param weather weather information
     * @param context android context
     * @return weather icon as {@link String}
     */
    @NonNull
    public String getWeatherIconAsText(@NonNull ImmutableWeather weather,
                                       @NonNull Context context) {
        throw new UnsupportedOperationException("getWeatherIconAsText hasn't been implemented");
    }

    /**
     * Returns weather icon as {@link String}.
     * @param weatherId weather icon id
     * @param isDay {@code true} if should be chosen icon for day and {@code false} for night
     * @param context android context
     * @return weather icon as {@link String}
     */
    // TODO static is temporary solution to avoid code duplication. Should be moved in another
    // class and retrieved through DI.
    @NonNull
    public static String getWeatherIconAsText(int weatherId, boolean isDay,
                                              @NonNull Context context) {
        int id = weatherId / 100;
        String icon = "";

        if (id == 2) {
            // thunderstorm
            switch (weatherId) {
                case 210:
                case 211:
                case 212:
                case 221:
                    icon = context.getString(R.string.weather_lightning);
                    break;
                case 200:
                case 201:
                case 202:
                case 230:
                case 231:
                case 232:
                default:
                    icon = context.getString(R.string.weather_thunderstorm);
                    break;
            }
        } else if (id == 3) {
            // drizzle/sprinkle
            switch (weatherId) {
                case 302:
                case 311:
                case 312:
                case 314:
                    icon = context.getString(R.string.weather_rain);
                    break;
                case 310:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 313:
                    icon = context.getString(R.string.weather_showers);
                    break;
                case 300:
                case 301:
                case 321:
                default:
                    icon = context.getString(R.string.weather_sprinkle);
                    break;
            }
        } else if (id == 5) {
            // rain
            switch (weatherId) {
                case 500:
                    icon = context.getString(R.string.weather_sprinkle);
                    break;
                case 511:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 520:
                case 521:
                case 522:
                    icon = context.getString(R.string.weather_showers);
                    break;
                case 531:
                    icon = context.getString(R.string.weather_storm_showers);
                    break;
                case 501:
                case 502:
                case 503:
                case 504:
                default:
                    icon = context.getString(R.string.weather_rain);
                    break;
            }
        } else if (id == 6) {
            // snow
            switch (weatherId) {
                case 611:
                    icon = context.getString(R.string.weather_sleet);
                    break;
                case 612:
                case 613:
                case 615:
                case 616:
                case 620:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 600:
                case 601:
                case 602:
                case 621:
                case 622:
                default:
                    icon = context.getString(R.string.weather_snow);
                    break;
            }
        } else if (id == 7) {
            // atmosphere
            switch (weatherId) {
                case 711:
                    icon = context.getString(R.string.weather_smoke);
                    break;
                case 721:
                    icon = context.getString(R.string.weather_day_haze);
                    break;
                case 731:
                case 761:
                case 762:
                    icon = context.getString(R.string.weather_dust);
                    break;
                case 751:
                    icon = context.getString(R.string.weather_sandstorm);
                    break;
                case 771:
                    icon = context.getString(R.string.weather_cloudy_gusts);
                    break;
                case 781:
                    icon = context.getString(R.string.weather_tornado);
                    break;
                case 701:
                case 741:
                default:
                    icon = context.getString(R.string.weather_fog);
                    break;
            }
        } else if (id == 8) {
            // clear sky or cloudy
            switch (weatherId) {
                case 800:
                    icon = isDay
                            ? context.getString(R.string.weather_day_sunny)
                            : context.getString(R.string.weather_night_clear);
                    break;
                case 801:
                case 802:
                    icon = isDay
                            ? context.getString(R.string.weather_day_cloudy)
                            : context.getString(R.string.weather_night_alt_cloudy);
                    break;
                case 803:
                case 804:
                default:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
            }
        } else if (id == 9) {
            switch (weatherId) {
                case 900:
                    icon = context.getString(R.string.weather_tornado);
                    break;
                case 901:
                    icon = context.getString(R.string.weather_storm_showers);
                    break;
                case 902:
                    icon = context.getString(R.string.weather_hurricane);
                    break;
                case 903:
                    icon = context.getString(R.string.weather_snowflake_cold);
                    break;
                case 904:
                    icon = context.getString(R.string.weather_hot);
                    break;
                case 905:
                    icon = context.getString(R.string.weather_windy);
                    break;
                case 906:
                    icon = context.getString(R.string.weather_hail);
                    break;
                case 957:
                default:
                    icon = context.getString(R.string.weather_strong_wind);
                    break;
            }
        }

        return icon;
    }

    /**
     * Returns weather icon as {@link Bitmap}.
     * @param context android context
     * @param text weather icon as String
     * @param color text color (not a color resource)
     * @return weather icon as {@link Bitmap}
     * @see #getWeatherIconAsText(int, boolean, Context)
     */
    // TODO static is temporary solution to avoid code duplication.  Should be moved in another
    // class and retrieved through DI.
    @NonNull
    public static Bitmap getWeatherIconAsBitmap(@NonNull Context context, @NonNull String text,
                                                int color) {
        Bitmap myBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = getPaint(color);
        Typeface clock = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        paint.setTypeface(clock);
        paint.setTextSize(150);
        myCanvas.drawText(text, 128, 180, paint);
        return myBitmap;
    }

    /**
     * Returns weather icon as {@link Bitmap}.
     * @param context android context
     * @param weather weather information
     * @param temperatureUnit temperature unit
     * @param color text color (not a color resource)
     * @return weather icon as {@link Bitmap}
     */
    // TODO static is temporary solution to avoid code duplication. Should be moved in another
    // class and retrieved through DI.
    @NonNull
    public static Bitmap getTemperatureAsBitmap(
            @NonNull Context context,
            @NonNull ImmutableWeather weather,
            @NonNull String temperatureUnit,
            int color
    ) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ICON_TEXT_SIZE, displayMetrics);
        final float oneDp = size / DEFAULT_ICON_TEXT_SIZE;
        final float minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_ICON_TEXT_SIZE - 1, displayMetrics);
        final Bitmap bitmap = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_4444);
        final int temperature = Math.round(weather.getTemperature(temperatureUnit));
        drawTemperature(bitmap, getTemperaturePaint(color), temperature, size, oneDp, minTextSize);
        return bitmap;
    }

    private static void drawTemperature(
            @NonNull Bitmap bitmap,
            @NonNull Paint paint,
            final int temperature,
            final float size, final float oneDp, final float minTextSize
    ) {
        final Canvas canvas = new Canvas(bitmap);
        float textSize = size;
        final String text = temperature + "°";

        Rect bounds = new Rect();
        float measuredWidth;
        do {
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
            measuredWidth = bounds.width();
            textSize -= oneDp;
        } while (measuredWidth > size && textSize > minTextSize);

        final float textHeight = (float) bounds.height();
        final float verticalPadding = (size - textHeight) / 2f;

        final float x = size / 2f;
        final float y = verticalPadding + textHeight;
        canvas.drawText(text, x, y, paint);
    }

    @NonNull
    private static Paint getTemperaturePaint(int color) {
        final Paint paint = getPaint(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paint.setLetterSpacing(-0.05f);
        }
        //paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        return paint;
    }

    @NonNull
    private static Paint getPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(color);
        paint.setTextAlign(Paint.Align.CENTER);
        return paint;
    }
}