/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package googleweathertest;

import sage.google.weather.GoogleWeather;
import sage.google.weather.WeatherUnderground;


/**
import java.net.*;
import java.io.*;
import java.util.Vector;
 *
 * @author Andrew Visscher
 */
public class GoogleWeatherTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
		boolean testWU = false;
		boolean testGW = false;
		boolean testOWM = true;

		if (testOWM){
			System.out.println( "OWM Step 1" );

		}
		
		
		// Test clock formatting?
/*
		if ( true )
		{
			java.text.SimpleDateFormat DFormat = new java.text.SimpleDateFormat("H:mm");
			String TestTimeText = "18:24";
			java.util.Date TestDate = new java.util.Date();

			try 
			{ 
				TestDate = DFormat.parse(TestTimeText);
				System.out.println( "TestDate = " + TestDate );
			} 
			catch (Exception e)
			{
				System.out.println( "Date parse error:" + e ); 
			}

			//String LocalPattern = java.text.SimpleDateFormat.toPattern( java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT) );
			//String LocalPattern = java.text.SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT).toPattern();
			
			java.text.DateFormat LocalTimeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
			
			System.out.println( "Local Time Pattern = " + LocalTimeFormat );


			//java.text.SimpleDateFormat DFormat12 = new java.text.SimpleDateFormat(LocalPattern);
			java.text.SimpleDateFormat DFormat12 = new java.text.SimpleDateFormat("h:mm a");
			try 
			{ 
				//String TimeText = DFormat12.format(TestDate);
				//String TimeText = LocalTimeFormat.format(TestDate);
				String TimeText = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(TestDate);
				System.out.println( "TimeText = " + TimeText );
			} 
			catch (Exception e)
			{
				System.out.println( "Time format error:" + e ); 
			}
		}
*/


		// Test Weather Underground weather?
		if ( testWU )
		{
	        System.out.println( "WU Step 1" );
	                
	        WeatherUnderground WeatherU;
	        System.out.println( "WU Step 2" );

			WeatherU = WeatherUnderground.getInstance();
	        System.out.println( "Got Instance" );

			java.util.Vector locList;

			locList = WeatherU.searchLocations( "New Brunswick" );
	        System.out.println( "Search for New Brunswick resulted in " + locList );
	        if (locList != null) System.out.println( "Search for New Brunswick found " + locList.size() + " locations." );

			locList = WeatherU.searchLocations( "Piscataway" );
	        System.out.println( "Search for Piscataway resulted in " + locList );
	        if (locList != null) System.out.println( "Search for Piscataway found " + locList.size() + " locations." );

/*
			locList = WeatherU.searchLocations( "08854" );
	        System.out.println( "Search for piscat resulted in " + locList );
	        if (locList != null) System.out.println( "Search for piscat found " + locList.size() + " locations." );

			locList = WeatherU.searchLocations( "piscat" );
	        System.out.println( "Search for piscat resulted in " + locList );
	        if (locList != null) System.out.println( "Search for piscat found " + locList.size() + " locations." );

			locList = WeatherU.searchLocations( "piscataway" );
	        System.out.println( "Search for piscat resulted in " + locList );
	        if (locList != null) System.out.println( "Search for piscat found " + locList.size() + " locations." );

			locList = WeatherU.searchLocations( "paris", "FR" );
	        System.out.println( "Search for piscat resulted in " + locList );
	        if (locList != null) System.out.println( "Search for piscat found " + locList.size() + " locations." );
*/


			WeatherU.removeWeatherLoc();	// Added here just to clear the cache.
			WeatherU.updateNow();
			WeatherU.getRadarURL( 25, 850, 480, 0 );
			WeatherU.getMoonImageURL();


			java.util.Vector AltLocList = WeatherU.getAlternateWeatherLocales();	// Get All alternate weather locations.
			System.out.println( "Alt weather locales: " + AltLocList );
	        
	        if ( AltLocList.size() > 1 )
			{
				String LocName = (String) AltLocList.get(1); 
	        	WeatherUnderground WeatherUAlt = WeatherUnderground.getInstance( LocName );
				WeatherUAlt.removeLocale();
			}
	
		}
		

		// Test Google Weather and NWS weather?
		if ( testGW )
		{
	        System.out.println( "Step 1" );
	                
	        GoogleWeather Weather;
	        System.out.println( "Step 2" );

			Weather = GoogleWeather.getInstance();
	        System.out.println( "Got Instance" );

			GoogleWeather Weather1 = GoogleWeather.getInstance();


			java.util.Vector allWeatherLocList = GoogleWeather.getAllWeatherLocations();

			java.util.Vector altWeatherLocList = GoogleWeather.getAlternateWeatherLocations();

			if ( altWeatherLocList.size() > 0 )
			{
				String altLoc = (String) altWeatherLocList.get(0);
	        	System.out.println( "Testing alt location: " + altLoc );

				GoogleWeather Weather2 = GoogleWeather.getInstance( altLoc ); 

				Weather2.setNWSZipCode( "75670" );

	        	System.out.println( "alt location uses zip code: " + Weather2.getNWSZipCode() );

				Weather2.updateAllNow();
				
				GoogleWeather Weather2a = GoogleWeather.getInstance( altLoc ); 
			}

			if ( altWeatherLocList.size() > 1 )
			{
				String altLoc = (String) altWeatherLocList.get(1);
	        	System.out.println( "Removing alt location: " + altLoc );

				GoogleWeather Weather3 = GoogleWeather.getInstance( altLoc );
				
				boolean delResult = Weather3.removeLocation(); 
	        	System.out.println( "Removed? " + delResult );

			}
		}
/*
		//Weather.setUnits("m");
		Weather.setUnits("s");



        System.out.println( "Updating Google weather" );
		String CurGWLoc = Weather.getGoogleWeatherLoc();
		if (CurGWLoc == null || CurGWLoc.length() == 0) 
			Weather.setGoogleWeatherLoc( "08854" );
        Weather.setGoogleWeatherLoc( "paris" );
        Weather.setGoogleWeatherLoc( "lyon" );
        
		Weather.updateGoogleNow();

        System.out.println( "Google weather loc: " + Weather.getGoogleWeatherLoc());
        System.out.println( "Google weather city: " + Weather.getGWCityName());


        System.out.println( "Updating NWS weather" );
		String CurNWSLoc = Weather.getNWSZipCode();
		if (CurNWSLoc == null || CurNWSLoc.length() == 0) 
			Weather.setNWSZipCode( "08854" );
		Weather.updateNWSNow();
*/
                

    }
}
