package cz.martykan.forecastie.weatherapi.owm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Date;

import cz.martykan.forecastie.models.Weather;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class OpenWeatherMapJsonParserTest {

    @Test
    public void testConvertJsonToWeatherList() {
        // TODO: Implement test case
    }

    @Test
    public void testConvertJsonToWeather() throws JSONException {
        String jsonWeatherBasic = "{\"coord\":{\"lon\":-122.08,\"lat\":37.39},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":282.55,\"feels_like\":281.86,\"temp_min\":280.37,\"temp_max\":284.26,\"pressure\":1023,\"humidity\":100},\"visibility\":16093,\"wind\":{\"speed\":1.5,\"deg\":350},\"clouds\":{\"all\":1},\"dt\":1560350645,\"sys\":{\"type\":1,\"id\":5122,\"message\":0.0139,\"country\":\"US\",\"sunrise\":1560343627,\"sunset\":1560396563},\"timezone\":-25200,\"id\":420006353,\"name\":\"Mountain View\",\"cod\":200}";
        Weather parsedWeatherBasic = OpenWeatherMapJsonParser.convertJsonToWeather(jsonWeatherBasic);

        Assert.assertEquals("City ID doesn't match", 420006353, parsedWeatherBasic.getCityId());
        Assert.assertEquals("City name doesn't match", "Mountain View", parsedWeatherBasic.getCity());
        Assert.assertEquals("Country doesn't match", "US", parsedWeatherBasic.getCountry());

        Assert.assertEquals("Date doesn't match", new Date(1560350645000L), parsedWeatherBasic.getDate());

        Assert.assertEquals("Longitude doesn't match", -122.08, parsedWeatherBasic.getLon(), 0.0);
        Assert.assertEquals("Latitude doesn't match", 37.39, parsedWeatherBasic.getLat(), 0.0);

        Assert.assertEquals("Weather ID doesn't match", 800, parsedWeatherBasic.getWeatherId());
        Assert.assertEquals("Weather description doesn't match", "Clear sky", parsedWeatherBasic.getDescription());
        Assert.assertEquals("Temperature doesn't match", 282.55, parsedWeatherBasic.getTemperature(), 0.0);
        Assert.assertEquals("Pressure doesn't match", 1023, parsedWeatherBasic.getPressure(), 0.0);
        Assert.assertEquals("Humidity doesn't match", 100, parsedWeatherBasic.getHumidity(), 0.0);

        Assert.assertEquals("Wind speed doesn't match", parsedWeatherBasic.getWind(), 1.5, 0.0);
        Assert.assertEquals("Wind direction doesn't match", 350, parsedWeatherBasic.getWindDirectionDegree(), 0.0);

        String jsonWeatherRain = "{\"coord\":{\"lon\":-157.8583,\"lat\":21.3069},\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10n\"}],\"base\":\"stations\",\"main\":{\"temp\":295.78,\"feels_like\":296.06,\"temp_min\":294.73,\"temp_max\":297.05,\"pressure\":1016,\"humidity\":75},\"visibility\":10000,\"wind\":{\"speed\":1.79,\"deg\":73,\"gust\":4.47},\"rain\":{\"1h\":0.76},\"clouds\":{\"all\":40},\"dt\":1639899427,\"sys\":{\"type\":2,\"id\":2010198,\"country\":\"US\",\"sunrise\":1639846995,\"sunset\":1639886002},\"timezone\":-36000,\"id\":5856195,\"name\":\"Honolulu\",\"cod\":200}";
        Weather parsedWeatherRain = OpenWeatherMapJsonParser.convertJsonToWeather(jsonWeatherRain);
        Assert.assertEquals("Rain doesn't match", 0.76, parsedWeatherRain.getRain(), 0.0);

        String jsonWeatherSnow = "{\"coord\":{\"lon\":37.6156,\"lat\":55.7522},\"weather\":[{\"id\":600,\"main\":\"Snow\",\"description\":\"light snow\",\"icon\":\"13d\"}],\"base\":\"stations\",\"main\":{\"temp\":271.32,\"feels_like\":267.4,\"temp_min\":270.52,\"temp_max\":271.69,\"pressure\":985,\"humidity\":97,\"sea_level\":985,\"grnd_level\":967},\"visibility\":556,\"wind\":{\"speed\":3.06,\"deg\":238,\"gust\":8.4},\"snow\":{\"1h\":0.13},\"clouds\":{\"all\":100},\"dt\":1639899167,\"sys\":{\"type\":2,\"id\":2000314,\"country\":\"RU\",\"sunrise\":1639893385,\"sunset\":1639918612},\"timezone\":10800,\"id\":524901,\"name\":\"Moscow\",\"cod\":200}";
        Weather parsedWeatherSnow = OpenWeatherMapJsonParser.convertJsonToWeather(jsonWeatherSnow);
        Assert.assertEquals("Snow doesn't match", 0.13, parsedWeatherSnow.getRain(), 0.0);
    }

    @Test
    public void testConvertJsonToUVIndex() throws JSONException {
        String jsonUVIndex = "{\"value\": 10.06}";

        double parsedUVIndex = OpenWeatherMapJsonParser.convertJsonToUVIndex(jsonUVIndex);

        Assert.assertEquals("UV-Index doesn't match", 10.06, parsedUVIndex, 0.0);
    }
}