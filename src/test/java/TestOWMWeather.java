import java.io.IOException;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import sageweather.*;

/**
 * Created by jusjoken on 7/13/2021.
 */
public class TestOWMWeather {

    @BeforeClass
    public static void init() throws IOException {
        utils.init();
    }

    @Test
    public void testOWMWeather() {

        String propFileSuffix = "testing";
        //set testing to true to avoid failure due to no userkey
        utils.setTesting(true);
        OWM w = new OWM(propFileSuffix);
        //remove all locations so we have a clean test set
        w.RemoveAllLocations();

        //build a list of sageweather locations to test with
        w.AddLocation(new geoLocation("Rockford, MN", "US", 45.0882, -93.7237, IWeatherLocation.Units.Standard));
        w.AddLocation(new geoLocation("Strathmore, AB", "CA", 51.0501, -113.3852, IWeatherLocation.Units.Metric));
        w.AddLocation(new geoLocation("Greater Sudbury, ON", "CA", 47.6526, -82.4753, IWeatherLocation.Units.Metric));
        w.AddLocation(new geoLocation("London, GB", "GB", 51.5085, -0.1257, IWeatherLocation.Units.Metric));
        w.AddLocation(new geoLocation("Wellington, NZ", "NZ", -41.2866, 174.7756, IWeatherLocation.Units.Metric));
        w.AddLocation(new geoLocation("Grytviken, GS", "GS", -54.2811, -36.5092, IWeatherLocation.Units.Metric));
        w.AddLocation(new geoLocation("Madison Heights, US", "US", 42.5016, -83.1027, IWeatherLocation.Units.Standard));
        w.AddLocation(new geoLocation("River Park Estates, US", "US", 42.87, -88.32, IWeatherLocation.Units.Standard));
        w.AddLocation(new geoLocation("Schaumburg, US", "US", 42.0334, -88.0834, IWeatherLocation.Units.Standard));

        Boolean updated;
        //w.Update();

        //run tests for all stored locations
        List<geoLocation> tLocations = w.GetWeatherLocations().sortedList();
        for (geoLocation loc:tLocations) {
            String locID = loc.getID();

            System.out.println("OWM location: " + w.GetLocationName(locID) + " locationID:" + locID);

            if (w.HasError(locID)) {
                fail("Failed to set location: " + w.GetError(locID));
            }

            updated = w.Update(locID);
            System.out.println("OWM location: " + w.GetLocation(locID) + " Name '" + w.GetLocationName(locID) + "'");
            if (w.HasError(locID)) {
                fail("Failed to update sageweather: " + w.GetError(locID));
            }
            assertEquals("Failed to update sageweather", true, updated);

            System.out.println("OWM test: 2nd update should not be allowed as not enough time passed");
            updated = w.Update(locID);
            if (w.HasError(locID)) {
                fail("Failed to update sageweather: " + w.GetError(locID));
            }
            assertEquals("Weather updated, but should not have", false, updated);

            assertEquals(loc.getName(), w.GetLocationName(locID));

            testWeather(w, locID);

        }

        //retest Default location with changed units
        String defaultID = w.GetDefaultLocation().getID();
        System.out.println("***OWM test: defaultID = " + defaultID);

        IWeatherLocation.Units newUnits;
        System.out.println("OWM test: testing update - same location with units changed: " + defaultID + " : " + w.GetLocationName(defaultID));
        //System.out.println("OWM test: for '" + w.GetLocationName(defaultID) + "' Old units: " + w.GetUnits(defaultID));
        System.out.println("OWM test: for '" + w.GetLocationName() + "' Old units: " + w.GetUnits());
        if (w.GetUnits().equals(IWeatherLocation.Units.Metric)){
            newUnits = IWeatherLocation.Units.Standard;
        }else{
            newUnits = IWeatherLocation.Units.Metric;
        }
        System.out.println("OWM test: New units: " + newUnits.name());
        w.SetUnits(newUnits.name());
        System.out.println("OWM test: After update units: " + w.GetUnits());

        assertEquals("Failed to update units", true, w.GetUnits().equals(newUnits.name()));
        updated = w.Update(defaultID);

        if (w.HasError()) {
            fail("Failed to update sageweather on change of units: " + w.GetError());
        }
        assertEquals("Weather did not update after change of units", true, updated);

        testWeather(w, defaultID);

    }

    private void testWeather(OWM w, String locID) {
        IForecastPeriod curWeather = w.GetCurrentWeather(locID);
        System.out.println(w.GetLocationName(locID) + " - " + w.GetUnits(locID) + " - " + w.GetRecordedDate(locID) + " - " + curWeather);
        // assertEquals("Current forecast day not correct", "Now",
        // curWeather.getDay());
        assertEquals("Current forecast type not correct", IForecastPeriod.Type.Current, curWeather.getType());
        assertNotNull("Date was null", curWeather.getDate());
        assertNotNull("No Type", curWeather.getType());
        assertEquals(curWeather.getType(), IForecastPeriod.Type.Current);
        assertNotNull("Temp was null", curWeather.getTemp());
        assertNotNull("Code was null", curWeather.getCode());
        assertNotNull("Condition was null", curWeather.getCondition());

        // validate supported elements
        SupportedTest("curWeather.getTemp", curWeather.getTemp(), true);
        SupportedTest("curWeather.getCode", curWeather.getCode(), true);
        SupportedTest("curWeather.getCondition", curWeather.getCondition(), true);
        SupportedTest("curWeather.getDescription", curWeather.getDescription(), true);
        SupportedTest("curWeather.getHumid", curWeather.getHumid(), true);
        SupportedTest("curWeather.getPrecip", curWeather.getPrecip(), true);
        SupportedTest("curWeather.getPrecipAccumulation", curWeather.getPrecipAccumulation(), true);
        SupportedTest("curWeather.getPrecipType", curWeather.getPrecipType().toString(), true);
        SupportedTest("curWeather.getCloudCover", curWeather.getCloudCover(), true);
        SupportedTest("curWeather.getWindDir", curWeather.getWindDir(), true);
        SupportedTest("curWeather.getWindDirText", curWeather.getWindDirText(), true);
        SupportedTest("curWeather.getWindSpeed", curWeather.getWindSpeed(), true);
        SupportedTest("curWeather.getDewPoint", curWeather.getDewPoint(), true);
        SupportedTest("curWeather.getFeelsLike", curWeather.getFeelsLike(), true);
        SupportedTest("curWeather.getPressure", curWeather.getPressure(), true);
        SupportedTest("curWeather.getPressureDir", curWeather.getPressureDir(), false);
        SupportedTest("curWeather.getMoonPhase", curWeather.getMoonPhase(), true);
        SupportedTest("curWeather.getSunrise", curWeather.getSunrise(), true);
        SupportedTest("curWeather.getSunset", curWeather.getSunset(), true);
        SupportedTest("curWeather.getUVIndex", curWeather.getUVIndex(), true);
        SupportedTest("curWeather.getVisibility", curWeather.getVisibility(), true);

        List<ILongRangeForecast> days = w.GetForecasts(locID);
        assertTrue("No forecast data", days.size() > 0);
        int daynum = 0;
        for (ILongRangeForecast wd : days) {
            IForecastPeriod day = wd.getForecastPeriodDay();
            System.out.println("  Day " + daynum + " '" + day);
            IForecastPeriod night = wd.getForecastPeriodNight();
            System.out.println("Night " + daynum + " '" + night);
            if (daynum == 0) {
                assertNotNull("Night Period for first day was null", night);
                assertEquals("Forecast type for '" + daynum + "' not correct", IForecastPeriod.Type.Night, night.getType());
            }
            if (daynum == 1) {
                assertNotNull("Day Period for second day was null", day);
                assertEquals("Forecast type for '" + daynum + "' not correct", IForecastPeriod.Type.Day, day.getType());
            }
            if (day != null) {
                assertNotNull("No Date", day.getDate());
                assertNotNull("No Type", day.getType());
                assertEquals(day.getType(), IForecastPeriod.Type.Day);
                assertNotNull("No Temp", day.getTemp());
                assertNotNull("No Code", day.getCode());
                assertNotNull("No Condition", day.getCondition());
                // validate supported elements
                SupportedTest("day.getTemp", day.getTemp(), true);
                SupportedTest("day.getCode", day.getCode(), true);
                SupportedTest("day.getCondition", day.getCondition(), true);
                SupportedTest("day.getDescription", day.getDescription(), true);
                SupportedTest("day.getHumid", day.getHumid(), true);
                SupportedTest("day.getPrecip", day.getPrecip(), true);
                SupportedTest("day.getPrecipAccumulation", day.getPrecipAccumulation(), true);
                SupportedTest("day.getPrecipType", day.getPrecipType().toString(), true);
                SupportedTest("day.getCloudCover", day.getCloudCover(), true);
                SupportedTest("day.getWindDir", day.getWindDir(), true);
                SupportedTest("day.getWindDirText", day.getWindDirText(), true);
                SupportedTest("day.getWindSpeed", day.getWindSpeed(), true);
                SupportedTest("day.getDewPoint", day.getDewPoint(), true);
                SupportedTest("day.getFeelsLike", day.getFeelsLike(), true);
                SupportedTest("day.getPressure", day.getPressure(), true);
                SupportedTest("day.getPressureDir", day.getPressureDir(), false);
                SupportedTest("day.getMoonPhase", day.getMoonPhase(), true);
                SupportedTest("day.getSunrise", day.getSunrise(), true);
                SupportedTest("day.getSunset", day.getSunset(), true);
                SupportedTest("day.getUVIndex", day.getUVIndex(), true);
                SupportedTest("day.getVisibility", day.getVisibility(), false);
            }

            if (night != null) {
                assertNotNull("No Date", night.getDate());
                assertNotNull("No Type", night.getType());
                assertEquals(night.getType(), IForecastPeriod.Type.Night);
                assertNotNull("No Temp", night.getTemp());
                assertNotNull("No Code", night.getCode());
                assertNotNull("No Condition", night.getCondition());
                // validate supported elements
                SupportedTest("night.getTemp", night.getTemp(), true);
                SupportedTest("night.getCode", night.getCode(), true);
                SupportedTest("night.getCondition", night.getCondition(), true);
                SupportedTest("night.getDescription", night.getDescription(), true);
                SupportedTest("night.getHumid", night.getHumid(), true);
                SupportedTest("night.getPrecip", night.getPrecip(), true);
                SupportedTest("night.getPrecipAccumulation", night.getPrecipAccumulation(), true);
                SupportedTest("night.getPrecipType", night.getPrecipType().toString(), true);
                SupportedTest("night.getCloudCover", night.getCloudCover(), true);
                SupportedTest("night.getWindDir", night.getWindDir(), true);
                SupportedTest("night.getWindDirText", night.getWindDirText(), true);
                SupportedTest("night.getWindSpeed", night.getWindSpeed(), true);
                SupportedTest("night.getDewPoint", night.getDewPoint(), true);
                SupportedTest("night.getFeelsLike", night.getFeelsLike(), true);
                SupportedTest("night.getPressure", night.getPressure(), true);
                SupportedTest("night.getPressureDir", night.getPressureDir(), false);
                SupportedTest("night.getMoonPhase", night.getMoonPhase(), true);
                SupportedTest("night.getSunrise", night.getSunrise(), true);
                SupportedTest("night.getSunset", night.getSunset(), true);
                SupportedTest("night.getUVIndex", night.getUVIndex(), true);
                SupportedTest("night.getVisibility", night.getVisibility(), false);

            }
            daynum++;
        }

    }

    private void SupportedTest(String testElement, int valueToTest, boolean expectedResult) {
        assertEquals("Supported not set correctly for '" + testElement + "'", expectedResult,
                valueToTest != IForecastPeriod.iNotSupported);
    }

    private void SupportedTest(String testElement, String valueToTest, boolean expectedResult) {
        if (valueToTest!=null){
            assertEquals("Supported not set correctly for '" + testElement + "'", expectedResult,
                    !valueToTest.equals(IForecastPeriod.sNotSupported));
        }
    }


}
