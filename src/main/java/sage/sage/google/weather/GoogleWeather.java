// Program: GoogleWeather
// Author: Andrew Visscher, based on WeatherDotCom.java by Niel Markwick & Jeff Kardatzke
// Last Modification Date: October 2, 2012
//
// Description: This application is intended to be used in a sageweather
// module for the SageTV custom stv files.  Currently the program is
// custom made to read the xml from http://www.google.com/ig/api?weather and/or
// forecast.sageweather.gov and return the data in to a text properties file.
// This version replaced the previous one that was based on the XML sageweather feed
// from www.sageweather.com.
//
// This version uses the DOM library which appears to be installed with
// the latest j2sdk from java.sun.com.


/*
 * Usage of updated version for SageTV integration in the Studio:
 *
 *	The version of this class can be obtained from:
 * sage_google_weather_GoogleWeather_getWeatherVersion()
 *
 *
 *  First get an instance of this class using:
 * Weather = sage_google_weather_GoogleWeather_getInstance()
 *
 *	Or, to get an instance of this class for an alternate sageweather location:
 *	Note: the AltLocName string must contain valid filename characters only.
 * Weather = sage_google_weather_GoogleWeather_getInstance("AltLocName")
 *
 *
 *	To get a list of alternate sageweather locations, including the main "" location:
 * sage_google_weather_GoogleWeather_getAllWeatherLocations() returns a Vector
 *
 *	To get a list of alternate sageweather locations, not including the "" location:
 * sage_google_weather_GoogleWeather_getAlternateWeatherLocations() returns a Vector
 *
 *  To remove an alternate location by removing its sageweather cache properties file:
 * sage_google_weather_GoogleWeather_removeLocation(Weather) returns boolean
 *
 *
 *  The location for Google Weather can be set using:
 * 		(can be a zip code, postal code, city,state, city,country, etc.)
 * sage_google_weather_GoogleWeather_setGoogleWeatherLoc(Weather, "CityName")
 *
 *	To clear the Google Weather location:
 * sage_google_weather_GoogleWeather_removeGoogleWeatherLoc(Weather)
 *
 *
 *  The Zip Code location for NWS Weather can be set using:
 *		(must be a zip code in the US)
 * sage_google_weather_GoogleWeather_setNWSZipCode(Weather, "01234")
 *
 *	To clear the NWS Weather zip code location:
 * sage_google_weather_GoogleWeather_removeNWSZipCode(Weather)
 *
 *
 *  The the units can be set using:
 *		(mus be "s" or "m", for standard/english or metric, respectively; default: "s") 
 * sage_google_weather_GoogleWeather_setUnits(Weather, "s")
 *
 *
 *  To cause an update to occur (it respects caching, returns a boolean):
 * sage_google_weather_GoogleWeather_updateAllNow(Weather)
 *
 *		To use an ISO 639-1 Language Code with Google sageweather:
 * sage_google_weather_GoogleWeather_updateAllNow(Weather,"LangCode")
 *
 *		To update only Google sageweather (with or w/o a lang code):
 * sage_google_weather_GoogleWeather_updateGoogleNow(Weather)
 * sage_google_weather_GoogleWeather_updateGoogleNow(Weather,"LangCode")
 *
 *		To update only NWS sageweather (with or w/o a lang code):
 * sage_google_weather_GoogleWeather_updateNWSNow(Weather)
 *
 *
 * 	To Get more info:
 * sage_google_weather_GoogleWeather_getGoogleWeatherLoc(Weather) returns String
 * sage_google_weather_GoogleWeather_getNWSZipCode(Weather) returns String
 * sage_google_weather_GoogleWeather_getUnits(Weather) returns String, "s" (default) or "m"
 *
 * sage_google_weather_GoogleWeather_isCurrentlyUpdatingAll(Weather)
 * sage_google_weather_GoogleWeather_isCurrentlyUpdatingGW(Weather)
 * sage_google_weather_GoogleWeather_isCurrentlyUpdatingNWS(Weather)
 *
 * sage_google_weather_GoogleWeather_getLastError(Weather) return String
 *
 * sage_google_weather_GoogleWeather_getLastUpdateTime(Weather) returns long
 * sage_google_weather_GoogleWeather_getLastUpdateTimeGW(Weather) returns long
 * sage_google_weather_GoogleWeather_getLastUpdateTimeNWS(Weather) returns long
 *
 *
 *	Get Google sageweather info:
 *
 * sage_google_weather_GoogleWeather_getGWCityName(Weather) return String
 *		Get current condition for properties:
 *			CondText
 *			HumidText
 *			Temp
 *			iconURL
 *			WindText
 * sage_google_weather_GoogleWeather_getGWCurrentCondition(Weather,propName) return String
 * sage_google_weather_GoogleWeather_getGWCurConditionProperties(Weather) returns a Map
 * sage_google_weather_GoogleWeather_getGWDayCount(Weather) return int
 *		Get forecast condition for properties on day #:
 *	 		CondText
 *	 		high
 *	 		iconURL
 *	 		low
 *	 		name
 * sage_google_weather_GoogleWeather_getGWForecastCondition(Weather,dayNum,propName) return String
 * sage_google_weather_GoogleWeather_getGWForecastConditionProperties(Weather,dayNum) returns a Map
 *
 *
 *	Get NWS sageweather info:
 *
 * sage_google_weather_GoogleWeather_getNWSCityName(Weather) return String
 * sage_google_weather_GoogleWeather_getNWSPeriodCount(Weather) return int
 *		Get forecast condition for properties in 12h period #:
 *			forecast_text
 *			icon_url
 *			name
 *			precip
 *			summary
 *			temp
 *			tempType - returns h or l
 * sage_google_weather_GoogleWeather_getNWSForecastCondition(Weather,periodNum,propName) return String
 * sage_google_weather_GoogleWeather_getNWSForecastConditionProperties(Weather,periodNum) returns a Map
 *
 */
package sage.google.weather;

import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class GoogleWeather 
{
    public static String GW_Weather_url = "http://www.google.com/ig/api?sageweather=";
    public static String GW_Lang_Prefix = "&hl=";

    public static String NWS_ZipCode_url = "http://graphical.sageweather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php?listZipCodeList=";
    public static String NWS_Weather_url = "http://forecast.sageweather.gov/MapClick.php?";
    public static String NWS_Lat_Prefix = "lat=";
    public static String NWS_Long_Prefix = "&lon=";
    public static String NWS_Unit_Prefix = "&unit=";
    public static String NWS_Weather_Postfix = "&FcstType=dwml";


	private static String propFilePrefix = "google_weather_cache";
    
	private static String weatherVersion = "2.0.2";

	//private static GoogleWeather myInstance;
	private static java.util.HashMap myInstanceMap = new java.util.HashMap();

	public static GoogleWeather getInstance()
	{
		return getInstance("");
	}
	public static GoogleWeather getInstance( String altLoc )
	{
		// First, make sure the string contains valid filename chars.

		GoogleWeather instance = null;
		/*
		if ( myInstanceMap.containsKey(altLoc) )
		{
			instance = (GoogleWeather) myInstanceMap.get(altLoc);
			System.out.println( "For location '" + altLoc + "', found instance= " + instance );
		}

		if (instance == null)
		{
			instance = new GoogleWeather(altLoc);
			myInstanceMap.put( altLoc, instance );
		}
		System.out.println( "instance for '" + altLoc + "' = " + instance );
		*/
		return instance;
/*
		if (myInstance == null)
			myInstance = new GoogleWeather();
		System.out.println( "myInstance = " + myInstance );
		return myInstance;
*/
	}

	protected GoogleWeather()
	{
		this("");
	}

	protected GoogleWeather( String altLoc )
	{
		myAltLoc = altLoc;
		loadWeatherDataFromCache();
	}

	public static String getWeatherVersion()
	{
		return weatherVersion;
	}

	public static java.util.Vector getAllWeatherLocations()
	{
		java.util.Vector altLocList = getAlternateWeatherLocations();
		java.util.Vector allLocList = new java.util.Vector();
		allLocList.add( "" );
		allLocList.addAll( altLocList );

		System.out.println( "Found " + allLocList.size() + " total sageweather locations: " + allLocList.toString() );

		return allLocList;
	}
	public static java.util.Vector getAlternateWeatherLocations()
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
		
		// Loop through list of sageweather cache properties files & create list of alternate locations.
		java.util.Vector locList = new java.util.Vector();
		int prefixSize = propFilePrefix.length();
		for ( int i = 0; i < propFiles.length; i++ )
		{
			String fileName = propFiles[i].getName();

			if (fileName.toLowerCase().startsWith(propFilePrefix))
			{
				int dotPos = fileName.indexOf( "." );
				String locName = fileName.substring( prefixSize, dotPos );
				
				// If there is a location name, remove the starting '_'
				if (locName.length() > 1 ) 
					locName = locName.substring( 1 );
				
				// If there is still a location name, add it to the list. This skips the main "" location.
				if (locName.length() > 1 ) 
					locList.add( locName );
					
			}
		}

		System.out.println( "Found " + locList.size() + " alternate sageweather locations: " + locList.toString() );

		return locList;
	}

	// Remove this instance's location by getting rid of its properties file.
	public boolean removeLocation()
	{
		boolean result = false;

		// Wait until nothing is currently updating.
		while (isCurrentlyUpdatingAll() || isCurrentlyUpdatingGW() || isCurrentlyUpdatingNWS())
		{
        	try { Thread.sleep(100l); } catch (InterruptedException e) {}
		}

		// No update in progress, so remove properties file.
		java.io.File cacheFile = new java.io.File(propFilePrefix + (myAltLoc.length() > 0 ? "_" + myAltLoc : "") + ".properties");
		System.out.println("Google Weather trying to delete locale cache: " + cacheFile );
		try {
			result = cacheFile.delete();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error deleting sageweather cache file '" + cacheFile + "':" + e));
			return false;
		}

		// Remove myAltLoc from list of instances.
		myInstanceMap.remove(myAltLoc);

		return result;
	}
	


	public void setGoogleWeatherLoc(String newGWLoc)
	{
		if (newGWLoc != null && !newGWLoc.equals(myGWLoc))
		{
			// Blow away the Google Weather cache
			lastGWUpdateTime = 0;
		}

		props.remove("GW/city");
		myGWLoc = newGWLoc;

		saveWeatherDataToCache();
	}
	public String getGoogleWeatherLoc()
	{
		return myGWLoc;
	}
	public void removeGoogleWeatherLoc()
	{
		// Blow away the Google Weather cache
		lastGWUpdateTime = 0;

		// Clear the Google location.
		props.remove("GW/city");
		myGWLoc = "";

		saveWeatherDataToCache();
	}


	public boolean setNWSZipCode(String zipCode)
	{
		// Don't try using an empty zip code value.
		if (zipCode == null || zipCode.length() == 0)
			return false;

		// If the zip code hasn't changed, it is already set.
		if (zipCode.equals(myNWSZipCode))
			return true;

		// Have a new zip code, so try finding its latitude,longitude values.
		try
		{
			String ZipCodeURLString = NWS_ZipCode_url + java.net.URLEncoder.encode(zipCode, "UTF-8");

			NWSZipCodeHandler zipHandler = new NWSZipCodeHandler();
			parseXmlFile(ZipCodeURLString, false, zipHandler);
			String[] latLong;
			latLong = zipHandler.getZipCodeLatLong();
			   
	  		System.out.println( "After zip code search, lat/long values are:");
	  		System.out.println( "latLong[0]=" + latLong[0] );
	  		System.out.println( "latLong[1]=" + latLong[1] );

			if (latLong == null)
			{
				System.out.println(lastError = "NWS returned empty Zip Code search doc");
				return false;
			}

			if (latLong[0].length() == 0 || latLong[1].length() == 0)
			{
				System.out.println(lastError = "NWS returned empty Lat/Long for Zip Code search");
				return false;
			}

			// Blow away the NWS cache, set new NWS location values, and save properties.
			lastNWSUpdateTime = 0;

			props.remove("NWS/city_name");
			
			myNWSZipCode = zipCode;
			myNWSLatitude  = latLong[0];
			myNWSLongitude = latLong[1];

			saveWeatherDataToCache();
			return true;
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with sageweather parsing:" + e));
			return false;
		}
	}
	public String getNWSZipCode()
	{
		return myNWSZipCode;
	}
	public void removeNWSZipCode()
	{
		// Blow away the NWS cache & remove NWS location values.
		lastNWSUpdateTime = 0;

		props.remove("NWS/city_name");
		
		myNWSZipCode = "";
		myNWSLatitude  = "";
		myNWSLongitude = "";

		saveWeatherDataToCache();
	}

	// Set the units to either "s" (US/English) "m" (metric)
	public void setUnits(String units)
	{
		if (units != null && !units.equals(myUnits))
		{
			// Blow away the cache
			lastGWUpdateTime = lastNWSUpdateTime = 0;
		}

		myUnits = units;
	}
	public String getUnits()
	{
		return myUnits;
	}


	public boolean updateAllNow()
	{
		return updateAllNow("");
	}
	public boolean updateAllNow(String lang)
	{
		boolean resultGW = false; 
		boolean resultNWS = false;
		 
		if (updating) return true;
		
		updating = true;
		resultGW = updateGoogleNow(lang); 
		resultNWS = updateNWSNow(); 
		saveWeatherDataToCache();

		updating = false;
		return resultGW && resultNWS;
	}
	public boolean isCurrentlyUpdatingAll()
	{
		return updating;
	}

	public boolean updateGoogleNow()
	{
		return updateGoogleNow("");
	}
	public boolean updateGoogleNow(String lang)
	{
		if (updatingGW) return true;

		if (myGWLoc == null || myGWLoc.length() == 0)
		{ 
			System.out.println( "   Google sageweather has no location set yet." );
			if (!isCurrentlyUpdatingAll()) 
				saveWeatherDataToCache();
			return false;
		}

		try
		{
			updatingGW = true;

			lastError = "";
			boolean updateGW = System.currentTimeMillis() - lastGWUpdateTime > 30*60*1000; // 30 minutes
			if (!updateGW)
			{
				System.out.println( "   Not time to update Google sageweather yet." );
				return true; // nothing to update right now, use the cache
			}
			
			String urlString = GW_Weather_url + java.net.URLEncoder.encode(myGWLoc, "UTF-8");
			if (lang != null && lang.length() > 0)
				urlString += GW_Lang_Prefix + lang;

			System.out.println( "   Updating Google sageweather from:" + urlString );

			parseXmlFile(urlString, false, new GWUpdateHandler(), "ISO-8859-2");


			if (!isCurrentlyUpdatingAll()) 
				saveWeatherDataToCache();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with Google sageweather parsing:" + e));
			e.printStackTrace();
			return false;
		}
		finally
		{
			updatingGW = false;
		}

		return true;
	}
	public boolean isCurrentlyUpdatingGW()
	{
		return updatingGW;
	}

	public boolean updateNWSNow()
	{
		if (updatingNWS) return true;

		if (myNWSLatitude == null || myNWSLatitude.length() == 0)
		{
			System.out.println( "   NWS sageweather has no lat/long set yet." );
			if (!isCurrentlyUpdatingAll()) 
				saveWeatherDataToCache();
		 	return false;
		}

		try
		{
			updatingNWS = true;

			lastError = "";
			boolean updateNWS = System.currentTimeMillis() - lastNWSUpdateTime > 1*60*60*1000; // 1 hour
			if (!updateNWS)
			{
				System.out.println( "   Not time to update NWS sageweather yet." );
				return true; // nothing to update right now, use the cache
			}

			String urlString = NWS_Weather_url + NWS_Lat_Prefix + myNWSLatitude +
												 NWS_Long_Prefix + myNWSLongitude;

			if ( myUnits.equalsIgnoreCase("s") )
				urlString += NWS_Unit_Prefix + 0;
			else if ( myUnits.equalsIgnoreCase("m") )
				urlString += NWS_Unit_Prefix + 1;

			urlString += NWS_Weather_Postfix;
			
			System.out.println( "   Updating NWS sageweather from:" + urlString );

			parseXmlFile(urlString, false, new NWSUpdateHandler());


			if (!isCurrentlyUpdatingAll()) 
				saveWeatherDataToCache();
		}
		catch (Exception e)
		{
			System.out.println(lastError = ("Error with NWS sageweather parsing:" + e));
			e.printStackTrace();
			return false;
		}
		finally
		{
			updatingNWS = false;
		}
		return true;
	}
	public boolean isCurrentlyUpdatingNWS()
	{
		return updatingNWS;
	}

	public String getLastError()
	{
		return lastError;
	}

	public long getLastUpdateTime()
	{
		return Math.max(lastGWUpdateTime, lastNWSUpdateTime);
	}
	public long getLastUpdateTimeGW()
	{
		return lastGWUpdateTime;
	}
	public long getLastUpdateTimeNWS()
	{
		return lastNWSUpdateTime;
	}


	////////////////////////////////////////////////////////
	//
	// Get values for Google Weather
	public String getGWCityName()
	{
		return props.getProperty("GW/city");
	}
	// Get current condition for properties:
	// 		CondText
	// 		HumidText
	// 		Temp
	// 		WindText
	public String getGWCurrentCondition(String propName)
	{
		return props.getProperty("GW/cur/" + propName);
	}
	public java.util.Map getGWCurConditionProperties()
	{
		return getPropertiesWithPrefix("GW/cur/");
	}
	
	public int getGWDayCount()
	{
		return Integer.parseInt( props.getProperty("GW/day/count") );
	}
	// Get forecast condition for properties on day #:
	// 		CondText
	// 		high
	// 		iconURL
	// 		low
	// 		name
	public String getGWForecastCondition(int dayNum, String propName)
	{
		return props.getProperty("GW/day/" + Integer.toString(dayNum) + "/" + propName);
	}
	public java.util.Map getGWForecastConditionProperties(int dayNum)
	{
		return getPropertiesWithPrefix("GW/day/" + Integer.toString(dayNum) + "/");
	}

	////////////////////////////////////////////////////////
	//
	// Get values for NWS sageweather
	public String getNWSCityName()
	{
		return props.getProperty("NWS/city_name");
	}
	public int getNWSPeriodCount()
	{
		return Integer.parseInt( props.getProperty("NWS/period/count") );
	}
	// Get forecast condition for properties in 12h period #:
	// 		forecast_text
	// 		icon_url
	// 		name
	// 		precip
	// 		summary
	// 		temp
	// 		tempType - h or l
	public String getNWSForecastCondition(int periodNum, String propName)
	{
		return props.getProperty("NWS/period/" + Integer.toString(periodNum) + "/" + propName);
	}
	public java.util.Map getNWSForecastConditionProperties(int periodNum)
	{
		return getPropertiesWithPrefix("NWS/period/" + Integer.toString(periodNum) + "/");
	}


	// general routines
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
		props.put("lastGWUpdateTime", Long.toString(lastGWUpdateTime));
		props.put("lastNWSUpdateTime", Long.toString(lastNWSUpdateTime));


		if (myLocId != null)
			props.put("locID", myLocId);

		// Save property values for Google Weather 
		if (myGWLoc != null)
			props.put("GW/loc", myGWLoc);

		// Save property values for NWS Weather 
		if (myNWSZipCode != null)
			props.put("NWS/ZipCode", myNWSZipCode);
		if (myNWSLatitude != null)
			props.put("NWS/Latitude", myNWSLatitude);
		if (myNWSLongitude != null)
			props.put("NWS/Longitude", myNWSLongitude);

		if (myUnits != null)
			props.put("units", myUnits);

		

		java.io.File cacheFile = new java.io.File(propFilePrefix + (myAltLoc.length() > 0 ? "_" + myAltLoc : "") + ".properties");
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

		java.io.File cacheFile = new java.io.File(propFilePrefix + (myAltLoc.length() > 0 ? "_" + myAltLoc : "") + ".properties");
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
			lastGWUpdateTime = Long.parseLong(props.getProperty("lastGWUpdateTime", "0"));
			lastNWSUpdateTime = Long.parseLong(props.getProperty("lastNWSUpdateTime", "0"));
		}catch (NumberFormatException e){}
		myLocId = props.getProperty("locID");
		myUnits = props.getProperty("units", "s");

		myGWLoc = props.getProperty("GW/loc");

		myNWSZipCode   = props.getProperty("NWS/ZipCode");
		myNWSLatitude  = props.getProperty("NWS/Latitude");
		myNWSLongitude = props.getProperty("NWS/Longitude");

	}


	
	private String myAltLoc;

	private String myGWLoc;

	private String myNWSZipCode;
	private String myNWSLatitude;
	private String myNWSLongitude;


	private String myLocId;
	private String myUnits = "s";
	//private java.util.Properties props = new java.util.Properties();
	private SortedWeatherProperties props = new SortedWeatherProperties();
	
	private long lastGWUpdateTime;
	private long lastNWSUpdateTime;
	private boolean updating = false;
	private boolean updatingGW = false;
	private boolean updatingNWS = false;
	private String lastError;
    
    
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


	private class NWSZipCodeHandler extends DefaultHandler
	{
		private StringBuffer buff = new StringBuffer();
		private String current_tag;
		private boolean inLatLong = false;
		String[] latLong;


		public String[] getZipCodeLatLong()
		{
			return latLong;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if ("latLonList".equalsIgnoreCase(qName))
			{
				latLong = new String[2];
				inLatLong = true;
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

			if ("err".equalsIgnoreCase(qName))
			{
				System.out.println(lastError = ("NWS zip code search returned error status " + data));
			}
			else if ("latLonList".equalsIgnoreCase(qName) && latLong != null && inLatLong)
			{
				String[] latLongPairList = data.split(" ");
				String[] latLongPair = latLongPairList[0].split(",");
				latLong = latLongPair;

				inLatLong = false;
			}
		}
	}


	private class NWSUpdateHandler extends DefaultHandler
	{
		private String current_tag;
		private StringBuffer buff = new StringBuffer();

		private boolean foundWeather = false;
		private boolean inData = false;
		private boolean inLocation = false;
		private boolean inTimeLayout = false;
		private boolean inParameters = false;
		private boolean inTemperature= false;
		private boolean inProbOfPrecip = false;
		private boolean inSummary = false;
		private boolean inIcons = false;
		private boolean inForecastText= false;

		private boolean in12hPeriod = false;
		private String PeriodKey12h = "";
		private int Num12hPeriods = 0; 
		private int CurPeriodNum = 0;

		private boolean in24hPeriod1 = false;
		private boolean in24hPeriod2 = false;
		private String[] PeriodKey24h = { "", "" }; 
		private String[] PeriodType24h = { "", "" };	// 24h Period type is "h" or "l" for high/low temps.  
		private int[] Num24hPeriods = { 0, 0 };
		private int Cur24hPeriodIndex = 0; 
		 
		

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{

			//System.out.println( "NWS startElement=" + qName );
			//System.out.println( "NWS   attributes length=" + attributes.getLength() );

			if ("dwml".equals(qName))
				foundWeather = true;
			else if ("data".equals(qName))
				inData = true;
			else if ("location".equals(qName))
				inLocation = true;
			else if ("time-layout".equals(qName))
				inTimeLayout = true;
			else if (in12hPeriod)
			{
				if ("start-valid-time".equals(qName))
				{
					String PeriodName = attributes.getValue("period-name");
					props.put("NWS/period/" + CurPeriodNum + "/name", PeriodName);
					CurPeriodNum++;
				}
			}
			else if ("parameters".equals(qName))
				inParameters = true;
			else if ("temperature".equals(qName))
			{
				inTemperature = true;

				// Is this set of temps for the 1st or 2nd 24h period sets?
				String TempKey = attributes.getValue("time-layout");
				if (TempKey.equals(PeriodKey24h[0]))
				{
					Cur24hPeriodIndex = 0;
					CurPeriodNum = 0;		// 1st 24h period, so store values starting at period 0, + 2 afterwards
				}
				else if (TempKey.equals(PeriodKey24h[1]))
				{
					Cur24hPeriodIndex = 1;
					CurPeriodNum = 1;		// 2nd 24h period, so store values starting at period 1, + 2 afterwards
				}
				else
					inTemperature = false;
					
				// Is this for high or low temps?
				if (inTemperature)
				{
					String TempType = attributes.getValue("type");
					if (TempType.equalsIgnoreCase("maximum"))
						PeriodType24h[Cur24hPeriodIndex] = "h"; 
					else if (TempType.equalsIgnoreCase("minimum"))
						PeriodType24h[Cur24hPeriodIndex] = "l"; 
					else
						inTemperature = false;
				}
			}
			else if ("probability-of-precipitation".equals(qName))
			{
				inProbOfPrecip = true;
				CurPeriodNum = 0;
			}
			else if ("sageweather".equals(qName))
			{
				inSummary = true;
				CurPeriodNum = 0;
			}
			else if (inSummary)
			{
				if ("sageweather-conditions".equals(qName))
				{
					String SummaryText = attributes.getValue("sageweather-summary");
					props.put("NWS/period/" + CurPeriodNum + "/summary", SummaryText);
					CurPeriodNum++;
				}
			}
			else if ("conditions-icon".equals(qName))
			{
				inIcons = true;
				CurPeriodNum = 0;
			}
			else if ("wordedForecast".equals(qName))
			{
				inForecastText = true;
				CurPeriodNum = 0;
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

			//System.out.println( "NWS  endElement=" + qName );
			//System.out.println( "NWS   data=[" + data + "]" );
			//System.out.println( "NWS   uri=" + uri );
			//System.out.println( "NWS   localName=" + localName );

			if (!foundWeather) return;

			if (inData)
			{
				if ("data".equals(qName))
				{
					inData = false;
					lastNWSUpdateTime = System.currentTimeMillis();
				}
				else if (inLocation)
				{
					if ("location".equals(qName))
						inLocation = false;
					else if ("city".equals(qName))
						props.put("NWS/city_name", data);
				}
				else if (inTimeLayout)
				{
					// Need to find certain time layout keys.
					if ("time-layout".equals(qName))
					{
						inTimeLayout = in12hPeriod = in24hPeriod1 = in24hPeriod2 = false;
					}
					else if ("layout-key".equals(qName))
					{
						String key = data;
						String[] keyList = key.split("-");
						if (keyList[1].equalsIgnoreCase("p12h"))
						{
							String periods = keyList[2].substring(1); 
							int numPeriods = Integer.parseInt(periods);
							//System.out.println( "	Found 12h key=" + key );
							//System.out.println( "	12h periods=" + numPeriods );

							in12hPeriod = true;
							PeriodKey12h = key;
							Num12hPeriods = numPeriods;
							props.put("NWS/period/count", Integer.toString(Num12hPeriods) ); 

						}
						else if (keyList[1].equalsIgnoreCase("p24h"))
						{
							String periods = keyList[2].substring(1); 
							int numPeriods = Integer.parseInt(periods); 
							//System.out.println( "	Found 24h key=" + key );
							//System.out.println( "	24h periods=" + numPeriods );

							if (PeriodKey24h[0].length() == 0)
							{
								// This is the first 24h time period layout: either days or nights; determined later.
								in24hPeriod1 = true;
								CurPeriodNum = 0;
								PeriodKey24h[0] = key;
								Num24hPeriods[0] = numPeriods;
							}
							else
							{
								// This is the second 24h time period layout: either nights or days; determined later.
								in24hPeriod2 = true;
								PeriodKey24h[1] = key;
								Num24hPeriods[1] = numPeriods;
							} 
						}
					}

				}
				else if (inParameters)
				{
					if ("parameters".equals(qName))
						inParameters = false;
					else if (inTemperature)
					{
						if ("temperature".equals(qName))
							inTemperature = false;
						else if ("value".equals(qName))
						{
							// data is the forecast temp for this period.
							props.put("NWS/period/" + CurPeriodNum + "/temp", data);
							props.put("NWS/period/" + CurPeriodNum + "/tempType", PeriodType24h[Cur24hPeriodIndex]);
							CurPeriodNum += 2;
						}
					}
					else if (inProbOfPrecip)
					{
						if ("probability-of-precipitation".equals(qName))
							inProbOfPrecip = false;
						else if ("value".equals(qName))
						{
							// If data is null, then there is no chance of precipitation.
							String percent;
							if (data == null || data.length() == 0)
								percent = "0";
							else 
								percent = data;

							props.put("NWS/period/" + CurPeriodNum + "/precip", percent);
							CurPeriodNum++;
						}
					}
					else if ("sageweather".equals(qName))
					{
						inSummary = false;
					}
					else if (inIcons)
					{
						if ("conditions-icon".equals(qName))
							inIcons = false;
						else if ("icon-link".equals(qName))
						{
							props.put("NWS/period/" + CurPeriodNum + "/icon_url", data);
							CurPeriodNum++;
						}
					}
					else if (inForecastText)
					{
						if ("wordedForecast".equals(qName))
							inForecastText = false;
						else if ("text".equals(qName))
						{
							props.put("NWS/period/" + CurPeriodNum + "/forecast_text", data);
							CurPeriodNum++;
						}
					}

				}
			}

		}
		public void endDocument() throws SAXException
		{
			if (!foundWeather)
				System.out.println(lastError = ("NWS Weather returned empty doc"));
		}
	}

	private class GWUpdateHandler extends DefaultHandler
	{
		private String current_tag;
		private StringBuffer buff = new StringBuffer();

		private boolean foundWeather = false;
		private boolean inForecastInfo = false;
		private boolean inCurConditions = false;
		private boolean inForecastConditions = false;

        private int CurForecastDayNum = 0;
        private String forecastUnits = "s";
        private boolean NeedTempConversion = false;


        private int convertFtoC(int temp) 
        {
            return (temp - 32) * 5 / 9;
        }

        private int convertCtoF(int temp) 
        {
            return (temp * 9 / 5) + 32;
        }


		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{

			//System.out.println( "GW startElement=" + qName );
			//System.out.println( "GW   attributes length=" + attributes.getLength() );
			//System.out.println( "GW   attributes data=" + attributes.getValue("data") );

			if ("sageweather".equals(qName))
				foundWeather = true;
			else if ("forecast_information".equals(qName))
				inForecastInfo = true;
			else if (inForecastInfo)
			{
			    if ("city".equals(qName))
                {
					//System.out.println( "GW city=" + attributes.getValue("data") );
                    String CityName = attributes.getValue("data");
                    props.put("GW/city", CityName);
					//System.out.println( "GW set city=" + attributes.getValue("data") );
                }
			    else if ("unit_system".equals(qName))
                {
                    String UnitType = attributes.getValue("data");
                    
			        if ( UnitType.equalsIgnoreCase("US") )
                                    forecastUnits = "s";
			        else if ( UnitType.equalsIgnoreCase("SI") )
                                    forecastUnits = "m";
                    
                    if ( !myUnits.equalsIgnoreCase(forecastUnits) )    
                        NeedTempConversion = true;
        
                    props.put("GW/units", UnitType);
                }
            }
			else if ("current_conditions".equals(qName))
				inCurConditions = true;
			else if (inCurConditions)
			{
			    if ("condition".equals(qName))
                {
                    String CondText = attributes.getValue("data"); 
                    props.put("GW/cur/CondText", CondText);
                }
			    else if ("temp_f".equals(qName) && myUnits.equalsIgnoreCase("s"))
                {
                    int curTemp = Integer.parseInt( attributes.getValue("data") ); 
                    props.put("GW/cur/Temp", Integer.toString(curTemp) );
                }
                else if ("temp_c".equals(qName) && myUnits.equalsIgnoreCase("m"))
                {
                    int curTemp = Integer.parseInt( attributes.getValue("data") ); 
                    props.put("GW/cur/Temp", Integer.toString(curTemp) );
                }
			    else if ("humidity".equals(qName))
                {
                    String HumidText = attributes.getValue("data");
                    props.put("GW/cur/HumidText", HumidText);
                }    
			    else if ("icon".equals(qName))
                {
                    String iconURL = "http://www.google.com" + attributes.getValue("data");
                    props.put("GW/cur/iconURL", iconURL);
                }    
			    else if ("wind_condition".equals(qName))
                {
                    String WindText = attributes.getValue("data");
                    props.put("GW/cur/WindText", WindText);
                }    
            }
			else if ("forecast_conditions".equals(qName))
				inForecastConditions = true;
			else if (inForecastConditions)
			{
                if ("day_of_week".equals(qName))
                {
                    String DayName = attributes.getValue("data");
                    props.put("GW/day/" + CurForecastDayNum + "/name", DayName); 
                }
                else if ("low".equals(qName))
                {
                    int temp = Integer.parseInt( attributes.getValue("data") );
                    if ( NeedTempConversion )
                    {
                        if (forecastUnits.equalsIgnoreCase("s"))
                            temp = convertFtoC(temp);
                        else     
                            temp = convertCtoF(temp);
                    }
                    props.put("GW/day/" + CurForecastDayNum + "/low", Integer.toString(temp) );
                }
                else if ("high".equals(qName))
                {
                    int temp = Integer.parseInt( attributes.getValue("data") );
                    if ( NeedTempConversion )
                    {
                        if (forecastUnits.equalsIgnoreCase("s"))
                            temp = convertFtoC(temp);
                        else     
                            temp = convertCtoF(temp);
                    }
                    props.put("GW/day/" + CurForecastDayNum + "/high", Integer.toString(temp) );
                }
                else if ("icon".equals(qName))
                {
                    String iconURL = "http://www.google.com" + attributes.getValue("data");
                    props.put("GW/day/" + CurForecastDayNum + "/iconURL", iconURL);
                }
                else if ("condition".equals(qName))
                {
                    String CondText = attributes.getValue("data"); 
                    props.put("GW/day/" + CurForecastDayNum + "/CondText", CondText);
                }
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

			//System.out.println( "GW  endElement=" + qName );
			//System.out.println( "GW   data=[" + data + "]" );
			//System.out.println( "GW   uri=" + uri );
			//System.out.println( "GW   localName=" + localName );

			if (!foundWeather) return;
            
			if ("forecast_information".equals(qName))
			{
				inForecastInfo = false;
				lastGWUpdateTime = System.currentTimeMillis();
			}
			else if ("current_conditions".equals(qName))
				inCurConditions = false;
			else if ("forecast_conditions".equals(qName))
            {
				inForecastConditions = false;
                CurForecastDayNum++;
				props.put("GW/day/count", Integer.toString(CurForecastDayNum) );
            }    
            


		}
		public void endDocument() throws SAXException
		{
			if (!foundWeather)
				System.out.println(lastError = ("Google Weather returned empty doc"));
		}
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
