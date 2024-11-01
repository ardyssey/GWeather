/**
 * Created by jusjoken on 7/18/2021.
 */

import org.junit.BeforeClass;
import org.junit.Test;
import sageweather.*;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestgeoLocationSearch {
    private boolean testCountries = false;

    @BeforeClass
    public static void init() throws IOException {
        //utils.init();
    }

    @Test
    public void testgeoLocationSearch() {

        String propFileSuffix = "testing";
        utils.setTesting(true);
        OWM tWeather = new OWM(propFileSuffix);

        WeatherLocations testLocations;
        //testLocations = tWeather.Search("London", geoLocationSearch.SearchType.Name, IWeatherLocation.Units.Metric );
        testLocations = tWeather.Search("48071", geoLocation.Country.UNITED_STATES, geoLocationSearch.SearchType.Zip, IWeatherLocation.Units.Standard );

        assertTrue("No search results found but expected",testLocations.hasLocations());

        List<geoLocation> tLocations = testLocations.sortedList();
        for (geoLocation loc:tLocations) {
            System.out.println("Search location: " + loc.getFullName());
            System.out.println("Search location: " + loc.getFullNameWithTemp());
        }

        //add the results to the tWeather so they get written to a properties file
        for (geoLocation loc:tLocations) {
            tWeather.AddLocation(loc);
        }

        //verify locations were added
        System.out.println("Verification location DefaultLocation: " + tWeather.GetDefaultID());
        for (geoLocation loc:tWeather.GetWeatherLocations().sortedList()) {
            System.out.println("Verification location: " + loc);
        }

        if (testCountries){
            //test the country list output to be used in selection lists
            for (geoLocation.Country item:tWeather.GetCountryList()){
                System.out.println("Country: " + tWeather.GetCountryFullName(item));
            }
        }

    }


}
