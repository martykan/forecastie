package cz.martykan.forecastie.weatherapi.owm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import cz.martykan.forecastie.models.Weather;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class OpenWeatherMapJsonParserTest {
    @Test
    public void testConvertJsonToWeather() throws JSONException {
        String jsonWeatherBasic = "{\"coord\":{\"lon\":-122.08,\"lat\":37.39},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":282.55,\"feels_like\":281.86,\"temp_min\":280.37,\"temp_max\":284.26,\"pressure\":1023,\"humidity\":100},\"visibility\":16093,\"wind\":{\"speed\":1.5,\"deg\":350},\"clouds\":{\"all\":1},\"dt\":1560350645,\"sys\":{\"type\":1,\"id\":5122,\"message\":0.0139,\"country\":\"US\",\"sunrise\":1560343627,\"sunset\":1560396563},\"timezone\":-25200,\"id\":420006353,\"name\":\"Mountain View\",\"cod\":200}";
        Weather parsedWeatherBasic = OpenWeatherMapJsonParser.convertJsonToWeather(jsonWeatherBasic);

        Assert.assertEquals("City ID doesn't match", 420006353, parsedWeatherBasic.getCityId());
        Assert.assertEquals("City name doesn't match", "Mountain View", parsedWeatherBasic.getCity());
        Assert.assertEquals("Country doesn't match", "US", parsedWeatherBasic.getCountry());

        Assert.assertEquals("Date doesn't match", new Date(1560350645000L), parsedWeatherBasic.getDate());

        Assert.assertEquals("Longitude doesn't match", -122.08, parsedWeatherBasic.getLon(), 0);
        Assert.assertEquals("Latitude doesn't match", 37.39, parsedWeatherBasic.getLat(), 0);

        Assert.assertEquals("Weather ID doesn't match", 800, parsedWeatherBasic.getWeatherId());
        Assert.assertEquals("Weather description doesn't match", "Clear sky", parsedWeatherBasic.getDescription());
        Assert.assertEquals("Temperature doesn't match", 282.55, parsedWeatherBasic.getTemperature(), 0);
        Assert.assertEquals("Pressure doesn't match", 1023, parsedWeatherBasic.getPressure(), 0);
        Assert.assertEquals("Humidity doesn't match", 100, parsedWeatherBasic.getHumidity(), 0);

        Assert.assertEquals("Wind speed doesn't match", 1.5, parsedWeatherBasic.getWind(), 0);
        Assert.assertEquals("Wind direction doesn't match", 350, parsedWeatherBasic.getWindDirectionDegree(), 0);

        Assert.assertEquals("Sunrise doesn't match", new Date(1560343627000L), parsedWeatherBasic.getSunrise());
        Assert.assertEquals("Sunset doesn't match", new Date(1560396563000L), parsedWeatherBasic.getSunset());

        String jsonWeatherRain = "{\"coord\":{\"lon\":-157.8583,\"lat\":21.3069},\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10n\"}],\"base\":\"stations\",\"main\":{\"temp\":295.78,\"feels_like\":296.06,\"temp_min\":294.73,\"temp_max\":297.05,\"pressure\":1016,\"humidity\":75},\"visibility\":10000,\"wind\":{\"speed\":1.79,\"deg\":73,\"gust\":4.47},\"rain\":{\"1h\":0.76},\"clouds\":{\"all\":40},\"dt\":1639899427,\"sys\":{\"type\":2,\"id\":2010198,\"country\":\"US\",\"sunrise\":1639846995,\"sunset\":1639886002},\"timezone\":-36000,\"id\":5856195,\"name\":\"Honolulu\",\"cod\":200}";
        Weather parsedWeatherRain = OpenWeatherMapJsonParser.convertJsonToWeather(jsonWeatherRain);
        Assert.assertEquals("Rain doesn't match", 0.76, parsedWeatherRain.getRain(), 0);

        String jsonWeatherSnow = "{\"coord\":{\"lon\":37.6156,\"lat\":55.7522},\"weather\":[{\"id\":600,\"main\":\"Snow\",\"description\":\"light snow\",\"icon\":\"13d\"}],\"base\":\"stations\",\"main\":{\"temp\":271.32,\"feels_like\":267.4,\"temp_min\":270.52,\"temp_max\":271.69,\"pressure\":985,\"humidity\":97,\"sea_level\":985,\"grnd_level\":967},\"visibility\":556,\"wind\":{\"speed\":3.06,\"deg\":238,\"gust\":8.4},\"snow\":{\"1h\":0.13},\"clouds\":{\"all\":100},\"dt\":1639899167,\"sys\":{\"type\":2,\"id\":2000314,\"country\":\"RU\",\"sunrise\":1639893385,\"sunset\":1639918612},\"timezone\":10800,\"id\":524901,\"name\":\"Moscow\",\"cod\":200}";
        Weather parsedWeatherSnow = OpenWeatherMapJsonParser.convertJsonToWeather(jsonWeatherSnow);
        Assert.assertEquals("Snow doesn't match", 0.13, parsedWeatherSnow.getRain(), 0);
    }

    @Test
    public void testConvertJsonToUVIndex() throws JSONException {
        String jsonUVIndex = "{\"value\": 10.06}";

        double parsedUVIndex = OpenWeatherMapJsonParser.convertJsonToUVIndex(jsonUVIndex);

        Assert.assertEquals("UV-Index doesn't match", 10.06, parsedUVIndex, 0);
    }

    @Test
    public void testConvertJsonToWeatherList() throws JSONException {
        String jsonWeatherList = "{\"cod\":\"200\",\"message\":0,\"cnt\":40,\"list\":[{\"dt\":1640541600,\"main\":{\"temp\":281.93,\"feels_like\":281.93,\"temp_min\":280.29,\"temp_max\":281.93,\"pressure\":1000,\"sea_level\":1000,\"grnd_level\":997,\"humidity\":97,\"temp_kf\":1.64},\"weather\":[{\"id\":804,\"main\":\"Clouds\",\"description\":\"overcast clouds\",\"icon\":\"04n\"}],\"clouds\":{\"all\":90},\"wind\":{\"speed\":0.62,\"deg\":54,\"gust\":0.66},\"visibility\":10000,\"pop\":0.28,\"sys\":{\"pod\":\"n\"},\"dt_txt\":\"2021-12-26 18:00:00\"},{\"dt\":1640606400,\"main\":{\"temp\":281.41,\"feels_like\":279.37,\"temp_min\":281.41,\"temp_max\":281.41,\"pressure\":994,\"sea_level\":994,\"grnd_level\":990,\"humidity\":97,\"temp_kf\":0},\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"clouds\":{\"all\":100},\"wind\":{\"speed\":3.34,\"deg\":150,\"gust\":10.39},\"visibility\":10000,\"pop\":0.37,\"rain\":{\"3h\":0.18},\"sys\":{\"pod\":\"d\"},\"dt_txt\":\"2021-12-27 12:00:00\"},{\"dt\":1640919600,\"main\":{\"temp\":285.73,\"feels_like\":285.24,\"temp_min\":285.73,\"temp_max\":285.73,\"pressure\":1013,\"sea_level\":1013,\"grnd_level\":1010,\"humidity\":84,\"temp_kf\":0},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01n\"}],\"clouds\":{\"all\":2},\"wind\":{\"speed\":6.66,\"deg\":218,\"gust\":18.27},\"visibility\":10000,\"pop\":0,\"sys\":{\"pod\":\"n\"},\"dt_txt\":\"2021-12-31 03:00:00\"}],\"city\":{\"id\":2643743,\"name\":\"London\",\"coord\":{\"lat\":51.5085,\"lon\":-0.1257},\"country\":\"GB\",\"population\":1000000,\"timezone\":0,\"sunrise\":1640505945,\"sunset\":1640534177}}";
        List<Weather> parsedWeatherList = OpenWeatherMapJsonParser.convertJsonToWeatherList(jsonWeatherList);

        Assert.assertEquals("List length doesn't match", 3, parsedWeatherList.size());

        Assert.assertEquals("City ID doesn't match", 2643743, parsedWeatherList.get(0).getCityId());
        Assert.assertEquals("City name does not match", "London", parsedWeatherList.get(0).getCity());
        Assert.assertEquals("Country doesn't match", "GB", parsedWeatherList.get(0).getCountry());

        Assert.assertEquals("Date doesn't match", new Date(1640541600000L), parsedWeatherList.get(0).getDate());

        Assert.assertEquals("Longitude doesn't match", -0.1257, parsedWeatherList.get(0).getLon(), 0);
        Assert.assertEquals("Latitude doesn't match", 51.5085, parsedWeatherList.get(0).getLat(), 0);

        Assert.assertEquals("Weather ID doesn't match", 500, parsedWeatherList.get(1).getWeatherId());
        Assert.assertEquals("Weather description doesn't match", "Light rain", parsedWeatherList.get(1).getDescription());
        Assert.assertEquals("Temperature doesn't match", 281.41, parsedWeatherList.get(1).getTemperature(), 0);
        Assert.assertEquals("Pressure doesn't match", 994, parsedWeatherList.get(1).getPressure(), 0);
        Assert.assertEquals("Humidity doesn't match", 97, parsedWeatherList.get(1).getHumidity(), 0);

        Assert.assertEquals("Wind speed doesn't match", 6.66, parsedWeatherList.get(2).getWind(), 0);
        Assert.assertEquals("Wind direction doesn't match", 218, parsedWeatherList.get(2).getWindDirectionDegree(), 0);

        Assert.assertEquals("Sunrise doesn't match", new Date(1640505945000L), parsedWeatherList.get(0).getSunrise());
        Assert.assertEquals("Sunset doesn't match", new Date(1640534177000L), parsedWeatherList.get(0).getSunset());

        Assert.assertEquals("Rain doesn't match", 0.18, parsedWeatherList.get(1).getRain(), 0);
        Assert.assertEquals("Chance of precipitation doesn't match", 0.37, parsedWeatherList.get(1).getChanceOfPrecipitation(), 0);
    }
}