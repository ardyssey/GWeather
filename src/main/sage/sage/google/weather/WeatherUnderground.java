// Program: WeatherUnderground
// Author: Andrew Visscher, based on WeatherUnderground.java by Jeff Kardatzke
// Last Modification Date: July 1, 2015
//
// Description: This application is intended to be used in a sageweather
// module for SageTV custom stv files.  Currently the program is
// custom made to read the xml from Weather Underground data 
// and return the data to a text properties file.

/*
 *	Version History:
 *
 *	2.0.0
 *		- Initial inclusion of W.U. data in the GoogleWeather plugin.
 *	2.0.1	Oct 16, 2012
 *		- Changed default icon set to 'c' instead of 'k'.
 *		- Clear data for each section when WU data is updated & section is encountered.
 *		- Check for Not Available data and substitute with appropriate N/A value.
 *		- Added call to retrieve text used to indicate Not Available.
 *		- Added call to get a unique ID (hash code) for the current set of alerts. If the ID changes, then the alerts changed.
 *		- Added local sageweather stations to search results list when available.
 *	2.0.2	Oct 16, 2012
 *		- Updated list of possible condition icons.
 *	2.0.3	July 1, 2015
 *		- Updated radar image parameters to include background map image behind radar image.
 *
 */

/*
 * Usage of version updated for SageTV integration in the Studio:
 *
 *	The version of this class can be obtained from:
 * sage_google_weather_WeatherUnderground_getWeatherVersion()
 *
 *	The text used to indicate data is Not Available can be obtained from:
 * sage_google_weather_WeatherUnderground_getNotAvailableText()
 *
 *
 *  First get an instance of this class using:
 * Weather = sage_google_weather_WeatherUnderground_getInstance()
 *
 *	Or, to get an instance of this class for an alternate sageweather locale:
 *	Note: the AltLocName string must contain valid filename characters only.
 * Weather = sage_google_weather_WeatherUnderground_getInstance("AltLocName")
 *
 *
 *	To get a list of alternate sageweather locales, including the primary "" locale:
 * sage_google_weather_WeatherUnderground_getAllWeatherLocales() returns a Vector
 *
 *	To get a list of alternate sageweather locales, not including the "" locale:
 * sage_google_weather_WeatherUnderground_getAlternateWeatherLocales() returns a Vector
 *
 *  To remove an alternate locale by removing its sageweather cache properties file:
 * sage_google_weather_WeatherUnderground_removeLocale(Weather) returns boolean
 *
 *
 *	Searching for a locale's location:
 *		Params:
 *			Weather: The sageweather class instance.
 *			query: The name of the location to search for.
 *			lang: The 2-letter code for the language to use in the search results.
 *		Returns:
 *			a Vector of location Maps
 * sage_google_weather_WeatherUnderground_ java.util.Vector searchLocations( Weather, query) returns a Vector
 * sage_google_weather_WeatherUnderground_ java.util.Vector searchLocations( Weather, query, lang) returns a Vector
 *
 *	To get information about a location Map:
 *		Params:
 *			Weather: The sageweather class instance.
 *			LocationMap: The Map of the location info.
 * sage_google_weather_WeatherUnderground_getLocationCity( Weather, LocationMap ) returns String
 * sage_google_weather_WeatherUnderground_getLocationState( Weather, LocationMap ) returns String
 * sage_google_weather_WeatherUnderground_getLocationCountry( Weather, LocationMap ) returns String
 * sage_google_weather_WeatherUnderground_getLocationNeighborhood( Weather, LocationMap ) returns String
 * sage_google_weather_WeatherUnderground_getLocationCode( Weather, LocationMap ) returns String
 *
 *	To set the location for a locale's Weather instance:
 * sage_google_weather_WeatherUnderground_setWeatherLoc( Weather, LocationMap ) Returns nothing 
 *
 *	To get a locale's Weather instance's current LocationMap setting: 
 * sage_google_weather_WeatherUnderground_getWeatherLoc( Weather ) returns a Map
 *
 *	To clear a locale's Weather instance's current location, use:  
 * sage_google_weather_WeatherUnderground_removeWeatherLoc( Weather ) returns nothing
 *
 *
 *  To get & set the current units setting:
 *		Params:
 *			Weather: The sageweather class instance.
 *			Units: The units string, (must be "s" or "m", for standard/english or metric, respectively; default: "s") 
 * sage_google_weather_WeatherUnderground_setUnits(Weather, Units ) returns nothing
 * sage_google_weather_WeatherUnderground_getUnits(Weather ) returns String
 *
 *
 *  To handle updating the sageweather data (Data is cached and updates only actually occure after time limits expire):
 *		Params:
 *			Weather: The sageweather class instance.
 *			lang: The 2-letter code for the language to use in the search results.
 * sage_google_weather_WeatherUnderground_updateNow(Weather) returns boolean
 * sage_google_weather_WeatherUnderground_updateNow( Weather, lang ) returns boolean
 * sage_google_weather_WeatherUnderground_isCurrentlyUpdating(Weather) returns boolean
 * sage_google_weather_WeatherUnderground_getLastUpdateTime(Weather) returns long
 *
 *
 *	To get information about sageweather condition icons:
 *		Retrieve a list of all icon condition names:
 * sage_google_weather_WeatherUnderground_getAllWeatherIconConditions() returns Vector
 *		Retrieve a reduced list of icon condition names:
 * sage_google_weather_WeatherUnderground_getReplacementWeatherIconConditions() returns Vector
 *		Get the reduced icon style replacement icon name for a single icon condition:  
 * sage_google_weather_WeatherUnderground_getIconReplacementString(IconName) returns String
 *
 *	Support for multiple sets of condition icons: 
 *		Params:
 *			Weather: The sageweather class instance.
 *			IconSet: The icon set to use; must be one from the list returned by getAllWeatherIconSets()  
 * sage_google_weather_WeatherUnderground_getAllWeatherIconSets(Weather) returns Vector
 * sage_google_weather_WeatherUnderground_getIconSet(Weather) returns String
 * sage_google_weather_WeatherUnderground_setIconSet( Weather, IconSet ) returns nothing
 *
 *	To get icon URLs for sageweather conditions:
 *		Params:
 *			Weather: The sageweather class instance.
 *			IconName: The icon condition name. 
 *			IconSet: The icon set to use; must be one from the list returned by getAllWeatherIconSets()  
 *
 *		Get an icon URL for the given icon condition name:
 * sage_google_weather_WeatherUnderground_getIconURL( Weather, IconName ) returns String
 * sage_google_weather_WeatherUnderground_getIconURL( Weather, IconName, IconSet ) returns String
 *
 *		Get an icon URL for reduced set replacement name for an icon condition name:
 * sage_google_weather_WeatherUnderground_getIconReplacementURL( Weather, IconName ) returns String
 * sage_google_weather_WeatherUnderground_getIconReplacementURL( Weather, IconName, IconSet ) returns String
 *
 *
 *	Misc info calls:
 *		Params:
 *			Weather: The sageweather class instance.
 *			Units: The units string, (must be "s" or "m", for standard/english or metric, respectively; default: "s") 
 * sage_google_weather_WeatherUnderground_getLastError(Weather) returns String
 *
 * sage_google_weather_WeatherUnderground_getTempSymbol(Weather) returns String
 * sage_google_weather_WeatherUnderground_getTempUnit(Weather) returns String
 * sage_google_weather_WeatherUnderground_getTempUnit( Weather, Units ) returns String
 *
 * sage_google_weather_WeatherUnderground_getDistUnit(Weather) returns String
 * sage_google_weather_WeatherUnderground_getDistUnit( Weather, Units ) returns String
 *
 * sage_google_weather_WeatherUnderground_getSpeedUnit(Weather) returns String
 * sage_google_weather_WeatherUnderground_getSpeedUnit( Weather, Units ) returns String
 *
 * sage_google_weather_WeatherUnderground_getPressureUnit(Weather) returns String
 * sage_google_weather_WeatherUnderground_getPressureUnit( Weather, Units ) returns String
 *
 * sage_google_weather_WeatherUnderground_getRainUnit(Weather) returns String
 * sage_google_weather_WeatherUnderground_getRainUnit( Weather, Units ) returns String
 *
 * sage_google_weather_WeatherUnderground_getSnowUnit(Weather) returns String
 * sage_google_weather_WeatherUnderground_getSnowUnit( Weather, Units ) returns String
 *
 *
 *	Once the sageweather data has been updated, retrieve data via these calls:
 *		Params:
 *			Weather: The sageweather class instance.
 *
 *		Get the location place names:
 * sage_google_weather_WeatherUnderground_getCityName(Weather) returns String
 * sage_google_weather_WeatherUnderground_getStateName(Weather) returns String
 * sage_google_weather_WeatherUnderground_getCountryName(Weather) returns String
 *
 *		Get current condition info for these property names:
 *			Description
 *			Dewpoint
 *			DisplayLocFull
 *			FeelsLike
 *			HeatIndex
 *			Humidity
 *			IconName
 *			ObservationLocCity
 *			ObservationLocCountry
 *			ObservationLocState
 *			ObservationTime
 *			Precip1Hr
 *			PrecipToday
 *			Pressure
 *			PressureTrend		returns: -, 0, or +
 *			Temp
 *			UV
 *			Visibility
 *			WindChill
 *			WindDir
 *			WindGust
 *			WindSpeed
 * sage_google_weather_WeatherUnderground_getCurrentCondition( Weather, propName ) returns String
 * sage_google_weather_WeatherUnderground_getCurConditionProperties(Weather) returns Map
 *
 *		Get 12 hour forecast for period number for these property names:
 *		Note: Days have even period numbers; nights have odd period numbers
 *			ChancePrecip
 *			FCText
 *			IconName
 *			PeriodName
 * sage_google_weather_WeatherUnderground_get12hrForecastStartPeriod(Weather) returns int
 * sage_google_weather_WeatherUnderground_get12hrForecastEndPeriod(Weather) returns int
 * sage_google_weather_WeatherUnderground_get12hrForecast( Weather, periodNum, propName ) returns String
 * sage_google_weather_WeatherUnderground_get12hrForecastProperties( Weather, periodNum ) returns Map
 *
 *
 *		Get 24 hour forecast for period number for these property names:
 *			ChancePrecip
 *			Conditions
 *			DateDayNum
 *			DateMonthName
 *			DateMonthNum
 *			DateWeekDay
 *			DateWeekDayShort
 *			DateYear
 *			High
 *			HumidAvg
 *			HumidMax
 *			HumidMin
 *			IconName
 *			Low
 *			RainAllDay
 *			RainDay
 *			RainNight
 *			SnowAllDay
 *			SnowDay
 *			SnowNight
 *			WindAvgDir
 *			WindAvgSpeed
 *			WindMaxDir
 *			WindMaxSpeed
 * sage_google_weather_WeatherUnderground_get24hrForecastStartPeriod(Weather) returns int
 * sage_google_weather_WeatherUnderground_get24hrForecastEndPeriod(Weather) returns int
 * sage_google_weather_WeatherUnderground_get24hrForecast( Weather, periodNum, propName) returns String
 * sage_google_weather_WeatherUnderground_get24hrForecastProperties( Weather, periodNum) returns Map
 *
 *
 *		Get sageweather alert information properties for an alert number:
 *			StartTime
 *			ExpireTime
 *			Title1
 *			Title2
 *			Message1
 *			Message2
 *			Message3
 * sage_google_weather_WeatherUnderground_getAlertCount(Weather) returns int
 * sage_google_weather_WeatherUnderground_getAlertUniqueID(Weather) returns int
 * sage_google_weather_WeatherUnderground_getAlertInfo( Weather, AlertNum, propName) returns String
 * sage_google_weather_WeatherUnderground_getAlertInfoProperties( Weather, AlertNum) returns Map
 *
 * 		Get Astronomy information for these property names:
 *			SunriseHour
 *			SunriseMinute
 *			SunsetHour
 *			SunsetMinute
 *			MoonPercentIlluminated
 *			MoonAge
 * sage_google_weather_WeatherUnderground_getAstronomyInfo( Weather, propName) returns String
 * sage_google_weather_WeatherUnderground_getAstronomyInfoProperties( Weather, AlertNum) returns Map
 *
 *		Get the moon image URL for the current moon age:
 * sage_google_weather_WeatherUnderground_getMoonImageURL(Weather) returns String
 *		Get the moon image URL for a specified moon age, in number of days:
 * sage_google_weather_WeatherUnderground_getMoonImageURL( Weather, MoonAge) returns String
 *
 *
 *		Get radar image URLs:
 * sage_google_weather_WeatherUnderground_getRadarNewestFrameNum(Weather) returns int
 * sage_google_weather_WeatherUnderground_getRadarOldestFrameNum(Weather) returns int
 *
 *		Get a radar image URL, using the following parameters:
 *			Params
 *				Weather 	The sageweather class instance.
 *				radius		The radius from the location, in miles.
 *				width		Width of image, in pixels
 *				height		Height of image, in pixels
 *				frameNum	The frame number, from getRadarOldestFrameNum() to getRadarNewestFrameNum() (5 - 0) 
 * sage_google_weather_WeatherUnderground_getRadarURL( Weather, radius, width, height, frameNum ) returns String
 *
 */


package sage.google.weather;

import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import sageweather.utils;


public class WeatherUnderground
{

    // base URL for data retrieval (includes Google license key)
	private static final String NA = "NA";

	private static final String weather_key = "f577e42a17aabb02";
    
    private static final String weather_url_scheme = "http";
    private static final String weather_url_base_address = "api.wunderground.com";

	private static final String default_location_code = "autoip";

    private static final String weather_update_url = "/api/" + weather_key + "/conditions/forecast10day/astronomy/alerts";
    private static final String weather_search_url = "/api/" + weather_key + "/geolookup";
    private static final String weather_lang_prefix = "/lang:";
    private static final String weather_query_prefix = "/q/";
    private static final String weather_feedtype_suffix = ".xml";

    private static final String weather_result_NA = "N/A";
    private static final String weather_icon_NA = "unknown";

	// Radar images URL etc.
	private static final int weather_newest_frame_num = 0; 
	private static final int weather_oldest_frame_num = 5; 
	private static final String weather_radar_url = "/api/" + weather_key + "/radar";
    private static final String weather_radar_url_suffix = ".gif";
    //private static final String weather_radar_standard_options = "newmaps=1&smooth=1&rainsnow=1&timelabel=1&timelabel.x=5&timelabel.y=25";
    private static final String weather_radar_standard_options = "timelabel=1&timelabel.y=25&newmaps=1&rainsnow=1";
    private static final String weather_radar_radius_prefix = "&radius=";
    private static final String weather_radar_width_prefix = "&width=";
    private static final String weather_radar_height_prefix = "&height=";
    private static final String weather_radar_frame_prefix = "&frame=";
	private static final int radar_default_radius = 50;
	private static final int radar_default_width  = 640;
	private static final int radar_default_height = 480;
	private static final int radar_default_frame  = 0;

	// Moon state images URL.
    private static final String weather_moon_url_base_address = "www.wunderground.com";
	private static final String weather_moon_url = "/graphics/moonpictsnew/moon";
    private static final String weather_moon_url_suffix = ".gif";


	// Icom URL
    private static final String weather_icon_base_address = "icons.wxug.com";
	private static final String weather_icon_url = "/i/c/";
	private static final String weather_icon_url_suffix = ".gif";

	private static final java.util.Map iconReplacementMap = new java.util.HashMap();

	private static final String propFilePrefix = "weather_underground_cache";

	private static java.util.HashMap myInstanceMap = new java.util.HashMap();


	private String myLocale;

	private java.util.Map myWULoc;
	private long lastUpdateTime;
	private SortedWeatherProperties props = new SortedWeatherProperties();


	private static final String UnitNameEnglish = "s";
	private static final String UnitNameMetric = "m";
	private static final String UnitNameDefault = UnitNameEnglish;
	private String myUnits = UnitNameDefault;

	private boolean updating = false;

	private String lastError;



	public static WeatherUnderground getInstance()
	{
		return getInstance("");
	}
	public static WeatherUnderground getInstance( String locale )
	{
		// First, make sure the string contains valid filename chars.

		WeatherUnderground instance = null;

		if ( myInstanceMap.containsKey(locale) )
		{
			instance = (WeatherUnderground) myInstanceMap.get(locale);
			System.out.println( "For location '" + locale + "', found instance= " + instance );
		}

		if (instance == null)
		{
			instance = new WeatherUnderground(locale);
			myInstanceMap.put( locale, instance );
		}
		System.out.println( "instance for '" + locale + "' = " + instance );
		return instance;
	}

	protected WeatherUnderground()
	{
		this("");
	}

	protected WeatherUnderground( String locale )
	{
		// If the icon replacement map hasn't yet been created, create it.
		if ( iconReplacementMap.size() == 0 )
			initIconReplacementData();

		myLocale = locale;
		loadWeatherDataFromCache();
	}

	public static String getWeatherVersion()
	{
		return utils.getVersion();
	}

	public static String getNotAvailableText()
	{
		return weather_result_NA;
	}


	/*
	 * Handle the icon condition replacement list that reuses icons
	 * for some conditions. 
	 */ 
	private static void initIconReplacementData()
	{
		if ( iconReplacementMap.size() == 0 )
		{
			iconReplacementMap.put("chanceflurries", "flurries");
			iconReplacementMap.put("nt_chanceflurries", "flurries");
			iconReplacementMap.put("chancerain", "rain");
			iconReplacementMap.put("nt_chancerain", "rain");
			iconReplacementMap.put("chancesleet", "sleet");
			iconReplacementMap.put("nt_chancesleet", "sleet");
			iconReplacementMap.put("chancesnow", "snow");
			iconReplacementMap.put("nt_chancesnow", "snow");
			iconReplacementMap.put("chancetstorms", "tstorms");
			iconReplacementMap.put("nt_chancetstorms", "tstorms");
			iconReplacementMap.put("clear", "sunny");
			iconReplacementMap.put("nt_cloudy", "cloudy");
			iconReplacementMap.put("nt_flurries", "flurries");
			iconReplacementMap.put("nt_fog", "fog");
			iconReplacementMap.put("hazy", "fog");
			iconReplacementMap.put("nt_hazy", "fog");
			iconReplacementMap.put("mostlycloudy", "cloudy");
			iconReplacementMap.put("nt_mostlycloudy", "cloudy");
			iconReplacementMap.put("mostlysunny", "sunny");
			iconReplacementMap.put("nt_mostlysunny", "nt_clear");
			iconReplacementMap.put("partlycloudy", "partlysunny");
			iconReplacementMap.put("nt_partlysunny", "nt_partlycloudy");
			iconReplacementMap.put("nt_sleet", "sleet");
			iconReplacementMap.put("nt_rain", "rain");
			iconReplacementMap.put("nt_snow", "snow");
			iconReplacementMap.put("nt_sunny", "nt_clear");
			iconReplacementMap.put("nt_tstorms", "tstorms");
			iconReplacementMap.put("unknown", "tstorms");
		}
	}

	public static String getIconReplacementString(String baseIcon)
	{
		String replaced = (String) iconReplacementMap.get(baseIcon);
		if (replaced != null)
			return replaced;
		else
			return baseIcon;
	}


	/*
	 * Define all possible icon conditions and add calls to report all conditions
	 * and just those that are used via a replacement list to reuse icons for
	 * some conditions. 
	 */ 
	private static final String[] AllIconConditionsList = 
					{ 
						"chanceflurries",			// 0
						"chancerain",				// 1
						"chancesleet",				// 2
						"chancesnow",				// 3
						"chancetstorms",			// 4
						"clear",					// 5
						"cloudy",					// 6
						"flurries",					// 7
						"fog",						// 8
						"hazy",						// 9
						"mostlycloudy",				// 10
						"mostlysunny",				// 11
						"partlycloudy",				// 12
						"partlysunny",				// 13
						"sleet",					// 14
                        "rain",                     // 15
						"snow",						// 16
						"sunny",					// 17
						"tstorms",					// 18
						"unknown",					// 19
						"nt_chanceflurries",		// 20
						"nt_chancerain",			// 21
						"nt_chancesleet",			// 22
						"nt_chancesnow",			// 23
						"nt_chancetstorms",			// 24
						"nt_clear",					// 25
						"nt_cloudy",				// 26
						"nt_flurries",				// 27
						"nt_fog",					// 28
						"nt_hazy",					// 29
						"nt_mostlycloudy",			// 30
						"nt_mostlysunny",			// 31
						"nt_partlycloudy",			// 32
						"nt_partlysunny",			// 33
						"nt_sleet",					// 34
                        "nt_rain",                  // 35
						"nt_snow",					// 36
						"nt_sunny",					// 37
						"nt_tstorms",				// 38
						"nt_unknown"				// 39
					};

	public static java.util.Vector getAllWeatherIconConditions()
	{
		java.util.Vector iconConditionsList = new java.util.Vector();

		// Just create a list of all possible icon conditions.
		for ( int i = 0; i < AllIconConditionsList.length; i++ )
		{
			String thisIconCondition = AllIconConditionsList[i];
			iconConditionsList.add( thisIconCondition );
		}

		System.out.println( "Found " + iconConditionsList.size() + " total sageweather icon conditions: " + iconConditionsList.toString() );

		return iconConditionsList;
	}

	public static java.util.Vector getReplacementWeatherIconConditions()
	{
		// If the icon replacement map hasn't yet been created, create it.
		if ( iconReplacementMap.size() == 0 )
			initIconReplacementData();

		java.util.Vector iconConditionsList = new java.util.Vector();

		// Create a list of all icon conditions, taking into consideration those that are replaced & reused for multiple condition strings.
		for ( int i = 0; i < AllIconConditionsList.length; i++ )
		{
			String thisIconCondition = AllIconConditionsList[i];
			String replacementIconCondition = getIconReplacementString( thisIconCondition );
		
			// Add this replacement condition to the list only if it isn't in the list already.
			if ( !iconConditionsList.contains( replacementIconCondition ) )
				iconConditionsList.add( replacementIconCondition );
		}

		System.out.println( "Found " + iconConditionsList.size() + " replacement sageweather icon conditions: " + iconConditionsList.toString() );

		return iconConditionsList;
	}


	/*
	 * These calls are used to handle using alternate sageweather locales instead of just 1 locale.
	 */  
	public static java.util.Vector getAllWeatherLocales()
	{
		java.util.Vector localeList = getAlternateWeatherLocales();
		java.util.Vector allLocList = new java.util.Vector();
		allLocList.add( "" );
		allLocList.addAll( localeList );

		System.out.println( "Found " + allLocList.size() + " total sageweather locales: " + allLocList.toString() );

		return allLocList;
	}

	public static java.util.Vector getAlternateWeatherLocales()
	{
		java.io.File workingDir = new java.io.File( System.getProperty("user.dir") );

		java.io.File[] propFiles = workingDir.listFiles(new java.io.FileFilter()
			{
				public boolean accept(java.io.File pathname)
				{
					return pathname.getName().toLowerCase().startsWith(propFilePrefix) &&
						   pathname.toString().toLowerCase().endsWith(".properties");
				}
			}
			);
		
		// Loop through list of sageweather cache properties files & create list of alternate locales.
		java.util.Vector locList = new java.util.Vector();
		int prefixSize = propFilePrefix.length();
		for ( int i = 0; i < propFiles.length; i++ )
		{
			String fileName = propFiles[i].getName();

			if (fileName.toLowerCase().startsWith(propFilePrefix))
			{
				int dotPos = fileName.indexOf( "." );
				String locName = fileName.substring( prefixSize, dotPos );
				
				// If there is a locale name, remove the starting '_'
				if (locName.length() > 1 ) 
					locName = locName.substring( 1 );
				
				// If there is still a locale name, add it to the list. This skips the main "" locale.
				if (locName.length() > 1 ) 
					locList.add( locName );
					
			}
		}

		System.out.println( "Found " + locList.size() + " alternate sageweather locales: " + locList.toString() );

		return locList;
	}

	// Remove this instance's locale by getting rid of its properties file.
	public boolean removeLocale()
	{
		boolean result = false;

		// Wait until nothing is currently updating.
		while ( isCurrentlyUpdating() )
		{
        	try { Thread.sleep(100l); } catch (InterruptedException e) {}
		}

		// No update in progress, so remove properties file.
		java.io.File cacheFile = new java.io.File(propFilePrefix + (myLocale.length() > 0 ? "_" + myLocale : "") + ".properties");
		System.out.println("Weather Underground trying to delete locale cache: " + cacheFile );
		try {
			result = cacheFile.delete();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error deleting sageweather cache file '" + cacheFile + "':" + e));
			return false;
		}

		// Remove myLocale from list of instances.
		myInstanceMap.remove(myLocale);

		return result;
	}


	public String getLastError()
	{
		return lastError;
	}


	// Handle searching for sageweather locations.
	// Searches return a Vector of locations. Locations are Maps containing
	// values for these keys:
	private static final String LOC_KEY_CITY 	= "city";
	private static final String LOC_KEY_STATE 	= "state";
	private static final String LOC_KEY_COUNTRY = "country";
	private static final String LOC_KEY_NBRHOOD = "neighborhood";
	private static final String LOC_KEY_LOCCODE = "loccode";

	// Returns a Vector of location item Maps
	public java.util.Vector searchLocations(String query)
	{
		return searchLocations(query, "");
	}
	public java.util.Vector searchLocations(String query, String lang)
	{
		try
		{

			String searchURLString = weather_search_url +
									 ( (lang != null && lang.length() > 0) ? weather_lang_prefix + lang : "" ) +
					    			 weather_query_prefix +
									 ( (query != null && query.length() > 0) ? query : default_location_code ) +  
									 weather_feedtype_suffix;
			//System.out.println( "Weather search URL = " + searchURLString );

			java.net.URI URIItem = new java.net.URI( weather_url_scheme,
			 										 weather_url_base_address,
			 										 searchURLString,
													 null,	//java.net.URLEncoder.encode("Test=Test Param", "UTF-8"),	// Only params, need encoding.	
													 null	
													);

			System.out.println( "Full Weather search URL = " + URIItem.toASCIIString() );


			SearchHandler searchy = new SearchHandler();
			parseXmlFile(URIItem.toASCIIString(), false, searchy);
			if (searchy.getSearchResults() == null)
				System.out.println(lastError = "Weather Underground returned empty doc");
			return searchy.getSearchResults();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with sageweather parsing:" + e));
			return null;
		}
	}

	// Get the city name for a location Map.
	public String getLocationCity( java.util.Map loc )
	{
		if ( loc == null ) return "";
		String cityname = (String) loc.get( LOC_KEY_CITY );
		return ( cityname == null ? "" : cityname );
	} 

	// Get the state name for a location Map.
	public String getLocationState( java.util.Map loc )
	{
		if ( loc == null ) return "";
		String statename = (String) loc.get( LOC_KEY_STATE );
		return ( statename == null ? "" : statename );
	} 

	// Get the country name for a location Map.
	public String getLocationCountry( java.util.Map loc )
	{
		if ( loc == null ) return "";
		String countryname = (String) loc.get( LOC_KEY_COUNTRY );
		return ( countryname == null ? "" : countryname );
	} 

	// Get the neighborhood name for a location Map.
	public String getLocationNeighborhood( java.util.Map loc )
	{
		if ( loc == null ) return "";
		String neighborhoodname = (String) loc.get( LOC_KEY_NBRHOOD );
		return ( neighborhoodname == null ? "" : neighborhoodname );
	} 
	
	// Get the location code name for a location Map.
	public String getLocationCode( java.util.Map loc )
	{
		if ( loc == null ) return "";
		String loccode = (String) loc.get( LOC_KEY_LOCCODE );
		return ( loccode == null ? "" : loccode );
	} 


	// Set, Get, Remove the sageweather location for the current locale.
	public void setWeatherLoc(java.util.Map newWULoc)
	{
		if (newWULoc != null && !newWULoc.equals(myWULoc))
		{
			// Blow away the Weather Underground data cache
			lastUpdateTime = 0;
		}

		props.remove("WU/" + LOC_KEY_CITY);
		props.remove("WU/" + LOC_KEY_STATE);
		props.remove("WU/" + LOC_KEY_COUNTRY);
		props.remove("WU/" + LOC_KEY_NBRHOOD);
		props.remove("WU/" + LOC_KEY_LOCCODE);
		myWULoc = newWULoc;

		saveWeatherDataToCache();
	}
	public java.util.Map getWeatherLoc()
	{
		return myWULoc;
	}
	public void removeWeatherLoc()
	{
		// Blow away the Weather Underground data cache
		lastUpdateTime = 0;

		// Clear the Weather Underground location.
		props.remove("WU/" + LOC_KEY_CITY);
		props.remove("WU/" + LOC_KEY_STATE);
		props.remove("WU/" + LOC_KEY_COUNTRY);
		props.remove("WU/" + LOC_KEY_NBRHOOD);
		props.remove("WU/" + LOC_KEY_LOCCODE);
		myWULoc = null;
		initCurrentWeatherLoc();

		saveWeatherDataToCache();
	}

	private void initCurrentWeatherLoc()
	{
		// Get current location settings.
		String CityName 		= NA;
		String StateName 		= NA;
		String CountryName  	= NA;
		String NeighborhoodName = NA;

		String LocationCode	= NA;

		//String CityName 		= props.getProperty("WU/" + LOC_KEY_CITY, 	 "" );
		//String StateName 		= props.getProperty("WU/" + LOC_KEY_STATE,	 "" );
		//String CountryName  	= props.getProperty("WU/" + LOC_KEY_COUNTRY, "" );
		//String NeighborhoodName = props.getProperty("WU/" + LOC_KEY_NBRHOOD, "" );

		//String LocationCode	= props.getProperty("WU/" + LOC_KEY_LOCCODE, default_location_code );

		myWULoc = new java.util.HashMap(); 
		myWULoc.put( LOC_KEY_CITY, CityName );
		myWULoc.put( LOC_KEY_STATE, StateName );
		myWULoc.put( LOC_KEY_COUNTRY, CountryName );
		myWULoc.put( LOC_KEY_NBRHOOD, NeighborhoodName );
		myWULoc.put( LOC_KEY_LOCCODE, LocationCode );
	}

	// Set the city name for the current locale
	private void setWeatherLocCity( String CityName )
	{
		// Make sure the current sageweather location data exists.
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
			initCurrentWeatherLoc();

		if ( CityName == null )
			CityName = "";

		props.setProperty("WU/" + LOC_KEY_CITY, CityName );
		myWULoc.put( LOC_KEY_CITY, CityName );
	} 

	// Set the state name for the current locale
	private void setWeatherLocState( String StateName )
	{
		// Make sure the current sageweather location data exists.
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
			initCurrentWeatherLoc();

		if ( StateName == null )
			StateName = "";

		props.setProperty("WU/" + LOC_KEY_STATE, StateName );
		myWULoc.put( LOC_KEY_STATE, StateName );
	} 

	// Set the country name for the current locale
	private void setWeatherLocCountry( String CountryName )
	{
		// Make sure the current sageweather location data exists.
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
			initCurrentWeatherLoc();

		if ( CountryName == null )
			CountryName = "";

		props.setProperty("WU/" + LOC_KEY_COUNTRY, CountryName );
		myWULoc.put( LOC_KEY_COUNTRY, CountryName );
	} 

	// Set the neighborhood name for the current locale
	private void setWeatherLocNeighborhood( String NeighborhoodName )
	{
		// Make sure the current sageweather location data exists.
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
			initCurrentWeatherLoc();

		if ( NeighborhoodName == null )
			NeighborhoodName = "";

		props.setProperty("WU/" + LOC_KEY_NBRHOOD, NeighborhoodName );
		myWULoc.put( LOC_KEY_NBRHOOD, NeighborhoodName );
	} 

	// Set the location code name for the current locale
	// This is public for the off-chance that the location code is known. 
	public void setWeatherLocCode( String LocationCode )
	{
		// Make sure the current sageweather location data exists.
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
			initCurrentWeatherLoc();

		if ( (LocationCode == null) || (LocationCode.length() == 0) )
			LocationCode = default_location_code;

		props.setProperty("WU/" + LOC_KEY_LOCCODE, LocationCode );
		myWULoc.put( LOC_KEY_LOCCODE, LocationCode );
		
		// Blow away the Weather Underground data cache
		lastUpdateTime = 0;
	} 



	// Set the units to either "s" (US/English) "m" (metric)
	public void setUnits(String units)
	{
		if ( (units != null) && (units.length() > 0) && (!units.equals(myUnits)) )
		{
			// New units setting must be UnitNameEnglish or UnitNameMetric.
			if ( (units.equals(UnitNameEnglish)) || (units.equals(UnitNameMetric)) )
			{
			myUnits = units;

			// Blow away the Weather Underground data cache
			lastUpdateTime = 0;
			}
		}

	}
	public String getUnits()
	{
		return myUnits;
	}
	 

	// Handle updating the sageweather data.
	public boolean isCurrentlyUpdating()
	{
		return updating;
	}

	public long getLastUpdateTime()
	{
		return lastUpdateTime;
	}

	public boolean updateNow()
	{
		return updateNow("");
	}
	public boolean updateNow(String lang)
	{
		//always return true to skip the failure as WUNDERGROUND is no longer available
		return true;
		/*
		if (updating) return true;

		
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
		{ 
			System.out.println( "   Weather has no location set yet." );
			saveWeatherDataToCache();
			return false;
		}

		try
		{
			updating = true;

			lastError = "";
			boolean updateWU = System.currentTimeMillis() - lastUpdateTime > 30*60*1000; // 30 minutes
			if (!updateWU)
			{
				System.out.println( "   Not time to update sageweather yet." );
				return true; // nothing to update right now, use the cache
			}
						
			String LocationCode = props.getProperty("WU/" + LOC_KEY_LOCCODE, default_location_code );;
			if (LocationCode.startsWith("/q/") )
				LocationCode = LocationCode.substring(3);

			String updateURLString = weather_update_url +
									 ( (lang != null && lang.length() > 0) ? weather_lang_prefix + lang : "" ) +
    								 weather_query_prefix +
									 LocationCode +  
									 weather_feedtype_suffix;
			//System.out.println( "Weather Update string URL = " + updateURLString );
			
			java.net.URI URIItem = new java.net.URI( weather_url_scheme,
			 										 weather_url_base_address,
			 										 updateURLString,
													 null,	//java.net.URLEncoder.encode("Test=Test Param", "UTF-8"),	// Only params, need encoding.	
													 null	
													);

			System.out.println( "Full Weather Update string URL = " + URIItem.toASCIIString() );


			parseXmlFile(URIItem.toASCIIString(), false, new WeatherUndergroundUpdateHandler() );

			saveWeatherDataToCache();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with sageweather parsing:" + e));
			e.printStackTrace();
			return false;
		}
		finally
		{
			updating = false;
		}
		return true;
		*/
	}
	
	// Handle icon sets.
	/*
	 * Define all possible icon conditions and add calls to report all conditions
	 * and just those that are used via a replacement list to reuse icons for
	 * some conditions. 
	 */ 
	private static final String[] AllIconSetsList = 
					{	"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k" };
	private static final String DefaultIconSet = "c"; 

	public static java.util.Vector getAllWeatherIconSets()
	{
		java.util.Vector iconSetsList = new java.util.Vector();

		// Just create a list of all possible icon sets.
		for ( int i = 0; i < AllIconSetsList.length; i++ )
		{
			String thisIconSet = AllIconSetsList[i];
			iconSetsList.add( thisIconSet );
		}

		System.out.println( "Found " + iconSetsList.size() + " total sageweather icon sets: " + iconSetsList.toString() );

		return iconSetsList;
	}

	public String getIconSet()
	{
		return props.getProperty("icon_set", DefaultIconSet);
	}

	public void setIconSet( String newSet )
	{
		String curSet = getIconSet();
		if ( (newSet != null) && (newSet.length() > 0) && (!newSet.equals(curSet)) )
		{
			props.setProperty("icon_set", newSet);
		}
	}

	// Get the URL for the requested IconName.
	public String getIconURL( String IconName )
	{
		return getIconURL( IconName, getIconSet() );
	}
	public String getIconURL( String IconName, String iconSet )
	{
		if ( (IconName == null) || (IconName.length() == 0) )
			return "";

		if ( (iconSet == null) || (iconSet.length() == 0) )
			iconSet = DefaultIconSet;


		String IconURL = weather_icon_url +
		 				 iconSet + "/" +
						 IconName + 
		 				 weather_icon_url_suffix;

		//System.out.println( "Weather icon URL: " + IconURL );
		
		try
		{
			java.net.URI URIItem = new java.net.URI( weather_url_scheme,
			 										 weather_icon_base_address,
			 										 IconURL,
													 null, //java.net.URLEncoder.encode("Test=Test Param", "UTF-8"),	// Only params, need encoding.	
													 null	
													);

			//System.out.println( "Full Weather icon URL = " + URIItem.toASCIIString() );
			return URIItem.toASCIIString();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with sageweather parsing:" + e));
			return null;
		}
	}

	// Get the URL for the replacement icon for the requested IconName.
	public String getIconReplacementURL( String IconName )
	{
		return getIconReplacementURL( IconName, getIconSet() );
	}
	public String getIconReplacementURL( String IconName, String iconSet )
	{
		if ( (IconName == null) || (IconName.length() == 0) )
			return "";

		return getIconURL( getIconReplacementString( IconName ), iconSet );
	}



	// Handle units.
	private static final String degree_symbol = "\u00b0";
	private static final String unit_temp = "F";
	private static final String unit_temp_metric = "C";
	private static final String unit_dist = "mi";
	private static final String unit_dist_metric = "km";
	private static final String unit_speed = "mph";
	private static final String unit_speed_metric = "kph";
	private static final String unit_press = "in";
	private static final String unit_press_metric = "mb";
	private static final String unit_precip = "in";
	private static final String unit_precip_metric = "mm";
	private static final String unit_snow = "in";
	private static final String unit_snow_metric = "cm";

	public String getTempSymbol()
	{
		return degree_symbol;
	}

	public String getTempUnit()
	{
		return getTempUnit( myUnits );
	}
	public String getTempUnit( String units )
	{
		if ( units.equals(UnitNameEnglish) )
			return unit_temp;
		else if ( units.equals(UnitNameMetric) )
			return unit_temp_metric;
		else
			return unit_temp;
	}	

	public String getDistUnit()
	{
		return getDistUnit( myUnits );
	}
	public String getDistUnit( String units )
	{
		if ( units.equals(UnitNameEnglish) )
			return unit_dist;
		else if ( units.equals(UnitNameMetric) )
			return unit_dist_metric;
		else
			return unit_dist;
	}	

	public String getSpeedUnit()
	{
		return getSpeedUnit( myUnits );
	}
	public String getSpeedUnit( String units )
	{
		if ( units.equals(UnitNameEnglish) )
			return unit_speed;
		else if ( units.equals(UnitNameMetric) )
			return unit_speed_metric;
		else
			return unit_speed;
	}	

	public String getPressureUnit()
	{
		return getPressureUnit( myUnits );
	}
	public String getPressureUnit( String units )
	{
		if ( units.equals(UnitNameEnglish) )
			return unit_press;
		else if ( units.equals(UnitNameMetric) )
			return unit_press_metric;
		else
			return unit_press;
	}	

	public String getRainUnit()
	{
		return getRainUnit( myUnits );
	}
	public String getRainUnit( String units )
	{
		if ( units.equals(UnitNameEnglish) )
			return unit_precip;
		else if ( units.equals(UnitNameMetric) )
			return unit_precip_metric;
		else
			return unit_precip;
	}	

	public String getSnowUnit()
	{
		return getSnowUnit( myUnits );
	}
	public String getSnowUnit( String units )
	{
		if ( units.equals(UnitNameEnglish) )
			return unit_snow;
		else if ( units.equals(UnitNameMetric) )
			return unit_snow_metric;
		else
			return unit_snow;
	}	


	////////////////////////////////////////////////////////
	//
	// Get values for Weather Underground sageweather report
	public String getCityName()
	{
		return NA;
		//return props.getProperty("WU/city");
	}
	public String getStateName()
	{
		return NA;
		//return props.getProperty("WU/city");
	}
	public String getCountryName()
	{
		return NA;
		//return props.getProperty("WU/city");
	}

	// Get current condition for properties:
	// 		Description
	// 		Dewpoint
	// 		DisplayLocFull
	// 		FeelsLike
	// 		HeatIndex
	// 		Humidity
	// 		IconName
	// 		ObservationLocCity
	// 		ObservationLocCountry
	// 		ObservationLocState
	// 		ObservationTime
	// 		Precip1Hr
	// 		PrecipToday
	// 		Pressure
	// 		PressureTrend		-, 0, or +
	// 		Temp
	// 		UV
	// 		Visibility
	// 		WindChill
	// 		WindDir
	// 		WindGust
	// 		WindSpeed
	public String getCurrentCondition(String propName)
	{
		return NA;
		//return props.getProperty("WU/CC/" + propName);
	}
	public java.util.Map getCurConditionProperties()
	{
		return getPropertiesWithPrefix("WU/CC/");
	}
	
	// Get 12 hour forecast for period number for properties:
	// 	Days have even period numbers; nights have odd period numbers
	// 		ChancePrecip
	// 		FCText
	// 		IconName
	// 		PeriodName
	public int get12hrForecastStartPeriod()
	{
		return Integer.parseInt( props.getProperty("WU/FC/12hr/StartPeriod", "0") );
	}
	public int get12hrForecastEndPeriod()
	{
		return Integer.parseInt( props.getProperty("WU/FC/12hr/EndPeriod", "0") );
	}
	public String get12hrForecast(int periodNum, String propName)
	{
		return NA;
		//return props.getProperty("WU/FC/12hr/" + Integer.toString(periodNum) + "/" + propName);
	}
	public java.util.Map get12hrForecastProperties(int periodNum)
	{
		return getPropertiesWithPrefix("WU/FC/12hr/" + Integer.toString(periodNum) + "/");
	}
	
	// Get 24 hour forecast for period number for properties:
	// 		ChancePrecip
	// 		Conditions
	// 		DateDayNum
	// 		DateMonthName
	// 		DateMonthNum
	// 		DateWeekDay
	// 		DateWeekDayShort
	// 		DateYear
	// 		High
	// 		HumidAvg
	// 		HumidMax
	// 		HumidMin
	// 		IconName
	// 		Low
	// 		RainAllDay
	// 		RainDay
	// 		RainNight
	// 		SnowAllDay
	// 		SnowDay
	// 		SnowNight
	// 		WindAvgDir
	// 		WindAvgSpeed
	// 		WindMaxDir
	// 		WindMaxSpeed
	public int get24hrForecastStartPeriod()
	{
		return Integer.parseInt( props.getProperty("WU/FC/24hr/StartPeriod", "0") );
	}
	public int get24hrForecastEndPeriod()
	{
		return Integer.parseInt( props.getProperty("WU/FC/24hr/EndPeriod", "0") );
	}
	public String get24hrForecast(int periodNum, String propName)
	{
		return NA;
		//return props.getProperty("WU/FC/24hr/" + Integer.toString(periodNum) + "/" + propName);
	}
	public java.util.Map get24hrForecastProperties(int periodNum)
	{
		return getPropertiesWithPrefix("WU/FC/24hr/" + Integer.toString(periodNum) + "/");
	}
	
	// Get alert information properties for an alert number:
	// 		StartTime
	// 		ExpireTime
	// 		Title1
	// 		Title2
	// 		Message1
	// 		Message2
	// 		Message3
	public int getAlertCount()
	{
		return Integer.parseInt( props.getProperty("WU/Alert/Count", "0") );
	}
	public int getAlertUniqueID()
	{
		return Integer.parseInt( props.getProperty("WU/Alert/UniqueID", "0") );
	}
	public String getAlertInfo(int AlertNum, String propName)
	{

		return NA;
		//return props.getProperty("WU/Alert/" + Integer.toString(AlertNum) + "/" + propName);
	}
	public java.util.Map getAlertInfoProperties(int AlertNum)
	{
		return getPropertiesWithPrefix("WU/Alert/" + Integer.toString(AlertNum) + "/");
	}
	
	// Get information properties for Astronomy info:
	// 		SunriseHour
	// 		SunriseMinute
	// 		SunsetHour
	// 		SunsetMinute
	// 		MoonPercentIlluminated
	// 		MoonAge
	public String getAstronomyInfo(String propName)
	{
		return NA;
		//return props.getProperty("WU/Astro/" + propName);
	}
	public java.util.Map getAstronomyInfoProperties(int AlertNum)
	{
		return getPropertiesWithPrefix("WU/Astro/");
	}
	public String getMoonImageURL()
	{
		String MoonAge = getAstronomyInfo("MoonAge");
		if ( (MoonAge == null) || (MoonAge.length() == 0) )
			MoonAge = "0";
			 
		return getMoonImageURL( Integer.parseInt(MoonAge) );
	}
	public String getMoonImageURL(int MoonAge)
	{
		int thisMoonAge = 0;
		if ( MoonAge >= 0 )
			thisMoonAge	 = MoonAge;


		String moonURLString = weather_moon_url +
							   Integer.toString(thisMoonAge) +
							   weather_moon_url_suffix;
								   
		//System.out.println( "Weather Moon image string URL: " + moonURLString );
		
		try
		{
			java.net.URI URIItem = new java.net.URI( weather_url_scheme,
			 										 weather_moon_url_base_address,
			 										 moonURLString,
													 null,	
													 null	
													);

			//System.out.println( "Full Weather Moon image string URL: " + URIItem.toASCIIString() );
		
			return URIItem.toASCIIString();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with moon image URL:" + e));
			return null;
		}
	}


	// Get radar image URLs.
	public int getRadarNewestFrameNum()
	{
		return weather_newest_frame_num;
	}
	public int getRadarOldestFrameNum()
	{
		return weather_oldest_frame_num;
	}
	//	Get a radar image URL, using the following parameters:
	// 		radius		The radius from the location, in miles.
	// 		width		Width of image, in pixels
	// 		height		Height of image, in pixels
	// 		frameNum	The frame number, from getRadarOldestFrameNum() to getRadarNewestFrameNum() (5 - 0) 
	public String getRadarURL(int radius, int width, int height, int frameNum )
	{
		int thisRadius	 = radar_default_radius;
		int thisWidth	 = radar_default_width;
		int thisHeight	 = radar_default_height;
		int thisFrameNum = radar_default_frame;

   		if ( radius > 0 )		thisRadius	 = radius;
   		if ( width > 0 )		thisWidth	 = width;
   		if ( height > 0 )		thisHeight	 = height;

   		if ( (frameNum <= weather_oldest_frame_num) && (frameNum >= weather_newest_frame_num) )
   			thisFrameNum = frameNum;


		String LocationCode = props.getProperty("WU/" + LOC_KEY_LOCCODE, default_location_code );;
		if (LocationCode.startsWith("/q/") )
			LocationCode = LocationCode.substring(3);

		try
		{
			String radarURLString = weather_radar_url +
									 weather_query_prefix +
									 LocationCode +
									 weather_radar_url_suffix;
									   
			//System.out.println( "Weather Radar string URL: " + radarURLString );
			

			String radarParamString = weather_radar_standard_options +
									  weather_radar_radius_prefix + java.net.URLEncoder.encode( Integer.toString(thisRadius), "UTF-8" )		+
									  weather_radar_width_prefix  + java.net.URLEncoder.encode( Integer.toString(thisWidth), "UTF-8" )		+
									  weather_radar_height_prefix + java.net.URLEncoder.encode( Integer.toString(thisHeight), "UTF-8" )		+
									  weather_radar_frame_prefix  + java.net.URLEncoder.encode( Integer.toString(thisFrameNum), "UTF-8" )
									  ;
									   
			//System.out.println( "Weather Radar Params: " + radarParamString );


			java.net.URI URIItem = new java.net.URI( weather_url_scheme,
			 										 weather_url_base_address,
			 										 radarURLString,
													 radarParamString,	
													 null	
													);

			//System.out.println( "Full Weather Radar string URL: " + URIItem.toASCIIString() );
		
			return URIItem.toASCIIString();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error sageweather radar URL:" + e));
			return null;
		}
	}



	// Utility calls.
	private java.util.Map getPropertiesWithPrefix(String prefix)
	{
		java.util.Enumeration walker = props.propertyNames();
		java.util.Map rv = new java.util.HashMap();
		while (walker.hasMoreElements())
		{
			Object elem = walker.nextElement();
			if (elem != null && elem.toString().startsWith(prefix))
			{
				rv.put(elem.toString().substring(prefix.length()), props.getProperty(elem.toString()));
			}
		}
		return rv;
	}

	private void saveWeatherDataToCache()
	{
		props.setProperty("lastUpdateTime", Long.toString(lastUpdateTime));

		// Save property values for Weather Underground
		// Store the location
		if ( (myWULoc == null) || (myWULoc.size() == 0) )
		{
			props.setProperty("WU/" + LOC_KEY_CITY, 	""		);
			props.setProperty("WU/" + LOC_KEY_STATE, 	""		);
			props.setProperty("WU/" + LOC_KEY_COUNTRY, 	""		);
			props.setProperty("WU/" + LOC_KEY_NBRHOOD, 	""		);
			props.setProperty("WU/" + LOC_KEY_LOCCODE, 	default_location_code );
		}
		else
		{
			props.setProperty("WU/" + LOC_KEY_CITY, 	getLocationCity( myWULoc )		);
			props.setProperty("WU/" + LOC_KEY_STATE, 	getLocationState( myWULoc )		);
			props.setProperty("WU/" + LOC_KEY_COUNTRY, 	getLocationCountry( myWULoc )	);
			props.setProperty("WU/" + LOC_KEY_NBRHOOD, 	getLocationNeighborhood( myWULoc )	);

			props.setProperty("WU/" + LOC_KEY_LOCCODE, 	getLocationCode( myWULoc )		);
		}
		
		// Remember what units are to be used.
		if (myUnits != null)
			props.setProperty("units", myUnits);
		

		java.io.File cacheFile = new java.io.File(propFilePrefix + (myLocale.length() > 0 ? "_" + myLocale : "") + ".properties");
		java.io.OutputStream out = null;
		//java.io.BufferedWriter out = null;
		try
		{
			out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(cacheFile));
			//out = new java.io.BufferedWriter( new java.io.OutputStreamWriter( new java.io.FileOutputStream(cacheFile), "UTF-8") );
			props.store(out, "SageTV Weather Data");
		}
		catch (Exception e)
		{
			System.out.println("Error caching sageweather data of:" + e);
		}
		finally
		{
			if (out != null)
			{
				try{out.close();}catch(Exception e){}
				out = null;
			}
		}
	}

	private void loadWeatherDataFromCache()
	{
		props = new SortedWeatherProperties();

		java.io.File cacheFile = new java.io.File(propFilePrefix + (myLocale.length() > 0 ? "_" + myLocale : "") + ".properties");
		java.io.InputStream in = null;
		//java.io.BufferedReader in = null;
		try
		{
			in = new java.io.BufferedInputStream(new java.io.FileInputStream(cacheFile));
			//in = new java.io.BufferedReader( new java.io.InputStreamReader( new java.io.FileInputStream(cacheFile), "UTF-8") );
			props.load(in);
		}
		catch (Exception e)
		{
			System.out.println("Error reading cached sageweather data of:" + e);
		}
		finally
		{
			if (in != null)
			{
				try{in.close();}catch(Exception e){}
				in = null;
			}
		}
		try
		{
			lastUpdateTime = Long.parseLong(props.getProperty("lastUpdateTime", "0"));
		}catch (NumberFormatException e){}

		myUnits = props.getProperty("units", UnitNameDefault);

		// Init location settings by using current property values, if any. 
		initCurrentWeatherLoc();
	}

    //This method gets the xml out of the document based on the tag and the parent
    //Caveat, it goes off the first match -- limit the matches using element selection
/*XXX
    public static String getXML(Element doc, String tag, String parent) {
	String tmpstr = null;
	try {
	    NodeList tmpNodes = doc.getElementsByTagName(tag);

	    //get the first match
	    for (int i = 0; i < tmpNodes.getLength() && tmpstr==null; i++)
	    {Untitled
//		System.out.println(i);
		if ((((Element)(tmpNodes.item(i).getParentNode())).getTagName()).equals(parent))
		{
		    // check children for value
//		    System.out.println("got an  xml node  for "+parent+"/"+tag);
//		    System.out.println("Node: "+((Element)tmpNodes.item(i)).getTagName());
//		    System.out.println("Node text: "+tmpNodes.item(i).toString());

		    NodeList children=tmpNodes.item(i).getChildNodes();
		    for ( int child=0; child < children.getLength(); child++) {
			if ( children.item(child).getNodeType()==Node.TEXT_NODE ) {
//			    System.out.println("value" +children.item(child).getNodeValue());
			    tmpstr=children.item(child).getNodeValue();
			    break;
			}
		    }
		}
	    }

	}
	catch (Exception e) {
	    System.out.println("Failed to get xml value for "+parent+"/"+tag);
	    e.printStackTrace();
	}

	if (tmpstr ==null)
	    // found nothing, return blank
	    return "";

	// clean up string

	tmpstr.trim();

	//gets rid of the weird (A) character
	if (tmpstr.indexOf((char)194) != -1)
	{
	    tmpstr = tmpstr.substring(0, tmpstr.indexOf((char)194)) + tmpstr.substring(tmpstr.indexOf((char)194) + 1);
	}

	//trim didn't do its job
	if ((int)tmpstr.charAt(0) == 32)
	    tmpstr = tmpstr.substring(1);

	if ((int)tmpstr.charAt(tmpstr.length() -1) == 32)
	    tmpstr = tmpstr.substring(0, tmpstr.length() - 1);

	return tmpstr;Untitled
    }
*/

    // Source for sample: http://javaalmanac.com/egs/javax.xml.transform/WriteDom.html
    // Parses an XML file and returns a DOM document.
    // If validating is true, the contents is validated against the DTD
    // specified in the file.
    public void parseXmlFile(String xml_url, boolean validating, DefaultHandler hnd) throws Exception
	{
		parseXmlFile( xml_url, validating, hnd, "UTF-8" ); 
	}
    public void parseXmlFile(String xml_url, boolean validating, DefaultHandler hnd, String charset) throws Exception
	{
		System.out.println("downloading from:" + xml_url);

		java.io.InputStream in = null;
	    try 
		{
			java.net.URL u = new java.net.URL(xml_url);
			java.net.URLConnection con = u.openConnection();
			con.setConnectTimeout(30000);
			con.setReadTimeout(30000);
		    in = u.openStream();

			//Reader reader = new InputStreamReader(in,"UTF-8");
			Reader reader = new InputStreamReader(in,charset);
 
			InputSource is = new InputSource(reader);
			//is.setEncoding("UTF-8");
			//is.setEncoding("ISO-8859-2");


			factory.setValidating(validating);
			// Create the builder and parse the file
			//factory.newSAXParser().parse(in, hnd);
			factory.newSAXParser().parse(is, hnd);
	    }
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (Exception e)
				{}
			}
		}
	}

	private SAXParserFactory factory = SAXParserFactory.newInstance();

	
	private class SearchHandler extends DefaultHandler
	{
		private String current_tag;
		private StringBuffer buff = new StringBuffer();

		private java.util.Map locMap;
		private java.util.Vector resultsList;
		
		private boolean inError = false;
		private String ErrorMsg;

		private boolean inLoc = false;
		private boolean inResults = false;
		private boolean inSingleResult = false;

		private java.util.Map locMapPWS;
		private boolean inNearbyWS = false;
		private boolean inAirportWS = false;
		private boolean inPWS = false;
		private boolean inStation = false;

		public java.util.Vector getSearchResults()
		{
			return resultsList;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			//System.out.println( "\n" );
			//System.out.println( "XML startElement=" + qName );
			//System.out.println( "XML   localName=" + localName );
			//System.out.println( "XML   attributes length=" + attributes.getLength() );
			//System.out.println( "XML   attributes data=" + attributes.getValue("data") );

			if ("location".equalsIgnoreCase(qName))
			{
				locMap = new java.util.HashMap();
				resultsList = new java.util.Vector();

				inResults = false;
				inLoc = true;
				inNearbyWS = false;
				//System.out.println( "XML: Found location start" );
			}
			else if ("results".equalsIgnoreCase(qName))
			{
				resultsList = new java.util.Vector();

				inResults = true;
				inLoc = false;
				inNearbyWS = false;
				//System.out.println( "XML: Found results start" );
			}
			else if (resultsList != null && inResults && "result".equalsIgnoreCase(qName) && attributes != null)
			{
				locMap = new java.util.HashMap();
				inSingleResult = true;
				//System.out.println( "XML: Found single result start" );
			}
			else if ("nearby_weather_stations".equalsIgnoreCase(qName) && inLoc)
			{
				inNearbyWS = true;
				//System.out.println( "XML: Found nearby_weather_stations start" );
			}
			else if (inNearbyWS)
			{
				if ("airport".equalsIgnoreCase(qName))
				{
					inAirportWS = true;
				}
				else if ("pws".equalsIgnoreCase(qName))
				{
					inPWS = true;
				}
				else if ( "station".equalsIgnoreCase(qName) && ((inPWS) || (inAirportWS)) )
				{
					locMapPWS = new java.util.HashMap();
					inStation = true;
				}
			}
			else if ("error".equalsIgnoreCase(qName))
			{
				inError = true;
				ErrorMsg = "Weather Underground returned error status.";
			}
			current_tag = qName;
		}
		public void characters(char[] ch, int start, int length)
		{
			String data = new String(ch,start,length);

			//Jump blank chunk
			if (data.trim().length() == 0)
				return;
			buff.append(data);
		}
		public void endElement(String uri, String localName, String qName)
		{
			String data = buff.toString().trim();

			if (qName.equals(current_tag))
				buff = new StringBuffer();

			//System.out.println( "\n" );
			//System.out.println( "XML  endElement=" + qName );
			//System.out.println( "XML   data=[" + data + "]" );
			//System.out.println( "XML   uri=" + uri );
			//System.out.println( "XML   localName=" + localName );

			if (inError)
			{
				if ("error".equalsIgnoreCase(qName))
				{
					inError = false;
					System.out.println(lastError = ErrorMsg );
				}
				else if ("type".equalsIgnoreCase(qName))
				{
					ErrorMsg = ErrorMsg + " Type: " + data;
				}
				else if ("description".equalsIgnoreCase(qName))
				{
					ErrorMsg = ErrorMsg + " Description: " + data;
				}
			}
			else if ("result".equalsIgnoreCase(qName) && resultsList != null)
			{
				//System.out.println( " Location search adding to list: " + locMap );
				resultsList.add( locMap );
				inSingleResult = false;
				//System.out.println( "XML: Found single result end" );
			}
			else if ("location".equalsIgnoreCase(qName) && resultsList != null)
			{
				//System.out.println( " Location search adding to list: " + locMap );
				//resultsList.add( locMap );
				resultsList.insertElementAt( locMap, 0 );
				inLoc = false;
				//System.out.println( "XML: Found location end" );
			}
			else if ( (inLoc && !inNearbyWS) || inSingleResult)
			{
			    if ("city".equalsIgnoreCase(qName))
                {
					String CityName = data;
					locMap.put( LOC_KEY_CITY, CityName );
					//System.out.println( "XML: Found CityName = " + CityName );
				}
                else if ("state".equalsIgnoreCase(qName))
                {
					String StateName = data;
					locMap.put( LOC_KEY_STATE, StateName );
					//System.out.println( "XML: Found StateName = " + StateName );
				}
                else if ("country_name".equalsIgnoreCase(qName))
                {
					String CountryName = data;
					locMap.put( LOC_KEY_COUNTRY, CountryName );
					//System.out.println( "XML: Found CountryName = " + CountryName );
				}
                else if ("l".equalsIgnoreCase(qName))
                {
					String LocationCode = data;

					if (LocationCode.startsWith("/q/") )
						LocationCode = LocationCode.substring(3);

					locMap.put( LOC_KEY_LOCCODE, LocationCode );
					//System.out.println( "XML: Found LocationCode = " + LocationCode );
				}
			}	// End else in location result.
			else if (inNearbyWS)
			{
				if ("nearby_weather_stations".equalsIgnoreCase(qName) && inLoc)
				{
					inNearbyWS = false;
				}
				else if ("airport".equalsIgnoreCase(qName))
				{
					inAirportWS = false;
				}
				else if ("pws".equalsIgnoreCase(qName))
				{
					inPWS = false;
				}
				else if (inStation)
				{
					if ( "station".equalsIgnoreCase(qName) && ((inPWS) || (inAirportWS)) )
					{
						//System.out.println( " Location search adding to list: " + locMapPWS );
						resultsList.add( locMapPWS );
						inStation = false;
					}
				    else if ("city".equalsIgnoreCase(qName))
	                {
						String CityName = data;
						locMapPWS.put( LOC_KEY_CITY, CityName );
						if (inAirportWS)
							locMapPWS.put( LOC_KEY_NBRHOOD, "Airport" );
						//System.out.println( "XML: Found CityName = " + CityName );
					}
	                else if ("state".equalsIgnoreCase(qName))
	                {
						String StateName = data;
						locMapPWS.put( LOC_KEY_STATE, StateName );
						//System.out.println( "XML: Found StateName = " + StateName );
					}
	                else if ("country_name".equalsIgnoreCase(qName))
	                {
						String CountryName = data;
						locMapPWS.put( LOC_KEY_COUNTRY, CountryName );
						//System.out.println( "XML: Found CountryName = " + CountryName );
					}
	                else if ("neighborhood".equalsIgnoreCase(qName))
	                {
						String NeightborhoodName = data;
						locMapPWS.put( LOC_KEY_NBRHOOD, NeightborhoodName );
						//System.out.println( "XML: Found NeightborhoodName = " + NeightborhoodName );
					}
	                else if ("icao".equalsIgnoreCase(qName) && inAirportWS)
	                {
						String LocationCode = data;

						if (LocationCode.startsWith("/q/") )
							LocationCode = LocationCode.substring(3);

						locMapPWS.put( LOC_KEY_LOCCODE, LocationCode );
						//System.out.println( "XML: Found Airport LocationCode = " + LocationCode );
					}
	                else if ("id".equalsIgnoreCase(qName) && inPWS)
	                {
						String LocationCode = data;

						if (LocationCode.startsWith("/q/") )
							LocationCode = LocationCode.substring(3);

						LocationCode = "pws:" + LocationCode;

						locMapPWS.put( LOC_KEY_LOCCODE, LocationCode );
						//System.out.println( "XML: Found Airport LocationCode = " + LocationCode );
					}
				}	// End else in station section.
			}	// End else in nearby sageweather station.

		}
	}


	private class WeatherUndergroundUpdateHandler extends DefaultHandler
	{
		private String current_tag;
		private StringBuffer buff = new StringBuffer();

		private boolean inError = false;
		private String ErrorMsg;

		private boolean foundResults;

		// Current Conditions	
		private boolean inCC = false;
		private boolean inDispLoc = false;
		private boolean inObsLoc = false;

		// 12 hour period forecasts	
		private boolean inTextForecast = false;
		private boolean inTextDayForecast = false;
		private int startTextPeriod = -1;
		private int endTextPeriod = -1;
		private int curTextPeriod = -1;
		private java.util.Map TextFCInfo;

		// 24 hour period forecasts	
		private boolean inSimpleForecast = false;
		private boolean inSimpleDayForecast = false;
		private int startSimplePeriod = -1;
		private int endSimplePeriod = -1;
		private int curSimplePeriod = -1;
		private java.util.Map SimpleFCInfo;

		private boolean inDate = false;
		private boolean inHigh = false;
		private boolean inLow = false;

		private boolean inQPFAllDay = false;
		private boolean inQPFDay = false;
		private boolean inQPFNight = false;

		private boolean inSnowAllDay = false;
		private boolean inSnowDay = false;
		private boolean inSnowNight = false;

		private boolean inMaxWind = false;
		private boolean inAvgWind = false;

		// Alerts	
		private boolean inAlerts = false;
		private boolean inSingleAlert = false;
		private boolean isUSAlert = true;
		private int CurAlertNum = 0;
		private java.util.Map AlertInfo;
		private String AllAlertText = "";

		// Astronomy	
		private boolean inAstronomy = false;
		private boolean inSunrise = false;
		private boolean inSunset = false;


		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if ("response".equals(qName))
				foundResults = true;
			else if ("current_observation".equals(qName))
			{
				inCC = true;

				// Clear current conditions properties so no old values are still active.
				props.put("WU/CC/Description", weather_result_NA );
				props.put("WU/CC/Temp", weather_result_NA ); 
				props.put("WU/CC/Humidity", weather_result_NA );
				props.put("WU/CC/WindDir", weather_result_NA );
				props.put("WU/CC/WindSpeed", weather_result_NA );
				props.put("WU/CC/WindGust", weather_result_NA );
				props.put("WU/CC/Pressure", weather_result_NA );
				props.put("WU/CC/PressureTrend", weather_result_NA );
				props.put("WU/CC/Dewpoint", weather_result_NA );
				props.put("WU/CC/HeatIndex", weather_result_NA );
				props.put("WU/CC/WindChill", weather_result_NA );
				props.put("WU/CC/FeelsLike", weather_result_NA );
				props.put("WU/CC/Visibility", weather_result_NA );
				props.put("WU/CC/UV", weather_result_NA );
				props.put("WU/CC/Precip1Hr", weather_result_NA );
				props.put("WU/CC/PrecipToday", weather_result_NA );
				
				props.setProperty("WU/CC/IconName", weather_icon_NA );
			}
			else if (inCC)
			{
				if ("display_location".equals(qName))
				{
					inDispLoc = true;
				}
				else if ("observation_location".equals(qName))
				{
					inObsLoc = true;
				}
			}
			else if ("txt_forecast".equals(qName))
			{
				inTextForecast = true;
				startTextPeriod = -1;
				endTextPeriod = -1;

				// Clear 12hr forecast properties so no old values are still active.
				props.setProperty("WU/FC/12hr/StartPeriod", "0" );
				props.setProperty("WU/FC/12hr/EndPeriod", "0" );

				props.setProperty("WU/FC/12hr/0/IconName", 		weather_icon_NA );
				props.setProperty("WU/FC/12hr/0/PeriodName", 	weather_result_NA );
				props.setProperty("WU/FC/12hr/0/FCText", 		weather_result_NA );
				props.setProperty("WU/FC/12hr/0/ChancePrecip", 	weather_result_NA );
			}
			else if (inTextForecast)
			{
				if ("forecastday".equals(qName))
				{
					inTextDayForecast = true;
					TextFCInfo = new java.util.HashMap(); 
				}
			}
			
			else if ("simpleforecast".equals(qName))
			{
				inSimpleForecast = true;
				startSimplePeriod = -1;
				endSimplePeriod = -1;
				
				// Clear 24hr forecast properties so no old values are still active.
				props.setProperty("WU/FC/24hr/StartPeriod", "0" );
				props.setProperty("WU/FC/24hr/EndPeriod", "0" );

				props.setProperty("WU/FC/24hr/0/Conditions", 		weather_result_NA );

				props.setProperty("WU/FC/24hr/0/IconName", 			weather_icon_NA );
				props.setProperty("WU/FC/24hr/0/ChancePrecip", 		weather_result_NA );

				props.setProperty("WU/FC/24hr/0/HumidAvg", 			weather_result_NA );
				props.setProperty("WU/FC/24hr/0/HumidMax", 			weather_result_NA );
				props.setProperty("WU/FC/24hr/0/HumidMin", 			weather_result_NA );

				props.setProperty("WU/FC/24hr/0/High",	 			weather_result_NA );
				props.setProperty("WU/FC/24hr/0/Low", 				weather_result_NA );

				props.setProperty("WU/FC/24hr/0/DateDayNum",		weather_result_NA );
				props.setProperty("WU/FC/24hr/0/DateMonthNum", 		weather_result_NA );
				props.setProperty("WU/FC/24hr/0/DateMonthName", 	weather_result_NA );
				props.setProperty("WU/FC/24hr/0/DateYear", 		 	weather_result_NA );
				props.setProperty("WU/FC/24hr/0/DateWeekDayShort",	weather_result_NA );
				props.setProperty("WU/FC/24hr/0/DateWeekDay",  		weather_result_NA );

				props.setProperty("WU/FC/24hr/0/RainAllDay",   		weather_result_NA );
				props.setProperty("WU/FC/24hr/0/RainDay",  			weather_result_NA );
				props.setProperty("WU/FC/24hr/0/RainNight",	  		weather_result_NA );

				props.setProperty("WU/FC/24hr/0/SnowAllDay",  		weather_result_NA );
				props.setProperty("WU/FC/24hr/0/SnowDay",  			weather_result_NA );
				props.setProperty("WU/FC/24hr/0/SnowNight",  		weather_result_NA );

				props.setProperty("WU/FC/24hr/0/WindMaxSpeed",  	weather_result_NA );
				props.setProperty("WU/FC/24hr/0/WindMaxDir",  		weather_result_NA );
				props.setProperty("WU/FC/24hr/0/WindAvgSpeed",  	weather_result_NA );
				props.setProperty("WU/FC/24hr/0/WindAvgDir",  		weather_result_NA );

			}
			else if (inSimpleForecast)
			{
				if ("forecastday".equals(qName))
				{
					inSimpleDayForecast = true;
					SimpleFCInfo = new java.util.HashMap(); 
				}
				else if (inSimpleDayForecast)
				{
					if ("date".equals(qName))
						inDate = true;
					else if ("high".equals(qName))
						inHigh = true;
					else if ("low".equals(qName))
						inLow = true;
					else if ("qpf_allday".equals(qName))
						inQPFAllDay = true;
					else if ("qpf_day".equals(qName))
						inQPFDay = true;
					else if ("qpf_night".equals(qName))
						inQPFNight = true;
					else if ("snow_allday".equals(qName))
						inSnowAllDay = true;
					else if ("snow_day".equals(qName))
						inSnowDay = true;
					else if ("snow_night".equals(qName))
						inSnowNight = true;
					else if ("maxwind".equals(qName))
						inMaxWind = true;
					else if ("avewind".equals(qName))
						inAvgWind = true;
				}
			}

			else if ("alerts".equals(qName))
			{
				inAlerts = true;
				isUSAlert = true;		// By default, assume US alert

				// Track all alert text for a hash code.
				AllAlertText = "";
				
				// Write initial number of alerts as 0.
				CurAlertNum = 0; 
				props.setProperty("WU/Alert/Count", Integer.toString(CurAlertNum) );
			}
			else if (inAlerts)
			{
				if ("alert".equals(qName))
				{
					inSingleAlert = true;
					AlertInfo = new java.util.HashMap();
				
					// Increment alert number & write it as the number of alerts.
					CurAlertNum = CurAlertNum + 1;
					props.setProperty("WU/Alert/Count", Integer.toString(CurAlertNum) );
				
					// Clear all data for this alert.
					props.setProperty("WU/Alert/" + CurAlertNum + "/StartTime",		"0" );
					props.setProperty("WU/Alert/" + CurAlertNum + "/ExpireTime",	"0" );
					
					props.setProperty("WU/Alert/" + CurAlertNum + "/Title1",		"" );
					props.setProperty("WU/Alert/" + CurAlertNum + "/Title2",		"" );
					
					props.setProperty("WU/Alert/" + CurAlertNum + "/Message1",		"" );
					props.setProperty("WU/Alert/" + CurAlertNum + "/Message2",		"" );
					props.setProperty("WU/Alert/" + CurAlertNum + "/Message3",		"" );
				}
			}

			else if ("moon_phase".equals(qName))
			{
				inAstronomy = true;
				
				// Clear astronomy properties so no old values are still active.
				// ccccccccccccccccc
				// weather_result_NA
				// weather_icon_NA
				props.put("WU/Astro/SunriseHour", weather_result_NA );
				props.put("WU/Astro/SunriseMinute", weather_result_NA );
				props.put("WU/Astro/SunsetHour", weather_result_NA );
				props.put("WU/Astro/SunsetMinute", weather_result_NA );
				props.put("WU/Astro/MoonPercentIlluminated", weather_result_NA );
				props.put("WU/Astro/MoonAge", weather_result_NA );
			}
			else if (inAstronomy)
			{
				if ("sunrise".equals(qName))
				{
					inSunrise = true;
				}
				if ("sunset".equals(qName))
				{
					inSunset = true;
				}
			}

			else if ("error".equalsIgnoreCase(qName))
			{
				inError = true;
				ErrorMsg = "Weather Underground returned error status.";
			}
			current_tag = qName;
		}
		public void characters(char[] ch, int start, int length)
		{
			String data = new String(ch,start,length);

			//Jump blank chunk
			if (data.trim().length() == 0)
				return;
			buff.append(data);
		}

		public void endElement(String uri, String localName, String qName)
		{
			String data = buff.toString().trim();

			if (qName.equals(current_tag))
				buff = new StringBuffer();

			if (inError)
			{
				if ("error".equalsIgnoreCase(qName))
				{
					inError = false;
					System.out.println(lastError = ErrorMsg );
				}
				else if ("type".equalsIgnoreCase(qName))
				{
					ErrorMsg = ErrorMsg + " Type: " + data;
				}
				else if ("description".equalsIgnoreCase(qName))
				{
					ErrorMsg = ErrorMsg + " Description: " + data;
				}
			}
			else if (inCC)
			{
				//System.out.println( "XML InCC data:" + qName + "=" + data );
				if ("current_observation".equals(qName))
				{
					inCC = false;
					lastUpdateTime = System.currentTimeMillis();
				}
				else if (inDispLoc)
				{
					if ("display_location".equals(qName))
					{
						inDispLoc = false;
					}
					else if ("full".equals(qName))
					{
						// data is the full name of the location.
						if (data != null && data.length() > 0)
							props.put("WU/CC/DisplayLocFull", data );
					}
					else if ("city".equals(qName))
					{
						// data is the city name. If it exists, repace the curernt loc's city if there is no current city.
						if (data != null && data.length() > 0)
						{
							String ThisCity = data;
							String CurCity = getLocationCity( myWULoc );
							if (CurCity == null || CurCity.length() == 0)
								setWeatherLocCity( ThisCity );
						}
					}
					else if ("state".equals(qName))
					{
						// data is the state name. If it exists, repace the curernt loc's state if there is no current state.
						if (data != null && data.length() > 0)
						{
							String ThisState = data;
							String CurState = getLocationState( myWULoc );
							if (CurState == null || CurState.length() == 0)
								setWeatherLocState( ThisState );
						}
					}
					else if ("country".equals(qName))
					{
						// data is the country name. If it exists, repace the curernt loc's country if there is no current country.
						if (data != null && data.length() > 0)
						{
							String ThisCountry = data;
							String CurCountry = getLocationCountry( myWULoc );
							if (CurCountry == null || CurCountry.length() == 0)
								setWeatherLocCountry( ThisCountry );
						}
					}
				}
				else if (inObsLoc)
				{
					// Handle data for the observation location.
					if ("observation_location".equals(qName))
						inObsLoc = false;
					else if ("city".equals(qName))
						props.put("WU/CC/ObservationLocCity", data );
					else if ("state".equals(qName))
						props.put("WU/CC/ObservationLocState", data );
					else if ("country".equals(qName))
						props.put("WU/CC/ObservationLocCountry", data );
				}
				else if ("observation_epoch".equals(qName))
					props.put("WU/CC/ObservationTime", data );
				else if ("sageweather".equals(qName))
					props.put("WU/CC/Description", ConvertWeatherResponseIfNA( data, weather_result_NA ) ); 
				else if ( ("temp_f".equals(qName)) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/Temp", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ( ("temp_c".equals(qName)) && (myUnits.equals(UnitNameMetric)) )
					props.put("WU/CC/Temp", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("relative_humidity".equals(qName))
					props.put("WU/CC/Humidity", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("wind_dir".equals(qName))
					props.put("WU/CC/WindDir", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ( ("wind_mph".equals(qName)) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/WindSpeed", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ( ("wind_gust_mph".equals(qName)) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/WindGust", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ( ("wind_kph".equals(qName)) && (myUnits.equals(UnitNameMetric)) )
					props.put("WU/CC/WindSpeed", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ( ("wind_gust_kph".equals(qName)) && (myUnits.equals(UnitNameMetric)) )
					props.put("WU/CC/WindGust", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				
				else if ( ("pressure_in".equals(qName)) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/Pressure", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ( ("pressure_mb".equals(qName)) && (myUnits.equals(UnitNameMetric)) )
					props.put("WU/CC/Pressure", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("pressure_trend".equals(qName))
					props.put("WU/CC/PressureTrend", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("dewpoint_f".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/Dewpoint", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("dewpoint_c".equals(qName) && (myUnits.equals(UnitNameMetric)) )
					props.put("WU/CC/Dewpoint", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("heat_index_f".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/HeatIndex", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("heat_index_c".equals(qName) && (myUnits.equals(UnitNameMetric)) )
					props.put("WU/CC/HeatIndex", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("windchill_f".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/WindChill", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("windchill_c".equals(qName)&& myUnits.equals(UnitNameMetric) )
					props.put("WU/CC/WindChill", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("feelslike_f".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/FeelsLike", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("feelslike_c".equals(qName)&& myUnits.equals(UnitNameMetric) )
					props.put("WU/CC/FeelsLike", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("visibility_mi".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/Visibility", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("visibility_km".equals(qName) && myUnits.equals(UnitNameMetric) )
					props.put("WU/CC/Visibility", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("UV".equals(qName))
					props.put("WU/CC/UV", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("precip_1hr_in".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/Precip1Hr", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("precip_1hr_metric".equals(qName) && myUnits.equals(UnitNameMetric) )
					props.put("WU/CC/Precip1Hr", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("precip_today_in".equals(qName) && myUnits.equals(UnitNameEnglish) )
					props.put("WU/CC/PrecipToday", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("precip_today_metric".equals(qName) && myUnits.equals(UnitNameMetric) )
					props.put("WU/CC/PrecipToday", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

				else if ("icon_url".equals(qName))
				{
					String IconName = data.substring(data.lastIndexOf('/') + 1, data.lastIndexOf('.'));
					props.setProperty("WU/CC/IconName", ConvertWeatherResponseIfNA( IconName, weather_icon_NA ) );
					//props.setProperty("WU/CC/IconNameReplacement", getIconReplacementString(IconName) );
				}
			}	// End else inCC
			else if (inTextForecast)
			{
				if ("txt_forecast".equals(qName))
				{
					inTextForecast = false;
				}
				else if (inTextDayForecast)
				{
					if ("forecastday".equals(qName))
					{
						inTextDayForecast = false;

						// Write all info for this period.
						props.setProperty("WU/FC/12hr/" + curTextPeriod + "/IconName", 					(String) TextFCInfo.get("IconName") );
						//props.setProperty("WU/FC/12hr/" + curTextPeriod + "/IconNameReplacement", 	(String) TextFCInfo.get("IconNameReplacement") );
						props.setProperty("WU/FC/12hr/" + curTextPeriod + "/PeriodName",	 			(String) TextFCInfo.get("PeriodName") );
						props.setProperty("WU/FC/12hr/" + curTextPeriod + "/FCText", 					(String) TextFCInfo.get("FCText") );
						props.setProperty("WU/FC/12hr/" + curTextPeriod + "/ChancePrecip", 				(String) TextFCInfo.get("ChancePrecip") );
					}
					else if ("period".equals(qName))
					{
						curTextPeriod = Integer.parseInt(data);
						if ( (startTextPeriod == -1) || (curTextPeriod < startTextPeriod) )
						{
							startTextPeriod = curTextPeriod;
							props.setProperty("WU/FC/12hr/StartPeriod", Integer.toString(startTextPeriod) );
						}
						
						if ( (endTextPeriod == -1) || (curTextPeriod > endTextPeriod) )
						{
							endTextPeriod = curTextPeriod;
							props.setProperty("WU/FC/12hr/EndPeriod", Integer.toString(endTextPeriod) );
						}
					}
					else if ("icon".equals(qName))
					{
						// Is this Day or Night?
						boolean isDay = (curTextPeriod % 2 == 0);
						//System.out.println( " curTextPeriod=" + curTextPeriod + " is " + (isDay ? "Day" : "Night") );
						String iconName = ConvertWeatherResponseIfNA( data, weather_icon_NA );
						if (!isDay)
						{
							if ( !iconName.startsWith("nt_") )
								iconName = "nt_" + iconName;
						}
						TextFCInfo.put( "IconName", iconName );
						//TextFCInfo.put( "IconNameReplacement", getIconReplacementString(iconName) );
					}
					else if ("title".equals(qName))
					{
						TextFCInfo.put( "PeriodName", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if ("fcttext".equals(qName) && myUnits.equals(UnitNameEnglish) )
					{
						TextFCInfo.put( "FCText", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if ("fcttext_metric".equals(qName) && myUnits.equals(UnitNameMetric) )
					{
						TextFCInfo.put( "FCText", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if ("pop".equals(qName))
					{
						TextFCInfo.put( "ChancePrecip", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
				}
			}	// End else 12h inTextForecast

			else if (inSimpleForecast)
			{
				if ("simpleforecast".equals(qName))
				{
					inSimpleForecast = false;
				}
				else if (inSimpleDayForecast)
				{
					if ("forecastday".equals(qName))
					{
						inSimpleDayForecast = false;

						// Write all info for this period.
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/Conditions", 			(String) SimpleFCInfo.get("Conditions") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/IconName", 			(String) SimpleFCInfo.get("IconName") );
						//props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/IconNameReplacement", (String) SimpleFCInfo.get("IconNameReplacement") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/ChancePrecip", 		(String) SimpleFCInfo.get("ChancePrecip") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/HumidAvg", 			(String) SimpleFCInfo.get("HumidAvg") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/HumidMax", 			(String) SimpleFCInfo.get("HumidMax") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/HumidMin", 			(String) SimpleFCInfo.get("HumidMin") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/High",	 			(String) SimpleFCInfo.get("High") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/Low", 				(String) SimpleFCInfo.get("Low") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/DateDayNum",		 	(String) SimpleFCInfo.get("DateDayNum") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/DateMonthNum", 		(String) SimpleFCInfo.get("DateMonthNum") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/DateMonthName", 		(String) SimpleFCInfo.get("DateMonthName") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/DateYear", 		 	(String) SimpleFCInfo.get("DateYear") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/DateWeekDayShort",	(String) SimpleFCInfo.get("DateWeekDayShort") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/DateWeekDay",  		(String) SimpleFCInfo.get("DateWeekDay") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/RainAllDay",   		(String) SimpleFCInfo.get("RainAllDay") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/RainDay",  			(String) SimpleFCInfo.get("RainDay") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/RainNight",	  		(String) SimpleFCInfo.get("RainNight") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/SnowAllDay",  		(String) SimpleFCInfo.get("SnowAllDay") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/SnowDay",  			(String) SimpleFCInfo.get("SnowDay") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/SnowNight",  			(String) SimpleFCInfo.get("SnowNight") );

						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/WindMaxSpeed",  		(String) SimpleFCInfo.get("WindMaxSpeed") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/WindMaxDir",  		(String) SimpleFCInfo.get("WindMaxDir") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/WindAvgSpeed",  		(String) SimpleFCInfo.get("WindAvgSpeed") );
						props.setProperty("WU/FC/24hr/" + curSimplePeriod + "/WindAvgDir",  		(String) SimpleFCInfo.get("WindAvgDir") );

					}
					else if ("period".equals(qName))
					{
						curSimplePeriod = Integer.parseInt(data);
						//SimpleFCInfo.put( "period", curSimplePeriod );
						if ( (startSimplePeriod == -1) || (curSimplePeriod < startSimplePeriod) )
						{
							startSimplePeriod = curSimplePeriod;
							props.setProperty("WU/FC/24hr/StartPeriod", Integer.toString(startSimplePeriod) );
						}

						if ( (endSimplePeriod == -1) || (curSimplePeriod > endSimplePeriod) )
						{
							endSimplePeriod = curSimplePeriod;
							props.setProperty("WU/FC/24hr/EndPeriod", Integer.toString(endSimplePeriod) );
						}
					}
					else if (inDate)
					{
						if ("date".equals(qName))
							inDate = false;
						else if ("day".equals(qName))
							SimpleFCInfo.put( "DateDayNum", data );
						else if ("month".equals(qName))
							SimpleFCInfo.put( "DateMonthNum", data );
						else if ("monthname".equals(qName))
							SimpleFCInfo.put( "DateMonthName", data );
						else if ("year".equals(qName))
							SimpleFCInfo.put( "DateYear", data );
						else if ("weekday_short".equals(qName))
							SimpleFCInfo.put( "DateWeekDayShort", data );
						else if ("weekday".equals(qName))
							SimpleFCInfo.put( "DateWeekDay", data );
					}
					else if (inHigh)
					{
						if ("high".equals(qName))
							inHigh = false;
						else if ("fahrenheit".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "High", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("celsius".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "High", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inLow)
					{
						if ("low".equals(qName))
							inLow = false;
						else if ("fahrenheit".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "Low", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("celsius".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "Low", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inQPFAllDay)
					{
						if ("qpf_allday".equals(qName))
							inQPFAllDay = false;
						else if ("in".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "RainAllDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("mm".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "RainAllDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inQPFDay)
					{
						if ("qpf_day".equals(qName))
							inQPFDay = false;
						else if ("in".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "RainDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("mm".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "RainDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inQPFNight)
					{
						if ("qpf_night".equals(qName))
							inQPFNight = false;
						else if ("in".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "RainNight", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("mm".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "RainNight", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inSnowAllDay)
					{
						if ("snow_allday".equals(qName))
							inSnowAllDay = false;
						else if ("in".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "SnowAllDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("cm".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "SnowAllDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inSnowDay)
					{
						if ("snow_day".equals(qName))
							inSnowDay = false;
						else if ("in".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "SnowDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("cm".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "SnowDay", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inSnowNight)
					{
						if ("snow_night".equals(qName))
							inSnowNight = false;
						else if ("in".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "SnowNight", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("cm".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "SnowNight", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inMaxWind)
					{
						if ("maxwind".equals(qName))
							inMaxWind = false;
						else if ("mph".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "WindMaxSpeed", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("kph".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "WindMaxSpeed", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("dir".equals(qName))
							SimpleFCInfo.put( "WindMaxDir", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if (inAvgWind)
					{
						if ("avewind".equals(qName))
							inAvgWind = false;
						else if ("mph".equals(qName) && myUnits.equals(UnitNameEnglish) )
							SimpleFCInfo.put( "WindAvgSpeed", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("kph".equals(qName) && myUnits.equals(UnitNameMetric) )
							SimpleFCInfo.put( "WindAvgSpeed", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
						else if ("dir".equals(qName))
							SimpleFCInfo.put( "WindAvgDir", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					}
					else if ("conditions".equals(qName))
						SimpleFCInfo.put( "Conditions", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					else if ("icon".equals(qName))
					{
						SimpleFCInfo.put( "IconName", ConvertWeatherResponseIfNA( data, weather_icon_NA ) );
						//SimpleFCInfo.put( "IconNameReplacement", getIconReplacementString(data) );
					}
					else if ("pop".equals(qName))
						SimpleFCInfo.put( "ChancePrecip", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					else if ("avehumidity".equals(qName))
						SimpleFCInfo.put( "HumidAvg", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					else if ("maxhumidity".equals(qName))
						SimpleFCInfo.put( "HumidMax", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					else if ("minhumidity".equals(qName))
						SimpleFCInfo.put( "HumidMin", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				}
			}	// End else 24h inSimpleForecast

			else if (inAlerts)
			{
				if ("alerts".equals(qName))
				{
					inAlerts = false;

					// Get hash code for all alert text & store that.
					int AlertHasCode = AllAlertText.hashCode();
					props.setProperty("WU/Alert/UniqueID", Integer.toString(AlertHasCode) );

				}
				else if (inSingleAlert)
				{
					if ("alert".equals(qName))
					{
						inSingleAlert = false;

						// Write all info for this alert.
						if (isUSAlert)
						{
							props.setProperty("WU/Alert/" + CurAlertNum + "/StartTime", 		(String) AlertInfo.get("date") );
							props.setProperty("WU/Alert/" + CurAlertNum + "/ExpireTime",		(String) AlertInfo.get("expires") );
							
							props.setProperty("WU/Alert/" + CurAlertNum + "/Title1", 			(String) AlertInfo.get("description") );
							
							props.setProperty("WU/Alert/" + CurAlertNum + "/Message1", 			(String) AlertInfo.get("message") );
						}
						else
						{
							props.setProperty("WU/Alert/" + CurAlertNum + "/StartTime", 		(String) AlertInfo.get("date") );
							props.setProperty("WU/Alert/" + CurAlertNum + "/ExpireTime",		(String) AlertInfo.get("expires") );

							props.setProperty("WU/Alert/" + CurAlertNum + "/Title1", 			(String) AlertInfo.get("wtype_meteoalarm_name") );
							props.setProperty("WU/Alert/" + CurAlertNum + "/Title2", 			(String) AlertInfo.get("level_meteoalarm_name") );

							props.setProperty("WU/Alert/" + CurAlertNum + "/Message1", 			(String) AlertInfo.get("level_meteoalarm_description") );
							props.setProperty("WU/Alert/" + CurAlertNum + "/Message2", 			(String) AlertInfo.get("message") );
							props.setProperty("WU/Alert/" + CurAlertNum + "/Message3", 			(String) AlertInfo.get("attribution") );
						}
					}


					// Handle alert fields.
					/*		
						US Fields:
					        description                     Title1
					        date		                    Start Time
					        expires		                    End Time
					        message                         Message1

						European Fields:
					        wtype_meteoalarm_name           Title1
					        level_meteoalarm_name           Title2
					        date		                    Start Time
					        expires		                    End Time
					        level_meteoalarm_description    Message1            
					        message                         Message2    Same as description?  
					        attribution                     Message3    Must include the attribution when displaying an alert from Europe
					*/
					// These alert fields are US or European.
					else if ("date".equals(qName))
					{
						AlertInfo.put( "date", data );
						AllAlertText = AllAlertText + data;
					}
					else if ("expires".equals(qName))
					{
						AlertInfo.put( "expires", data );
						AllAlertText = AllAlertText + data;
					}

					else if ("description".equals(qName))
					{
						AlertInfo.put( "description", data );
						AllAlertText = AllAlertText + data;
					}
					else if ("message".equals(qName))
					{
						AlertInfo.put( "message", data );
						AllAlertText = AllAlertText + data;
					}

					// These alert fields are only European.
					else if ("wtype_meteoalarm_name".equals(qName))
					{
						AlertInfo.put( "wtype_meteoalarm_name", data );
						isUSAlert = false;		// European alert
						AllAlertText = AllAlertText + data;
					}
					else if ("level_meteoalarm_name".equals(qName))
					{
						AlertInfo.put( "level_meteoalarm_name", data );
						isUSAlert = false;		// European alert
						AllAlertText = AllAlertText + data;
					}
					else if ("level_meteoalarm_description".equals(qName))
					{
						AlertInfo.put( "level_meteoalarm_description", data );
						isUSAlert = false;		// European alert
						AllAlertText = AllAlertText + data;
					}
					else if ("attribution".equals(qName))
					{
						AlertInfo.put( "attribution", data );
						isUSAlert = false;		// European alert
						AllAlertText = AllAlertText + data;
					}

				}
			}	// End else in alerts section

			else if (inAstronomy)
			{
				if ("moon_phase".equals(qName))
				{
					inAstronomy = false;
				}
				else if (inSunrise)
				{
					if ("sunrise".equals(qName))
					{
						inSunrise = false;
					}
					else if ("hour".equals(qName))
						props.put("WU/Astro/SunriseHour", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					else if ("minute".equals(qName))
						props.put("WU/Astro/SunriseMinute", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				}
				else if (inSunset)
				{
					if ("sunset".equals(qName))
					{
						inSunset = false;
					}
					else if ("hour".equals(qName))
						props.put("WU/Astro/SunsetHour", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
					else if ("minute".equals(qName))
						props.put("WU/Astro/SunsetMinute", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				}
				else if ("percentIlluminated".equals(qName))
					props.put("WU/Astro/MoonPercentIlluminated", ConvertWeatherResponseIfNA( data, weather_result_NA ) );
				else if ("ageOfMoon".equals(qName))
					props.put("WU/Astro/MoonAge", ConvertWeatherResponseIfNA( data, weather_result_NA ) );

			}	// End else in Astronomy section

		}


		public void endDocument() throws SAXException
		{
			if (!foundResults)
				System.out.println(lastError = ("Weather Underground returned empty doc."));
		}
	}

	// Check response value for any type of NA equivalent & if it is, set the desired NA value. 
	private static String ConvertWeatherResponseIfNA( String responseValue, String NAText )
	{
		boolean IsNA = false;

		if ( responseValue == null )
			IsNA = true;
		else if (responseValue.length() == 0 )
			IsNA = true;
		else if (responseValue.startsWith("-999") )
			IsNA = true;
		else if (responseValue.equals("-"))
			IsNA = true;
		else if (responseValue.equalsIgnoreCase("null"))
			IsNA = true;
		else if (responseValue.equalsIgnoreCase("NA"))
			IsNA = true;
		else if (responseValue.equalsIgnoreCase("N/A"))
			IsNA = true;

		return (IsNA ? NAText : responseValue);
	}


	// Private support class to sort the properties file. 
	private class SortedWeatherProperties extends java.util.Properties 
	{
	  public java.util.Enumeration keys() 
	  {
	     java.util.Enumeration keysEnum = super.keys();
	     java.util.Vector keyList = new java.util.Vector();

	     while(keysEnum.hasMoreElements())
	     {
	       keyList.add((String)keysEnum.nextElement());
	     }
	     java.util.Collections.sort(keyList);
	     return keyList.elements();
	  }
	}

}
