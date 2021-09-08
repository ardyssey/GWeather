package sageweather;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class OWMWeatherLocation implements IWeatherLocation {

    private long lastChecked = 0;
    private Date lastUpdated = null;
    private Date recordedDate = null;
    private int ttl = 180;
    private boolean locationRetrieved = false;
    private boolean disallowUpdates = false;
    private boolean hasWeather = false;

    private String error;

    private geoLocation gLocation = new geoLocation();

    private String locationID;

    private IForecastPeriod currentForecast;
    private List<ILongRangeForecast> longRangeForecast;

    /**
     * Create a weather location for a known locationID
     * Loads location details from properties
     */
    public OWMWeatherLocation() {
        error = "No location set yet";
    }

    /**
     * Create a weather location for a known locationID
     * Loads location details from properties
     */
    public OWMWeatherLocation(String inLocationID) {
        error = null;
        loadConfig(inLocationID);
    }

    /**
     * Create a weather location for a lat/long pair
     * Saves location details to properties
     */
    public OWMWeatherLocation(geoLocation inGeoLocation ) {
        this(inGeoLocation.getName(),inGeoLocation.getCountry(),inGeoLocation.getLatitude(),inGeoLocation.getLongitude(),inGeoLocation.getUnits());
    }
    public OWMWeatherLocation(String inLocationName, String inCountry, Double inLatitude, Double inLongitude, IWeatherLocation.Units inUnits ) {
        error = null;
        saveConfig(inLocationName, inCountry, inLatitude, inLongitude, inUnits);
    }

    @Override
    public String getSourceName() {
        return "OWM";
    }

    public boolean loadConfig(String locationID) {
        //read all location details from properties and set values
        //TODO: load location
        return false;
    }

    public boolean saveConfig(String locationName, String country, Double latitude, Double longitude,IWeatherLocation.Units units  ) {
        //start by assuming we do NOT have a valid location
        locationRetrieved = false;
        if (latitude==null || longitude==null){
            Log.info("OWMWeatherLocation","OWM: saveConfig failed as lat/long were invalid or null");
            return false;
        }
        if (locationName==null || locationName.isEmpty() ){
            locationName = "Unknown";
        }
        if (country==null || country.isEmpty() ){
            locationName = "";
        }
        //create the geoLocation
        gLocation.setName(locationName);
        gLocation.setCountry(country);
        gLocation.setLatitude(latitude);
        gLocation.setLongitude(longitude);
        gLocation.setUnits(units);

        //get locationID from lat/long
        locationID = String.valueOf(utils.getUniqueId(latitude,longitude));

        //save all location details to properties and set values
        //TODO: save location config

        return true;
    }

    @Override
    public boolean update() {
        error = null;
        setLastChecked(System.currentTimeMillis());

        //make sure updates are allowed if a userKey has been entered in the sageTV config
        if (hasUserKey()){
            disallowUpdates = false;
        }

        if (!disallowUpdates){
            if (!isConfigured()) {
                Log.info("OWMWeatherLocation","OWM: update called but location is not configured - exiting");
                error = "Latitude and Longitude or a valid location are required";
                return false;
            }
            Log.info("OWMWeatherLocation","OWM: update - configuration is valid");

            if (shouldUpdate()) {
                String units = null;

                if (getUnits() == Units.Metric) {
                    units = "metric";
                } else {
                    units = "imperial";
                }

                try {

                    String urlLocation = "lat=" + gLocation.getLatitude() + "&lon=" + gLocation.getLongitude();
                    //String urlUnits = "&units=" + utils.encode(units);
                    String urlUnits = "&units=" + units;

                    //check for user key from the weather config or OWM.properties
                    //if not found then do not allow more than 1 update
                    String urlExclude = "&exclude=hourly,alerts,minutely";
                    String urlAPIKey = "&appid=" + utils.getAPIKey();
                    //String rssUrl = "https://api.darksky.net/forecast/" + utils.encode(k) + "/" + urlLocation + "?" + urlUnits;
                    String rssUrl = "https://api.openweathermap.org/data/2.5/onecall?" + urlLocation + urlExclude + urlUnits + urlAPIKey;
                    Log.info("OWMWeatherLocation","OWM: update - Getting OWM Weather - url = '" + rssUrl + "'");

                    OWMWeatherJsonHandler handler = new OWMWeatherJsonHandler();
                    //handler.parse(rssUrl, city, region, country);
                    handler.parse(rssUrl,getUnits());
                    hasWeather = true;

                    lastUpdated = new Date(System.currentTimeMillis());

                    currentForecast = handler.getCurrent();
                    longRangeForecast = handler.getDays();
                    recordedDate = handler.getRecordedDate();

                    //see if we are using a userkey - otherwise do not allow further updates
                    if (!hasUserKey()){
                        if (utils.isTesting()){
                            Log.info("OWMWeatherLocation","OWM: update - user key override for testing set - update processed");
                            disallowUpdates = false;
                        }else{
                            Log.info("OWMWeatherLocation","OWM: update -  no user key provided - update not processed");
                            disallowUpdates = true;
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = "OWM: update - update failed";
                    Log.info("OWMWeatherLocation","OWM: update - Failed to update weather for " + gLocation.getLatitude() + "," + gLocation.getLongitude() + " : " + e);
                }
            }

        }


        return false;
    }

    @Override
    public geoLocation getLocation() {
        return gLocation;
    }

    @Override
    public String getLocationName() {
        return gLocation.getName();
    }

    @Override
    public Units getUnits() {
        return gLocation.getUnits();
    }

    @Override
    public void setUnits(Units units){
        disallowUpdates = false;
        lastUpdated=null;
        gLocation.setUnits(units);
    }

    @Override
    public IForecastPeriod getCurrentWeather() {
        return currentForecast;
    }

    @Override
    public List<ILongRangeForecast> getForecasts() {
        return longRangeForecast;
    }

    @Override
    public int getForecastDays() {
        if (longRangeForecast != null) {
            return longRangeForecast.size();
        }
        return 0;
    }

    @Override
    public boolean isConfigured() {
        error = null;
        if (gLocation.isValid()){
            return true;
        }else{
            error = "Latitude and Longitude for a valid location are required";
        }
        return false;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    @Override
    public Date getRecordedDate() {
        return recordedDate;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public String getUserKey() {
        return utils.getAPIKey();
    }

    @Override
    public boolean hasUserKey(){
        return !StringUtils.isEmpty(this.getUserKey());
    }

    @Override
    public boolean needUserKey(){
        return true;
    }

    /**
     * Returns the Larger of the configured Updated Interval vs the Weather's TTL
     */
    private int getTTLInSeconds() {
        int ttl1=ttl * 60;
        int ttl2=utils.getUpdateInterval();
        return Math.max(ttl1,ttl2);
    }

    private boolean shouldUpdate() {
        if (lastUpdated == null){
            return true;
        }
        long later = lastUpdated.getTime() + (getTTLInSeconds() * 1000);
        if (System.currentTimeMillis() > later)
            return true;
        Log.info("OWMWeatherLocation","OWM: shouldUpdate - Not time to perform an update. Last update at '" + lastUpdated + "'");
        return false;
    }

    public boolean hasWeather() {
        return hasWeather;
    }
}
