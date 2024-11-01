package sageweather;

import com.google.gson.*;

import java.io.IOException;

/**
 * Created by jusjoken on 7/16/2021.
 */
public class geoLocationSearch {

    /**
     * Used to configure the SearchType for a location search
     */
    public static enum SearchType {
        Name, Zip, Postal
    }

    private SearchType searchType;
    private String searchPhrase;
    private geoLocation.Country searchCountryCode;
    private IWeatherLocation.Units searchUnits;
    private WeatherLocations searchResults = new WeatherLocations();

    public geoLocationSearch(String searchPhrase, IWeatherLocation.Units searchUnits) {
        this(searchPhrase, geoLocation.Country.NONE,SearchType.Name, searchUnits);
    }

    public geoLocationSearch(String searchPhrase, geoLocation.Country searchCountryCode, IWeatherLocation.Units searchUnits) {
        this(searchPhrase,searchCountryCode,SearchType.Name, searchUnits);
    }

    public geoLocationSearch(String searchPhrase, geoLocation.Country searchCountryCode, SearchType searchType, IWeatherLocation.Units searchUnits ) {
        this.searchType = searchType;
        this.searchPhrase = searchPhrase;
        this.searchUnits = searchUnits;
        this.searchCountryCode = searchCountryCode;
    }

    //Name search -       http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}
    //Zip-Postal search - http://api.openweathermap.org/geo/1.0/zip?zip={zip code},{country code}&appid={API key}
    //Note - postal code search uses first 3 characters of entered postal code and must then have the 2 char country added

    public boolean search(){
        boolean foundResults = false;
        String urlBase = "http://api.openweathermap.org/geo/1.0/";
        String urlType;
        String urlSearchLoc;
        String urlLimit = "";
        String url;
        //build url

        if(searchType.equals(SearchType.Name)) {
            urlType = "direct?q=";
            urlSearchLoc = searchPhrase + formatCountryCode(searchCountryCode);
            urlLimit = "&limit=5";
        }else if(searchType.equals(SearchType.Postal)){
            urlType = "zip?zip=";
            //OWM only uses the first 3 characters of the postal so grab only that
            urlSearchLoc = searchPhrase.substring(0, Math.min(3, searchPhrase.length())) + formatCountryCode(searchCountryCode);
        }else{ //must be zip
            urlType = "zip?zip=";
            urlSearchLoc = searchPhrase + formatCountryCode(searchCountryCode);
        }
        url = urlBase + urlType + urlSearchLoc + urlLimit + "&appid=" + utils.getAPIKey();
        Log.info("geoLocationSearch","geoLocationSearch: search - Searching for locations - url = '" + url + "'");

        try {
            parse(url);
        } catch (IOException e) {
            Log.info("geoLocationSearch","OWM: geoLocationSearch - failed to parse details from url '" + url + "'");
        }
        if (searchResults.mapLocations.size()>0){
            foundResults = true;
        }

        return foundResults;
    }

    private void parse(String urlString) throws IOException, JsonIOException {
        //read the url and get the JSON results as a string
        String data = utils.getContentAsString(urlString);

        // Convert to a JSON object to get the elements
        JsonParser jp = new JsonParser(); //from gson
        JsonElement rootElement = jp.parse(data); //Convert the input stream to a json element
        //JsonObject root = rootElement.getAsJsonObject(); //May be an array, may be an object.
        if (rootElement == null) throw new IOException("JSON Response for 'root' Location did not contain a valid response");

        if(searchType.equals(SearchType.Name)){
            //first element is an array so need to handle each
            JsonArray locationArray = rootElement.getAsJsonArray();
            for (JsonElement itemElement: locationArray) {
                JsonObject item = itemElement.getAsJsonObject();
                geoLocation itemLoc = parseLocation(item);
                if(itemLoc.isValid()){
                    searchResults.add(itemLoc);
                }
            }
        }else{
            geoLocation itemLoc = parseLocation(rootElement.getAsJsonObject());
            if(itemLoc.isValid()){
                searchResults.add(itemLoc);
            }
        }


    }

    private geoLocation parseLocation(JsonObject item){
        Double latitude, longitude;
        String name, country;

        latitude = utils.GetJSONAsDouble("lat", item);
        longitude = utils.GetJSONAsDouble("lon", item);
        name = utils.GetJSONAsString("name", item);
        country = utils.GetJSONAsString("country", item);

        geoLocation newLoc = new geoLocation(name,country,latitude,longitude,searchUnits);
        return newLoc;
    }

    private String formatCountryCode(geoLocation.Country country){
        if (country==null || country.equals(geoLocation.Country.NONE)){
            return "";
        }else{
            return "," + country.getCountryCode();
        }
    }


    public WeatherLocations getSearchResults(){
        return searchResults;
    }

}
