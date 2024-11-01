package sageweather;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by jusjoken on 7/21/2021.
 * This is the public interface to use from SageTV
 */
public class OWM {
    public static String weatherLocKey = Const.BaseProp + Const.PropDivider + Const.WeatherLoc + Const.PropDivider;
    private WeatherLocations api = null;
    private geoLocation defaultLocation;
    private OWMWeatherLocation defaultWeather;

    private String searchPhrase;
    private geoLocation.Country searchCountryCode;
    private geoLocationSearch.SearchType searchType;
    private IWeatherLocation.Units searchUnits;
    private WeatherLocations searchResults;
    private boolean searchInProgress = false;

    private String propFileSuffix = "";
    public static WeatherProperties wProps;

    private static HashMap<Integer, Integer> dayCodeMap = new HashMap<Integer, Integer>();

    static {
        dayCodeMap.put(23, 24);//
        dayCodeMap.put(27, 28);//
        dayCodeMap.put(29, 30);//
        dayCodeMap.put(31, 32);//
        dayCodeMap.put(33, 34);//
        dayCodeMap.put(47, 37);//
        dayCodeMap.put(45, 39);//
        dayCodeMap.put(46, 41);//
    }

    private static HashMap<Integer, Integer> nightCodeMap = new HashMap<Integer, Integer>();

    static {
        nightCodeMap.put(24, 23);//
        nightCodeMap.put(28, 27);//
        nightCodeMap.put(30, 29);//
        nightCodeMap.put(32, 31);//
        nightCodeMap.put(34, 33);//
        nightCodeMap.put(36, 31);//
        nightCodeMap.put(37, 47);//
        nightCodeMap.put(38, 47);//
        nightCodeMap.put(39, 45);//
        nightCodeMap.put(41, 46);//
    }

    private static HashMap<Integer, String> codeMap = new HashMap<Integer, String>();

    static {
        codeMap.put(-1, "Unknown");
        codeMap.put(0, "Tornado");
        codeMap.put(1, "Tropical Storm");
        codeMap.put(2, "Hurricane");
        codeMap.put(3, "Severe Thunderstorms");
        codeMap.put(4, "Thunderstorms");
        codeMap.put(5, "Mixed Rain and Snow");
        codeMap.put(6, "Mixed Rain and Sleet");
        codeMap.put(7, "Mixed Snow and Sleet");
        codeMap.put(8, "Freezing Drizzle");
        codeMap.put(9, "Drizzle");
        codeMap.put(10, "Freezing Rain");
        codeMap.put(11, "Showers");
        codeMap.put(12, "Showers");
        codeMap.put(13, "Snow Flurries");
        codeMap.put(14, "Light Snow Showers");
        codeMap.put(15, "Blowing Snow");
        codeMap.put(16, "Snow");
        codeMap.put(17, "Hail");
        codeMap.put(18, "Sleet");
        codeMap.put(19, "Dust");
        codeMap.put(20, "Foggy");
        codeMap.put(21, "Haze");
        codeMap.put(22, "Smoky");
        codeMap.put(23, "Blustery");
        codeMap.put(24, "Windy");
        codeMap.put(25, "Cold");
        codeMap.put(26, "Cloudy");
        codeMap.put(27, "Mostly Cloudy"); // night
        codeMap.put(28, "Mostly Cloudy");
        codeMap.put(29, "Partly Cloudy"); // night
        codeMap.put(30, "Partly Cloudy");
        codeMap.put(31, "Clear"); // night
        codeMap.put(32, "Sunny");
        codeMap.put(33, "Fair"); // night
        codeMap.put(34, "Fair");
        codeMap.put(35, "Mixed Rain and Hail");
        codeMap.put(36, "Hot");
        codeMap.put(37, "Isolated Thunderstorms");
        codeMap.put(38, "Scattered Thunderstorms");
        codeMap.put(39, "Scattered Thunderstorms");
        codeMap.put(40, "Scattered Showers");
        codeMap.put(41, "Snow");
        codeMap.put(42, "Scattered Snow Showers");
        codeMap.put(43, "Heavy Snow");
        codeMap.put(44, "Partly Cloudy");
        codeMap.put(45, "Thundershowers");
        codeMap.put(46, "Snow Showers");
        codeMap.put(47, "Isolated Thundershowers");
        codeMap.put(3200, "Unknown");
    }

    static {
    }

    /**
     * Public API for OpenWeatherMap
     * Use this api to access all methods and properties of the OWM solution
     *  - pass a propFileSuffix only if you want the weather properties stored in other than the default properties file (for testing typically)
     */
    public OWM() {
        this("");
    }
    public OWM(String propFileSuffix) {
        //call init for any general settings/configuration to be initialized
        utils.init();

        this.propFileSuffix = propFileSuffix;
        this.wProps = new WeatherProperties(this.propFileSuffix);
        //prep the search parameters
        ClearSearch();

        //at this time there is only 1 implementation which is OWM
        //load the properties into the WeatherLocations map
        api = loadWeatherLocations();

        if (!this.api.hasLocations()) {
            Log.info("OWM","OWM: constructor - Unable to configure weather. No locations configured. Weather will not work until a location is added.");
        }else{
            Log.info("OWM","OWM: constructor - Locations found and loaded");
        }

    }

    //reload the properties from the properties file incase it was manually edited
    public void RefreshProps(){
        wProps.clear();
        wProps.load();
        api.clear();
        api = loadWeatherLocations();
    }

    private WeatherLocations loadWeatherLocations(){
        WeatherLocations tLocs = new WeatherLocations();
        if (wProps.size()>0){
            String defaultLocID = wProps.getProperty(weatherLocKey + "defaultLocationID", Const.KEY_NOT_FOUND);
            if(!defaultLocID.equals(Const.KEY_NOT_FOUND)){
                tLocs.setDefaultID(defaultLocID);
            }

            //load the locations from props to WeatherLocations and return it
            //get all the location keys from the props
            Map locKeys = wProps.getPropertiesWithPrefix(weatherLocKey);
            for (Object locKey : locKeys.keySet()){
                String tkey = weatherLocKey + locKey.toString();
                //only process properties that are a locationID then get the remaining properties for that location
                if (locKey.toString().equals(wProps.getProperty(tkey))){
                    String tName = wProps.getProperty(tkey + Const.PropDivider + "name");
                    String tCountry = wProps.getProperty(tkey + Const.PropDivider + "country");
                    String tLat = wProps.getProperty(tkey + Const.PropDivider + "latitude");
                    String tLon = wProps.getProperty(tkey + Const.PropDivider + "longitude");
                    String tUnits = wProps.getProperty(tkey + Const.PropDivider + "units");
                    if (locKey.toString().equals(defaultLocID)){
                        tLocs.add(new geoLocation(tName,tCountry,tLat,tLon,tUnits),true);
                        Log.info("OWM","OWM.loadWeatherLocations: " + locKey + ":" + tName + ":" + tCountry + ":" + tLat + ":" + tLon + ":" + tUnits + ":DEFAULT");
                    }else{
                        tLocs.add(new geoLocation(tName,tCountry,tLat,tLon,tUnits));
                        Log.info("OWM","OWM.loadWeatherLocations: " + locKey + ":" + tName + ":" + tCountry + ":" + tLat + ":" + tLon + ":" + tUnits);
                    }
                }
            }

        }
        return tLocs;

    }

    private void saveWeatherLocation(geoLocation loc){
        String weatherLocIDKey = weatherLocKey + loc.getID();
        wProps.put(weatherLocIDKey,loc.getID());
        wProps.put(weatherLocIDKey + Const.PropDivider + "name",loc.getName());
        wProps.put(weatherLocIDKey + Const.PropDivider + "latitude",loc.getLatitude().toString());
        wProps.put(weatherLocIDKey + Const.PropDivider + "longitude",loc.getLongitude().toString());
        wProps.put(weatherLocIDKey + Const.PropDivider + "country",loc.getCountry());
        wProps.put(weatherLocIDKey + Const.PropDivider + "units",loc.getUnits().toString());

        //save the default weather location in case it's changed
        wProps.put(weatherLocKey + "defaultLocationID", api.getDefault().getID());
        //write out the changes
        wProps.save();
    }

    private void removeWeatherLocation(String locationID){
        Map locKeys = wProps.getPropertiesWithPrefix(weatherLocKey + locationID);
        for (Object locKey : locKeys.keySet()){
            String tKey = weatherLocKey + locationID + locKey;
            wProps.remove(tKey);
        }
        //save the default weather location in case it's changed
        if(api.hasLocations()){
            wProps.put(weatherLocKey + "defaultLocationID", api.getDefault().getID());
        }else{
            wProps.put(weatherLocKey + "defaultLocationID", "");
        }
        wProps.save();
    }

    private void removeAllWeatherLocations(){
        Map locKeys = wProps.getPropertiesWithPrefix(weatherLocKey);
        for (Object locKey : locKeys.keySet()){
            wProps.remove(locKey);
        }
        wProps.put(weatherLocKey + "defaultLocationID", "");
        wProps.save();
    }

    public String GetSearchPhrase() {
        return searchPhrase;
    }

    public void SetSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }

    public boolean HasSearchPhrase(){
        if(searchPhrase.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public geoLocation.Country GetSearchCountryCode() {
        return searchCountryCode;
    }

    public void SetSearchCountryCode(geoLocation.Country searchCountryCode) {
        this.searchCountryCode = searchCountryCode;
    }

    public geoLocationSearch.SearchType GetSearchType() {
        return searchType;
    }

    public void SetSearchType(geoLocationSearch.SearchType searchType) {
        this.searchType = searchType;
    }

    public IWeatherLocation.Units GetSearchUnits() {
        return searchUnits;
    }

    public void SetSearchUnits(IWeatherLocation.Units searchUnits) {
        this.searchUnits = searchUnits;
    }

    public void SetSearchUnitsNext(){
        if (searchUnits.equals(IWeatherLocation.Units.Metric)){
            SetSearchUnits(IWeatherLocation.Units.Standard);
        }else{
            SetSearchUnits(IWeatherLocation.Units.Metric);
        }
    }

    public void ClearSearch(){
        searchInProgress = false;
        SetSearchPhrase("");
        SetSearchCountryCode(geoLocation.Country.NONE);
        SetSearchType(geoLocationSearch.SearchType.Name);
        SetSearchUnits(IWeatherLocation.Units.Standard);
        searchResults = new WeatherLocations();
    }

    public List<geoLocationSearch.SearchType> GetSearchTypeList(){
        List<geoLocationSearch.SearchType> tList = new ArrayList<>();
        for (geoLocationSearch.SearchType sType: geoLocationSearch.SearchType.values()) {
            tList.add(sType);
        }
        return tList;
    }

    public List<geoLocation> GetSearchResults() {
        return searchResults.sortedList();
    }

    public int GetSearchResultsSize(){
        return searchResults.sortedList().size();
    }

    public boolean HasSearchResults(){
        return searchResults.hasLocations();
    }

    public String GetSearchStatus(){
        if(HasSearchResults()){
            return Const.SearchResultFound;
        }else if(searchInProgress){
            if(searchType.equals(geoLocationSearch.SearchType.Postal) && searchCountryCode.equals(geoLocation.Country.NONE)){
                return Const.SearchResultNotRunMissingCountry;
            }else{
                return Const.SearchResultNotFound;
            }
        }else{
            if(searchType.equals(geoLocationSearch.SearchType.Postal) && searchCountryCode.equals(geoLocation.Country.NONE)){
                return Const.SearchResultNotRunMissingCountry;
            }else{
                return Const.SearchResultNotRun;
            }
        }
    }

    public String GetSearchLocationName(geoLocation loc, boolean showWeather){
        if(showWeather){
            return loc.getFullNameWithTemp();
        }else{
            return loc.getFullName();
        }
    }

    /**
     * Initiates a search for a weather location and returns the results
     *
     */
    public WeatherLocations Search(String searchPhrase, geoLocationSearch.SearchType searchType, IWeatherLocation.Units searchUnits){
        return Search(searchPhrase,null,searchType,searchUnits);
    }
    public WeatherLocations Search(String searchPhrase, geoLocation.Country searchCountryCode, geoLocationSearch.SearchType searchType, IWeatherLocation.Units searchUnits){
        geoLocationSearch tSearch = new geoLocationSearch(searchPhrase,searchCountryCode,searchType,searchUnits);
        tSearch.search();
        return tSearch.getSearchResults();
    }
    public void Search(){
        searchInProgress = true;
        searchResults = Search(searchPhrase,searchCountryCode,searchType,searchUnits);
    }

    /**
     * Forces the current and forecasted weather to update. It is up to the
     * implementation to ensure that weather updates are cached. If the sageweather
     * is updated since the last call then it will return true. If an Error
     * happens, then IsError will return true and GetError will contain the
     * failure message.
     *
     * @return true if the weather was updated
     */
    public synchronized boolean Update() {
        return Update(null);
    }
    public synchronized boolean Update(String locationID) {
        if (api==null) {
            Log.info("OWM","OWM: Update - Weather is not configured.  Update Ignored.");
            return false;
        }

        if (locationID==null || locationID.isEmpty()){ //null locationID means perform update on all locations if enough time has passed
            if (GetUpdateIntervalSecs()>0 && (System.currentTimeMillis() < (api.getLastChecked() +GetUpdateIntervalMS()) )) {
                // no update
                Log.info("OWM","OWM: Update - No Weather Update required, since ALL locations are within the " + GetUpdateIntervalSecs() + " seconds window");
                return false;
            }
        }else if (GetUpdateIntervalSecs()>0 && (System.currentTimeMillis() < (api.getWeather(locationID).getLastChecked() +GetUpdateIntervalMS()) )) {
            // no update
            Log.info("OWM","OWM: Update - No Weather Update required, since we are within the " + GetUpdateIntervalSecs() + " seconds window");
            return false;
        }

        if (api.update(locationID)) {
            return true;
        }
        return false;
    }

    public int GetLocationCount(){
        return api.getMapLocations().size();
    }

    //finds the next locationID in the sorted list of locations
    //returns the first location if not found or if current is the last item
    public String GetLocationIDNext(String currentID){
        boolean found = false;
        for (geoLocation loc:api.sortedList()) {
            if(found){
                return loc.getID();
            }
            if(loc.getID().equals(currentID)){
                found = true;
            }
        }
        return api.sortedList().get(0).getID();
    }

    //finds the previous locationID in the sorted list of locations
    //returns the last location if not found or if current is the first item
    public String GetLocationIDPrevious(String currentID){
        boolean found = false;

        for (int i = api.sortedList().size() - 1; i >= 0; i--) {
            geoLocation loc = api.sortedList().get(i);
            if(found){
                return loc.getID();
            }
            if(loc.getID().equals(currentID)){
                found = true;
            }
        }
        return api.sortedList().get(api.sortedList().size() - 1).getID();
    }

    public String GetLocationID(geoLocation loc){
        return loc.getID();
    }

    /**
     * Adds a location to the api list of locations
     * returns false if the location already exists
     *
     * @return true if the api added the location.
     */
    public boolean AddLocation(geoLocation location, boolean isDefault) {
        boolean locAdded = AddLocation(location);
        if (locAdded && isDefault){
            api.setDefaultID(location.getID());
        }
        return locAdded;
    }
    public boolean AddLocation(geoLocation location) {
        //avoid duplicates...check if api already contains this new location
        if (api.hasLocation(location.getID())){
            Log.info("OWM","OWM: AddLocation - location already exists so not added:" + location.getFullName());
            return false;
        }
        api.add(location);
        //need to save the location to the properties file
        saveWeatherLocation(location);
        return true;
    }

    /**
     * Get the weather location's geoLocation entry
     *
     * @return geoLocation
     */
    public geoLocation GetLocation() {
        return GetLocation(api.getDefaultID());
    }
    public geoLocation GetLocation(String locationID) {
        return api.get(locationID);
    }

    /**
     * Removes the location from the list of locations.
     *
     * @return
     */
    public void RemoveLocation(String locationID)
    {
        if(locationID==null || locationID.isEmpty()){
            Log.info("OWM","OWM: RemoveLocation called with null or empty locationID");
        }else{
            Log.info("OWM","OWM: RemoveLocation called with ID:" + locationID);
            if (api.remove(locationID)){
                //need to remove the location from properties and then save the properties file
                removeWeatherLocation(locationID);
            }
        }

    }

    /**
     * Removes all locations.
     *
     * @return
     */
    public void RemoveAllLocations()
    {
        removeAllWeatherLocations();
        api.clear();
    }

    public void SetLocationAsDefault(String locationID){
        api.setDefaultID(locationID);
    }

    /**
     * Set the Unit for the weather service. Valid values are 'm' for Metric,
     * and 's' for Standard (imperial) units.
     *
     * @param units
     */
    public void SetUnits(String units) {
        SetUnits(units,api.getDefaultID());
    }
    public void SetUnits(String units, String locationID) {
        IWeatherLocation.Units u = null;
        if (units == null)
            u = IWeatherLocation.Units.Metric;
        if (units.toLowerCase().startsWith("m")) {
            u = IWeatherLocation.Units.Metric;
        } else {
            u = IWeatherLocation.Units.Standard;
        }

        String oldUnits = GetUnits(locationID);
        api.get(locationID).setUnits(u);
        if (oldUnits == null || !u.name().equalsIgnoreCase(oldUnits)) {
            //units has changed so the properties need saving
            saveWeatherLocation(GetLocation(locationID));
        }
    }

    /**
     * Return the configured units for the Weather Location
     *
     * @return
     */
    public String GetUnits() {
        return GetUnits(api.getDefaultID());
    }
    public String GetUnits(String locationID) {
        IWeatherLocation.Units u = api.get(locationID).getUnits();
        if (u == null)
            u = IWeatherLocation.Units.Metric;
        return u.name();
    }

    public void SetUnitsNext(){
        SetUnitsNext(api.getDefaultID());
    }
    public void SetUnitsNext(String locationID){
        if (api.get(locationID).getUnits().equals(IWeatherLocation.Units.Metric)){
            SetUnits(IWeatherLocation.Units.Standard.name(),locationID);
        }else{
            SetUnits(IWeatherLocation.Units.Metric.name(),locationID);
        }
    }

    /**
     * Return true if the Weather Service is configured.
     *
     * @return true if configured
     */
    public boolean IsConfigured() {
        return IsConfigured(api.getDefaultID());
    }
    public boolean IsConfigured(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return false;
        }else{
            return api.hasLocations();
        }
    }

    /**
     * Returns the {@link Date} the weather was last updated.
     *
     * @return {@link Date} of last update
     */
    public Date GetLastUpdated() {
        return GetLastUpdated(api.getDefaultID());
    }
    public Date GetLastUpdated(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.getWeather(locationID).getLastUpdated();
    }

    /**
     * Returns the {@link Date} the weather was recorded.
     *
     * @return {@link Date} weather was recorded
     */
    public Date GetRecordedDate() {
        return GetRecordedDate(api.getDefaultID());
    }
    public Date GetRecordedDate(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.getWeather(locationID).getRecordedDate();
    }

    /**
     * Returns the location name (usually the City) if known. This may be null
     * until an update happens.
     *
     * @return location name, usually the city
     */
    public String GetLocationName() {
        return GetLocationName(api.getDefaultID());
    }
    public String GetLocationName(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.getWeather(locationID).getLocationName();
    }

    /**
     * Returns the full location name. This may be null
     * until an update happens.
     *
     * @return location name and country
     */
    public String GetLocationFullName() {
        return GetLocationFullName(api.getDefaultID());
    }
    public String GetLocationFullName(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.getWeather(locationID).getLocation().getFullName();
    }
    /**
     * Returns the location title - formatted to caption if not configured
     *
     * @return location name unless not configured
     */
    public String GetLocationTitle() {
        return GetLocationTitle(api.getDefaultID());
    }
    public String GetLocationTitle(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return "Not Configured";
        }
        if (IsConfigured(locationID)){
            return api.getWeather(locationID).getLocationName();
        }else{
            return "Not Configured";
        }
    }

    /**
     * Returns the location lat/long - formatted to caption if not configured
     *
     * @return location lat/long unless not configured
     */
    public String GetLocationLatLon() {
        return GetLocationLatLon(api.getDefaultID());
    }
    public String GetLocationLatLon(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return "Not Configured";
        }
        if (IsConfigured(locationID)){
            return api.get(locationID).getLatitude().toString() + "/" + api.get(locationID).getLongitude().toString();
        }else{
            return "Not Configured";
        }
    }

    /**
     * Returns the location country - formatted to caption if not configured
     *
     * @return location country
     */
    public String GetLocationCountry() {
        return GetLocationCountry(api.getDefaultID());
    }
    public String GetLocationCountry(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return "XX";
        }
        if (IsConfigured(locationID)){
            //geoLocation.Country tCountry = geoLocation.findByCountryCode(api.get(locationID).getCountry());
            return api.get(locationID).getCountry();
        }else{
            return "XX";
        }
    }

    public boolean HasUserKey(){
        return utils.hasUserKey();
    }

    public String GetUserKeyMessage(){
        return "OpenWeatherMap requires a personal user key. \n \n Go to 'https://home.openweathermap.org/users/sign_up' and sign up for a user key and place it as a single line with \n \n key=YOURUSERKEYHERE \n \nin a file called 'owmkey.properties' in the SageTV root folder and try again.";
    }

    /**
     * Return true if there was a Weather Service error
     *
     * @return true if error
     */
    public boolean HasError() {
        return HasError(api.getDefaultID());
    }
    public boolean HasError(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return false;
        }
        return api.getWeather(locationID).hasError();
    }

    /**
     * Returns the error if HasError return true, otherwise it will return null.
     *
     * @return
     */
    public String GetError() {
        return GetError(api.getDefaultID());
    }
    public String GetError(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.getWeather(locationID).getError();
    }

    /**

     * Return the Windchill or Feels like temperature for the forecast period
     *
     * @return Feel Like Temp

     */
    public int GetFeelsLike(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getFeelsLike();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public IForecastPeriod GetCurrentWeather() {
        return GetCurrentWeather(api.getDefaultID());
    }
    public IForecastPeriod GetCurrentWeather(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }else{
            return api.getWeather(locationID).getCurrentWeather();
        }
    }

    public int GetForecastDays() {
        return GetForecastDays(api.getDefaultID());
    }
    public int GetForecastDays(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return 0;
        }
        return api.getWeather(locationID).getForecastDays();
    }

    public int GetForecastPeriodCount() {
        return GetForecastPeriodCount(api.getDefaultID());
    }
    public int GetForecastPeriodCount(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return 0;
        }
        return api.getWeather(locationID).getForecasts().size();
    }

    /**
     * Gets the specific day number for a ForecastPeriod
     *
     * @return int day number, defaults to 0
     */
    public int GetForecastDay(IForecastPeriod period) {
        return GetForecastDay(period,api.getDefaultID());
    }
    public int GetForecastDay(IForecastPeriod period, String locationID) {
        int periodIndex = GetForecastPeriods(locationID).indexOf(period);
        if (periodIndex == -1) {
            return 0;
        }
        if (HasTodaysHigh(locationID)) {
            return periodIndex / 2;
        } else {
            return (periodIndex + 1) / 2;
        }
    }

    public List<IForecastPeriod> GetForecastPeriods(int MaxPeriods) {
        return GetForecastPeriods(MaxPeriods,api.getDefaultID());
    }
    public List<IForecastPeriod> GetForecastPeriods(int MaxPeriods, String locationID) {
        List<IForecastPeriod> tPeriods = new ArrayList<IForecastPeriod>();
        int counter = 0;
        for (IForecastPeriod fp : GetForecastPeriods()) {
            counter++;
            if (counter > MaxPeriods) {
                break;
            }
            tPeriods.add(fp);
        }
        return tPeriods;
    }

    public List<IForecastPeriod> GetForecastPeriods() {
        return GetForecastPeriods(api.getDefaultID());
    }
    public List<IForecastPeriod> GetForecastPeriods(String locationID) {
        List<IForecastPeriod> tPeriods = new ArrayList<IForecastPeriod>();
        if (GetForecasts(locationID) != null) {
            for (ILongRangeForecast lr : GetForecasts(locationID)) {
                IForecastPeriod p = lr.getForecastPeriodDay();

                // TOOO: check if valid
                if (p != null) {
                    tPeriods.add(p);
                }

                p = lr.getForecastPeriodNight();

                // TODO: check if valid
                if (p != null) {
                    tPeriods.add(p);
                }
            }
        }
        return tPeriods;
    }

    public List<ILongRangeForecast> GetForecasts(int MaxDays) {
        return GetForecasts(api.getDefaultID());
    }
    public List<ILongRangeForecast> GetForecasts(int MaxDays, String locationID) {
        List<ILongRangeForecast> tDays = new ArrayList<ILongRangeForecast>();
        int counter = 0;
        for (ILongRangeForecast lrf : GetForecasts(locationID)) {
            counter++;
            if (counter > MaxDays) {
                break;
            }
            tDays.add(lrf);
        }

        return tDays;
    }

    public List<ILongRangeForecast> GetForecasts() {
        return GetForecasts(api.getDefaultID());
    }
    public List<ILongRangeForecast> GetForecasts(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.getWeather(locationID).getForecasts();
    }

    /**
     * Gets the LongRangForecast for a specific day
     *
     * @return ILongRangeForecast
     */
    public ILongRangeForecast GetForecast(int day) {
        return GetForecast(day, api.getDefaultID());
    }
    public ILongRangeForecast GetForecast(int day, String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        if (api.getWeather(locationID).getForecasts() == null) {
            return null;
        }
        if (api.getWeather(locationID).getForecasts().size() > day) {
            return api.getWeather(locationID).getForecasts().get(day);
        }
        return null;
    }

    /**
     * Returns the forecast data for this specific forecast day
     *
     * @return ForecastPeriod for the Day
     */
    public IForecastPeriod GetForecastPeriodDay(int day) {
        return GetForecastPeriodDay(day,api.getDefaultID());
    }
    public IForecastPeriod GetForecastPeriodDay(int day, String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        if (api.getWeather(locationID).getForecasts().size() > day) {
            return api.getWeather(locationID).getForecasts().get(day).getForecastPeriodDay();
        }
        return null;
    }

    /**

     * Returns the forecast data for this specific forecast day
     *
     * @return LongRangeforecastPeriod for the Day

     */
    public IForecastPeriod GetForecastPeriodDay(ILongRangeForecast ilongrangeforecast) {
        try {
            if (ilongrangeforecast == null) {
                return null;
            }
            return ilongrangeforecast.getForecastPeriodDay();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the forecast data for this specific forecast day
     *
     * @return ForecastPeriod for the Night
     */
    public IForecastPeriod GetForecastPeriodNight(int day) {
        return GetForecastPeriodNight(day, api.getDefaultID());
    }
    public IForecastPeriod GetForecastPeriodNight(int day, String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        if (api.getWeather(locationID).getForecasts().size() > day) {
            return api.getWeather(locationID).getForecasts().get(day).getForecastPeriodNight();
        }
        return null;
    }

    /**

     * Returns the forecast data for this specific forecast night
     *
     * @return LongRangeforecastPeriod for the Night

     */
    public IForecastPeriod GetForecastPeriodNight(ILongRangeForecast ilongrangeforecast) {
        try {
            if (ilongrangeforecast == null) {
                return null;
            }
            return ilongrangeforecast.getForecastPeriodNight();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the best forecast period for this specific forecast day
     *
     * @return ForecastPeriod for the Day unless null then uses the Night
     */
    public IForecastPeriod GetForecastPeriodSingle(int day) {
        return GetForecastPeriodSingle(day, api.getDefaultID());
    }
    public IForecastPeriod GetForecastPeriodSingle(int day, String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        if (api.getWeather(locationID).getForecasts().size() > day) {
            if (api.getWeather(locationID).getForecasts().get(day).getForecastPeriodDay() == null) {
                return api.getWeather(locationID).getForecasts().get(day).getForecastPeriodNight();
            }
            return api.getWeather(locationID).getForecasts().get(day).getForecastPeriodDay();
        }
        return null;
    }

    /**
     * Returns the best forecast period for this specific long range forecast
     * day
     *
     * @return ForecastPeriod for the Day unless null then uses the Night
     */
    public IForecastPeriod GetForecastPeriodSingle(ILongRangeForecast ilongrangeforecast) {
        if (ilongrangeforecast == null) {
            return null;
        }
        if (ilongrangeforecast.getForecastPeriodDay() == null) {
            return ilongrangeforecast.getForecastPeriodNight();
        }
        return ilongrangeforecast.getForecastPeriodDay();
    }

    /**
     * Gets the ForecastPeriod for a specific period
     *
     * @return IForecastPeriod
     */
    public IForecastPeriod GetForecastPeriod(int period) {
        return GetForecastPeriod(period, api.getDefaultID());
    }
    public IForecastPeriod GetForecastPeriod(int period, String locationID) {
        if (GetForecastPeriods(locationID) == null) {
            return null;
        }
        if (GetForecastPeriods(locationID).size() > period) {
            return GetForecastPeriods(locationID).get(period);
        }
        return null;
    }

    public String GetFormattedTemp(Object temp) {
        return GetFormattedTemp(temp, api.getDefaultID());
    }
    public String GetFormattedTemp(Object temp, String locationID) {
        if (temp == null)
            return "N/A";

        if (IWeatherLocation.Units.Metric.name().equals(GetUnits(locationID))) {
            return temp + " C";
        } else {
            return temp + " F";
        }
    }

    public String GetFormattedSpeed(Object speed) {
        return GetFormattedSpeed(speed, api.getDefaultID());
    }
    public String GetFormattedSpeed(Object speed, String locationID) {
        if (speed == null)
            return "N/A";
        if (IWeatherLocation.Units.Metric.name().equals(GetUnits(locationID))) {
            return speed + " k/h";
        } else {
            return speed + " mph";
        }
    }

    public String GetFormattedVisibility(int visibility) {
        return GetFormattedVisibility(visibility, api.getDefaultID());
    }
    public String GetFormattedVisibility(int visibility, String locationID) {
        if (locationID==null || locationID.isEmpty() || visibility==IForecastPeriod.iNotSupported)
            return "N/A";
        if (IWeatherLocation.Units.Metric.name().equals(GetUnits(locationID))) {
            if(visibility==10000){
                return "10 km";
            }
            return (int) Math.round(visibility/1000) + " km";
        } else {
            if(visibility==10000){
                return "10 mi";
            }
            return (int) Math.round(visibility*0.000621371192) + " mi";
        }
    }

    /**
     * Convenience function to return formatted wind information examples -
     * "Calm" or SW/5 mph
     *
     * @return wind info formatted
     */
    public String GetFormattedWind(IForecastPeriod forecastperiod, String separator) {
        return GetFormattedWind(forecastperiod, separator, api.getDefaultID());
    }
    public String GetFormattedWind(IForecastPeriod forecastperiod, String separator, String locationID) {
        if (forecastperiod == null) {
            return "";
        }
        if (forecastperiod.getWindDirText().toLowerCase().equals("calm")) {
            return forecastperiod.getWindDirText();
        } else {
            // make sure this is supported and valid
            String retVal = "";
            if (IsSupported(forecastperiod.getWindSpeed()) && IsValid(forecastperiod.getWindSpeed())) {
                retVal = GetFormattedSpeed(forecastperiod.getWindSpeed(),locationID);
                if (IsSupported(forecastperiod.getWindDirText()) && IsSupported(forecastperiod.getWindDirText())) {
                    retVal = forecastperiod.getWindDirText() + separator + retVal;
                }
            }
            return retVal;
        }
    }

    public String GetDay(Date date) {
        if (date == null)
            return "Unknown";
        SimpleDateFormat df = new SimpleDateFormat("E");
        return df.format(date);
    }

    /**
     * Returns the short day name for this specific day example - (Mon, Tue,
     * Wed, etc)
     *
     * @return period day name
     */
    public String GetDay(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null) {
            return "N/A";
        }
        if (longrangeforecast.getForecastPeriodDay() == null) {
            return GetDay(longrangeforecast.getForecastPeriodNight());
        }
        return GetDay(longrangeforecast.getForecastPeriodDay());
    }

    /**
     * Returns the full day name for this specific day example - (Today,
     * Tonight, Monday, Monday Night, Tuesday, etc)
     *
     * @return period full day name
     */
    public String GetDayFull(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null) {
            return "N/A";
        }
        if (longrangeforecast.getForecastPeriodDay() == null) {
            return GetDayFull(longrangeforecast.getForecastPeriodNight());
        }
        return GetDayFull(longrangeforecast.getForecastPeriodDay());
    }

    /**

     * Returns the {@link Date} for this specific forecast period
     *
     * @return {@link Date}

     */
    public Date GetDate(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getDate();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Return the Date for the specific Day
     *
     * @return Day Date
     */
    public Date GetDate(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null) {
            return null;
        }
        if (longrangeforecast.getForecastPeriodDay() == null) {
            return longrangeforecast.getForecastPeriodNight().getDate();
        }
        return longrangeforecast.getForecastPeriodDay().getDate();
    }

    /**
     * Return the High Feels Like temp for a specific Day
     *
     * @return Day High Feels Like temp
     */
    public int GetHighFeelsLike(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null || longrangeforecast.getForecastPeriodDay() == null) {
            return IForecastPeriod.iInvalid;
        }
        return longrangeforecast.getForecastPeriodDay().getFeelsLike();
    }

    /**

     * Return the humidity for the forecast Period - should be -1 if invalid or
     * unavailable
     *
     * @return Period Humidity

     */
    public String GetHumid(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return IForecastPeriod.sInvalid;
            }
            return iforecastperiod.getHumid();
        } catch (Throwable t) {
            t.printStackTrace();
            return IForecastPeriod.sInvalid;
        }
    }

    /**
     * Return the Low Feels Like temp for a specific Day
     *
     * @return Day Low Feels Like temp
     */
    public int GetLowFeelsLike(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null || longrangeforecast.getForecastPeriodNight() == null) {
            return IForecastPeriod.iInvalid;
        }
        return longrangeforecast.getForecastPeriodNight().getFeelsLike();
    }

    /**
     * Return the High temp for a specific Day
     *
     * @return Day High temp
     */
    public int GetHigh(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null || longrangeforecast.getForecastPeriodDay() == null) {
            return IForecastPeriod.iInvalid;
        }
        return longrangeforecast.getForecastPeriodDay().getTemp();
    }

    /**
     * Return the Low temp for a specific Day
     *
     * @return Day Low temp
     */
    public int GetLow(ILongRangeForecast longrangeforecast) {
        if (longrangeforecast == null || longrangeforecast.getForecastPeriodNight() == null) {
            return IForecastPeriod.iInvalid;
        }
        return longrangeforecast.getForecastPeriodNight().getTemp();
    }

    /**
     * Determine if the first forecast day has a Day record with a valid High
     * temp
     *
     * @return
     */
    public boolean HasTodaysHigh() {
        return HasTodaysHigh(api.getDefaultID());
    }
    public boolean HasTodaysHigh(String locationID) {
        if (GetForecasts(locationID) == null) {
            return false;
        }
        if (GetForecasts(locationID).get(0).getForecastPeriodDay() == null) {
            return false;
        }
        if (GetForecasts(locationID).get(0).getForecastPeriodDay().getTemp() == IForecastPeriod.iInvalid) {
            return false;
        }
        return true;
    }

    /**

     * Returns the type of this period (Day/Night/Current)
     *
     * @return period type (Day or Night or Current)

     */
    public IForecastPeriod.Type GetType(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getType();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Return the Day Type Name for the period can be Day, Night or Current
     *
     * @return Day Type as string
     */
    public String GetTypeName(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return "N/A";
        }
        return forecastperiod.getType().name();
    }

    /**
     * Return the Day Name for the period will be 3 character name - Mon, Tue,
     * Wed, etc
     *
     * @return Short Day Name
     */
    public String GetDay(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return "N/A";
        }
        return getDayName(forecastperiod.getDate());
    }

    /**
     * Return the Long Day Name for the period will be - Today, Tonight, Monday,
     * Monday Night, etc
     *
     * @return Full Day Name
     */
    public String GetDayFull(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return "N/A";
        }
        String tName = getDayNameFull(forecastperiod.getDate());
        if (forecastperiod.getType().equals(IForecastPeriod.Type.Night)) {
            if (tName.equals("Today")) {
                return "Tonight";
            } else {
                return tName + " Night";
            }
        } else {
            return tName;
        }
    }

    private String getDayName(Date Day) {
        Calendar thisDay = Calendar.getInstance();
        thisDay.setTime(Day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE");
        return dateFormat.format(thisDay.getTime());
    }

    private String getDayNameFull(Date Day) {
        boolean isToday = false;
        Calendar Today = Calendar.getInstance();
        Today.setTime(Calendar.getInstance().getTime());
        Calendar thisDay = Calendar.getInstance();
        thisDay.setTime(Day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
        if (Today.get(Calendar.ERA) == thisDay.get(Calendar.ERA) && Today.get(Calendar.YEAR) == thisDay.get(Calendar.YEAR)
                && Today.get(Calendar.DAY_OF_YEAR) == thisDay.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        } else {
            return dateFormat.format(thisDay.getTime());
        }
    }

    /**

     * Returns the temperature for the current weather forecast period.
     *
     * @return current temp

     */
    public int GetTemp(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getTemp();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Return the temp type text for the periods temperature can be Low, High or
     * Now
     *
     * @return temp type text
     */
    public String GetTempType(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return "N/A";
        }
        if (forecastperiod.getType().equals(IForecastPeriod.Type.Day)) {
            return "High";
        } else if (forecastperiod.getType().equals(IForecastPeriod.Type.Night)) {
            return "Low";
        } else {
            return "Now";
        }
    }

    /**

     * Return the Formatted cloud cover for the forecast period
     *
     * @return formatted Cloud Cover

     */
    public String GetCloudCover(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getCloudCover();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }



    /**

     * Return the high weather condition code, similar as described in
     * http://developer.yahoo.com/weather/#codes
     * <p/>
     * <pre>
     * 0	tornado
     * 1	tropical storm
     * 2	hurricane
     * 3	severe thunderstorms
     * 4	thunderstorms
     * 5	mixed rain and snow
     * 6	mixed rain and sleet
     * 7	mixed snow and sleet
     * 8	freezing drizzle
     * 9	drizzle
     * 10	freezing rain
     * 11	showers
     * 12	showers
     * 13	snow flurries
     * 14	light snow showers
     * 15	blowing snow
     * 16	snow
     * 17	hail
     * 18	sleet
     * 19	dust
     * 20	foggy
     * 21	haze
     * 22	smoky
     * 23	blustery
     * 24	windy
     * 25	cold
     * 26	cloudy
     * 27	mostly cloudy (night)
     * 28	mostly cloudy (day)
     * 29	partly cloudy (night)
     * 30	partly cloudy (day)
     * 31	clear (night)
     * 32	sunny
     * 33	fair (night)
     * 34	fair (day)
     * 35	mixed rain and hail
     * 36	hot
     * 37	isolated thunderstorms
     * 38	scattered thunderstorms
     * 39	scattered thunderstorms
     * 40	scattered showers
     * 41	heavy snow
     * 42	scattered snow showers
     * 43	heavy snow
     * 44	partly cloudy
     * 45	thundershowers
     * 46	snow showers
     * 47	isolated thundershowers
     * -1	not available
     * </pre>
     * <p/>
     * May vary by implementation but should be a code between 0 and 47 Should
     * be -1 if not available Implementations should map their condition codes
     * to the codes listed
     *
     * @return condition code for the period
     */
    public int GetCode(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getCode();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    /**
     * Return the text condition for the specific passed in code
     *
     * @return weather code condition
     */
    public String GetCodeText(int code) {
        if (codeMap.containsKey(code)) {
            return codeMap.get(code);
        } else {
            return "Unknown";
        }
    }

    /**
     * Return the text condition for the specific forecast period
     *
     * @return weather code condition
     */
    public String GetCodeText(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return "Unknown";
        }
        if (codeMap.containsKey(forecastperiod.getCode())) {
            return codeMap.get(forecastperiod.getCode());
        } else {
            return "Unknown (" + forecastperiod.getCode() + ")";
        }
    }

    /**
     * Return the weather day code for the specific passed in code - even if it
     * is a night code
     *
     * @return weather code for day
     */
    public int GetCodeForceDay(int code) {
        if (dayCodeMap.containsKey(code)) {
            return dayCodeMap.get(code);
        } else {
            return code;
        }
    }

    /**
     * Return the weather night code for the specific passed in code - even if
     * it is a day code
     *
     * @return weather code for night
     */
    public int GetCodeForceNight(int code) {
        if (nightCodeMap.containsKey(code)) {
            return nightCodeMap.get(code);
        } else {
            return code;
        }
    }

    /**

     * Returns a short descriptive text for this specific forecast period. It
     * will be something like, "Mostly Cloudy", or "Partly Sunny"
     *
     * @return short description of the periods weather condition.

     */
    public String GetCondition(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getCondition();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public boolean IsValid(int object) {
        if (object == IForecastPeriod.iInvalid) {
            return false;
        }
        return true;
    }

    public boolean IsValid(String object) {
        if (object.equals(IForecastPeriod.sInvalid)) {
            return false;
        }
        return true;
    }

    public boolean IsSupported(int object) {
        if (object == IForecastPeriod.iNotSupported) {
            return false;
        }
        return true;
    }

    public boolean IsSupported(String object) {
        if (object.equals(IForecastPeriod.sNotSupported)) {
            return false;
        }
        return true;
    }

    public boolean IsDay(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return false;
        }
        if (forecastperiod.getType().equals(IForecastPeriod.Type.Day)) {
            return true;
        }
        return false;
    }

    public boolean IsNight(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return false;
        }
        if (forecastperiod.getType().equals(IForecastPeriod.Type.Night)) {
            return true;
        }
        return false;
    }

    public boolean IsCurrent(IForecastPeriod forecastperiod) {
        if (forecastperiod == null) {
            return false;
        }
        if (forecastperiod.getType().equals(IForecastPeriod.Type.Current)) {
            return true;
        }
        return false;
    }

    public int GetUpdateIntervalSecs() {
        //TODO: get this interval from the properties
        int def = 60*30; // 30 minutes
        //return NumberUtils.toInt(Configuration.GetServerProperty(API_CHECK_PROP, String.valueOf(def)),def);
        return def;
    }

    public int GetUpdateIntervalMS() {
        return GetUpdateIntervalSecs()*1000;
    }

    /**

     * Return the UVIndex for the forecast period
     *
     * @return UVIndex

     */
    public String GetUVIndex(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getUVIndex();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**

     * Return the Visibility for the forecast period
     *
     * @return Visibility

     */
    public int GetVisibility(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getVisibility();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public long GetLastChecked() {
        return GetLastChecked(api.getDefaultID());
    }
    public long GetLastChecked(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return 0;
        }
        return api.getWeather(locationID).getLastChecked();
    }

    public void SetLastChedked(long timeInMs) {
        SetLastChedked(timeInMs,api.getDefaultID());
    }
    public void SetLastChedked(long timeInMs, String locationID) {
        if(locationID==null || locationID.isEmpty()){
        }else {
            api.getWeather(locationID).setLastChecked(timeInMs);
        }
    }

    public long GetTimeUntilNextCheckAllowed() {
        return GetTimeUntilNextCheckAllowed(api.getDefaultID());
    }
    public long GetTimeUntilNextCheckAllowed(String locationID) {
        if(locationID==null || locationID.isEmpty()){
            return 0;
        }
        return GetUpdateIntervalMS() + api.getWeather(locationID).getLastChecked() - System.currentTimeMillis();
    }

    public boolean HasSnow(IForecastPeriod forecastperiod){
        if (forecastperiod==null)return false;
        if (forecastperiod.getPrecipAccumulation().equals(IForecastPeriod.PercentNone))return false;
        if (forecastperiod.getPrecipAccumulation().equals(IForecastPeriod.sNotSupported))return false;
        if (forecastperiod.getPrecipType().equals(IForecastPeriod.PrecipType.Snow)) return true;
        return false;
    }

    public boolean HasPrecip(IForecastPeriod forecastperiod){
        if (forecastperiod==null)return false;
        if (forecastperiod.getPrecipType().equals(IForecastPeriod.PrecipType.None))return false;
        return true;
    }

    /**

     * Return the MoonPhase for the forecast period
     *  - an integer between 0 and 29 indicating the phase of the moon
     *  - to be used to display a specific moon phase image in the UI
     *  - 0 indicates a New Moon
     *
     * @return MoonPhase

     */
    public int GetMoonPhase(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getMoonPhase();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public String GetMoonPhaseFileName(ILongRangeForecast longrangeforecast, String prefix, String fileExt){
        if (longrangeforecast.getForecastPeriodDay() == null) {
            return GetMoonPhaseFileName(longrangeforecast.getForecastPeriodNight(),prefix,fileExt);
        }
        return GetMoonPhaseFileName(longrangeforecast.getForecastPeriodDay(),prefix,fileExt);

    }

    public String GetMoonPhaseFileName(IForecastPeriod forecastperiod,String prefix, String fileExt){
        if (forecastperiod==null)return prefix + "na" + "." + fileExt ;
        if (forecastperiod.getMoonPhase()==IForecastPeriod.iInvalid) return prefix + "na" + "." + fileExt ;
        return prefix + forecastperiod.getMoonPhase() + "." + fileExt;
    }

    public WeatherLocations GetWeatherLocations() {
        return api;
    }

    public geoLocation GetDefaultLocation(){return api.getDefault();}

    public String GetDefaultID(){return api.getDefaultID();}

    public IWeatherLocation GetDefaultLocationWeather(){return api.getDefault().getWeather();}

    public boolean IsDefault(String locationID){
        if(locationID==null || locationID.isEmpty()){
            return false;
        }
        if(locationID.equals(api.getDefaultID())){
            return true;
        }else{
            return false;
        }
    }

    public void SetDefaultID(String locationID){
        api.setDefaultID(locationID);
        utils.SetServerPropertyWithSave(weatherLocKey + "defaultLocationID",locationID);
    }

    /**

     * Returns the degrees wind direction for the forecast period.
     *
     * @return wind direction in degrees

     */
    public int GetWindDir(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getWindDir();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    /**

     * Returns the wind direction for the forecast period.
     *
     * @return wind direction as text

     */
    public String GetWindDirText(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getWindDirText();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**

     * Returns the wind speed for the forecast period. - should be 0 for CALM -
     * should be -1 if invalid or unavailable
     *
     * @return wind speed

     */
    public int GetWindSpeed(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getWindSpeed();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    /**

     * Return the Long Description of the forecast Period
     *
     * @return long Period Description

     */
    public String GetDescription(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getDescription();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**

     * Return the DewPoint for the forecast period
     *
     * @return DewPoint

     */
    public String GetDewPoint(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getDewPoint();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public IWeatherLocation GetLocationWeather(String locationID){
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        return api.get(locationID).getWeather();
    }

    public String GetWeatherImplKey(){return "OWM";}

    /**

     * Get the impl name for the current impl key ('yahoo', 'darksky')
     *
     * @return name of current impl key

     */
    public String GetWeatherImplName() {
        return GetSourceName();
    }

    public String GetSourceName(){return "OpenWeather";}

    /**

     * Returns the Sunrise time, such as, "7:28 am", if known
     *
     * @return formatted time of sunrise

     */
    public String GetSunrise(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getSunrise();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**

     * Return the Sunset time, such as, "4:53 pm", if known
     *
     * @return formatted time of sunset

     */
    public String GetSunset(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getSunset();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public String GetNightShortName(IForecastPeriod forecastperiod){
        return GetNightShortName(forecastperiod, api.getDefaultID());
    }
    public String GetNightShortName(IForecastPeriod forecastperiod,String locationID){
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        String tName = "";
        if (IsDay(forecastperiod)){
            return "Tomorrow";
        }else{
            if (GetForecastDay(forecastperiod,locationID)==0){
                return "Tonight";
            }else{
                return GetDay(forecastperiod) + " Night";
            }
        }
    }

    /**

     * Return the Formatted precipitation for the forecast period - can be in
     * either inches, mm, or may be % so should be fully formatted for display
     *
     * @return Period Precipitation

     */
    public String GetPrecip(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getPrecip();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**

     * Return the PrecipAccumulation (Snow) for the forecast period
     *  - this string should be full formatted for display including the units
     *
     * @return PrecipAccumulation

     */
    public String GetPrecipAccumulation(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getPrecipAccumulation();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public String GetPrecipType(IForecastPeriod iforecastperiod){
        return iforecastperiod.getPrecipType().name();
    }

    /**

     * Return the Barometric Pressure for the forecast period
     *
     * @return Pressure

     */
    public String GetPressure(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return null;
            }
            return iforecastperiod.getPressure();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**

     * Return an indicator of the Barometric Pressure rising/falling for the
     * forecast period.
     *  - 0 steady (default) 1 rising -1 falling
     *
     * @return Pressure Direction

     */
    public int GetPressureDir(IForecastPeriod iforecastperiod) {
        try {
            if (iforecastperiod == null) {
                return 0;
            }
            return iforecastperiod.getPressureDir();
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    private static boolean isValidZIP(String ZIPCode){
        //String regex = "^\\d{5}(-\\d{4})?$"; //extended zip
        String regex = "^\\d{5}";  //5 digit zip
        //LOG.debug("isValidZIP: checking for ZIP '" + ZIPCode + "'");
        return Pattern.matches(regex, ZIPCode);
    }

    public String GetBackground(){
        return GetBackground(api.getDefaultID());
    }
    public String GetBackground(String locationID){
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        IForecastPeriod current = GetCurrentWeather(locationID);
        return GetBackground(current,locationID);
    }
    public String GetBackground(IForecastPeriod current){
        return GetBackground(current,api.getDefaultID());
    }
    public String GetBackground(IForecastPeriod current,String locationID){
        if(locationID==null || locationID.isEmpty()){
            return null;
        }
        if (IsConfigured(locationID)){
            int code = GetCode(current);
            return GetBackground(code);
        }else{
            return "";
        }
    }
    public String GetBackground(int code){
        if (IsConfigured()){
            if (code>-1 && code <48){
                //use the proper day or night code
                if (WIcons.IsDaytime(GetCurrentWeather())){
                    code = GetCodeForceDay(code);
                }else{
                    code = GetCodeForceNight(code);
                }

                //determine which image file to return
                int BGIndex = GetBackgroundIndex(code);
                if (GetBackgroundsList(code).size()>BGIndex){
                    return GetBackgroundsPath(code) + GetBackgroundsList(code).get(BGIndex);
                }else{
                    if (GetBackgroundsList(code).size()>0){
                        //adjust the index back to 0
                        SetBackgroundIndex(code, 0);
                        return GetBackgroundsPath(code) + GetBackgroundsList(code).get(0);
                    }
                    return "";
                }
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public ArrayList<String> GetBackgroundsList(int code){
        SortedSet<String> tList = new TreeSet<String>();
        File BGLoc = new File(GetBackgroundsPath(code));
        if (BGLoc!=null){
            File[] files = BGLoc.listFiles();
            if (files==null){
                Log.info("OWM","GetBackgroundsList: for code '" + code + "' invalid backgrounds location '" + GetBackgroundsPath(code) + "'");
                return new ArrayList<String>();
            }else{
                for (File file : files){
                    if (!file.isDirectory()){
                        tList.add(file.getName());
                    }
                }
                //LOG.debug("GetBackgroundsList: for code '" + code + "' found '" + tList + "'");
                return new ArrayList<String>(tList);
            }
        }else{
            Log.info("OWM","GetBackgroundsList: for code '" + code + "' invalid backgrounds location '" + GetBackgroundsPath(code) + "'");
            return new ArrayList<String>();
        }
    }

    public String GetBackgroundsPath(int code){
        return utils.WeatherLocationBackgrounds() + File.separator + "Backgrounds" + File.separator + code + File.separator;
    }

    private String getBGPropLocation(int code){
        return Const.Weather + Const.PropDivider + Const.WeatherBGIndex + Const.PropDivider + code;
    }

    public int GetBackgroundIndex(int code){
        return utils.GetServerPropertyAsInteger(getBGPropLocation(code), 0);
    }
    public void SetBackgroundIndexNext(int code){
        int BGIndex = GetBackgroundIndex(code)+1;
        utils.SetServerProperty(getBGPropLocation(code), BGIndex + "");
    }
    public void SetBackgroundIndex(int code, int newIndex){
        utils.SetServerProperty(getBGPropLocation(code), newIndex + "");
    }

    public String GetMoonPhaseImage(ILongRangeForecast longrangeforecast){
        return utils.WeatherLocation() + File.separator + "MoonPhases" + File.separator + GetMoonPhaseFileName(longrangeforecast,"moon_","jpg");
    }

    public String GetMoonPhaseImage(IForecastPeriod iforecastperiod){
        return utils.WeatherLocation() + File.separator + "MoonPhases" + File.separator + GetMoonPhaseFileName(iforecastperiod,"moon_","jpg");
    }

    public String GetIconImage(IForecastPeriod iforecastperiod){
        if (GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            return WIcons.GetWeatherIconByNumber(GetCode(iforecastperiod));
        }
    }
    public String GetIconImageCurrent(IForecastPeriod iforecastperiod){
        if (GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            if (WIcons.IsDaytime(iforecastperiod)){
                return WIcons.GetWeatherIconByNumber(GetCodeForceDay(GetCode(iforecastperiod)));
            }else{
                return WIcons.GetWeatherIconByNumber(GetCodeForceNight(GetCode(iforecastperiod)));
            }
        }
    }
    public String GetIconImageDay(IForecastPeriod iforecastperiod){
        if (GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            return WIcons.GetWeatherIconByNumber(GetCodeForceDay(GetCode(iforecastperiod)));
        }
    }
    public String GetIconImageNight(IForecastPeriod iforecastperiod){
        if (GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            return WIcons.GetWeatherIconByNumber(GetCodeForceNight(GetCode(iforecastperiod)));
        }
    }

    public boolean HasDescription(){
        return HasDescription(api.getDefaultID());
    }
    public boolean HasDescription(String locationID){
        if(locationID==null || locationID.isEmpty()){
            return false;
        }
        if (GetForecastPeriods(locationID)==null){
            return false;
        }
        if (IsSupported(GetDescription(GetForecastPeriod(0,locationID)))){
            return true;
        }else{
            return false;
        }

    }
    public int GetForecastColumns(boolean ForecastExpandFocused){
        return GetForecastColumns(ForecastExpandFocused);
    }
    public int GetForecastColumns(boolean ForecastExpandFocused,String locationID){
        if(locationID==null || locationID.isEmpty()){
            if (ForecastExpandFocused){
                return 3;
            }else{
                return 5;
            }
        }
        if (HasDescription()){
            if (ForecastExpandFocused){
                return 3;
            }else{
                return 5;
            }
        }else if(GetForecastDays(locationID)<3){
            return 2;
        }else{
            if (ForecastExpandFocused){
                return 4;
            }else{
                return 5;
            }
        }
    }

    public double GetForecastWidth(boolean Focused, boolean ForecastExpandFocused, IForecastPeriod NightPeriod){
        return GetForecastWidth(Focused,ForecastExpandFocused,NightPeriod,api.getDefaultID());
    }
    public double GetForecastWidth(boolean Focused, boolean ForecastExpandFocused, IForecastPeriod NightPeriod,String locationID){
        if(locationID==null || locationID.isEmpty()){
            return 0;
        }
        if (HasDescription(locationID)){
            if (Focused){
                if (ForecastExpandFocused){
                    if (NightPeriod==null){
                        return (1.0/5)*1.5;
                    }else{
                        return (1.0/5)*3;
                    }
                }else{
                    return (1.0/5);
                }
            }else{
                return (1.0/5);
            }
        }else if(GetForecastDays(locationID)<3){
            return (1.0/2);
        }else{
            if (Focused){
                if (ForecastExpandFocused){
                    if (NightPeriod==null){
                        return (1.0/5)*1;
                    }else{
                        return (1.0/5)*2;
                    }
                }else{
                    return (1.0/5);
                }
            }else{
                return (1.0/5);
            }
        }
    }

    public boolean UseSplitForecast(boolean Focused, boolean ForecastExpandFocused){
        return UseSplitForecast(Focused,ForecastExpandFocused,api.getDefaultID());
    }
    public boolean UseSplitForecast(boolean Focused, boolean ForecastExpandFocused,String locationID){
        if(locationID==null || locationID.isEmpty()){
            return false;
        }
        if (HasDescription(locationID)){
            if (Focused){
                if (ForecastExpandFocused){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }else if(GetForecastDays(locationID)<3){
            return true;
        }else{
            if (Focused){
                if (ForecastExpandFocused){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
    }


    public ArrayList<geoLocation.Country> GetCountryList(){
        return new ArrayList<>(Arrays.asList(geoLocation.Country.values()));
    }

    public String GetCountryName(geoLocation.Country country){
        return country.name();
    }
    public String GetCountryFullName(geoLocation.Country country){
        return country.getCountryFullName();
    }
    public String GetCountryCode(geoLocation.Country country){
        if(country.equals(geoLocation.Country.NONE)){
            return "NONE";
        }else{
            return country.getCountryCode();
        }
    }
}
