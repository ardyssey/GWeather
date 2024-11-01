package sageweather;

import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import static sageweather.OWM.wProps;

/**
 * Created by jusjoken on 7/10/2021.
 */
public class utils {
    private static boolean testing;
    private static String userKey = "";
    private static int maxDevAPIKeyUse = 3;
    private static int countDevAPIKeyUse = 0;
    private static String apiKeyFileName = "owmkey.properties";
    private static String weatherLocationsFileName = "GWeatherLocations";
    private static String weatherIconSetKey = Const.BaseProp + Const.PropDivider + Const.WeatherProp + Const.PropDivider + "IconSet";


    private static String version = "3.0.3";

    //create a single instance of the OWM class to use for all general weather calls
    private static OWM serverOWM = new OWM();
    private static boolean loaderActive = false;

    //TODO: need a public map of weatherLocations loaded at init

    //TODO: this is temp only as the user will need to provide a key
    private static String sAPI = "861b3f1247bf0ef5b3b904538e4dba15";

    public static void init(){

        WIcons.init();

    }

    public static String getWeatherIconSet() {
        return GetServerProperty(weatherIconSetKey,Const.OptionNotFound);
    }

    public static void setWeatherIconSet(String weatherIconSet) {
        SetServerPropertyWithSave(weatherIconSetKey,weatherIconSet);
    }

    public static OWM getServerOWM() {
        return serverOWM;
    }

    public static boolean isLoaderActive() {
        return loaderActive;
    }

    public static void setLoaderActive(boolean loaderActive) {
        utils.loaderActive = loaderActive;
    }

    public static String WeatherLocation(){
        return GetSageTVRootDir() + File.separator + "STVs" + File.separator + "GWeather" + File.separator + "Weather";
    }

    public static String WeatherLocationBackgrounds(){
        return GetSageTVRootDir() + File.separator + "STVs" + File.separator + "Gemstone" + File.separator + "Weather";
    }

    public static String encode(String data) {
        if (data == null)
            return "";
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.info("utils","utils: encode - Failed to url encode data: " + data + " as UTF-8; will try again using default encoding : " + e);
            return URLEncoder.encode(data);
        }
    }

    public static String getContentAsString(String url) throws IOException {
        URL thisURL = new URL(url);
        URLConnection conn = thisURL.openConnection();
        InputStream is = conn.getInputStream();
        return IOUtils.toString(is, "UTF-8");
    }

    //create a unique id from the lat long of a location to use as a locationID
    //use in properties files to list multiple locations for the user
    public static String getUniqueId(double lat,double lon) {
        long lat_int = (long) (lat * 10000000);
        long lon_int = (long) (lon * 10000000);
        long latMask = lat_int > 0x7FFF0000L ? 0x1ffff0000L : 0xffff0000L;
        long val=Math.abs(lat_int << 16 &  latMask | lon_int & 0x0000ffff);
        val = val % Integer.MAX_VALUE;
        Log.info("utils","utils: getUniqueId - generated: " + val);
        return String.valueOf(val);
    }

    public static boolean hasUserKey(){
        return !StringUtils.isEmpty(utils.getUserKey());
    }

    public static String getAPIKey(){
        if (utils.hasUserKey()){
            return utils.userKey;
        }else {
            return utils.getDevKey();
        }
    }

    public static String getUserKey(){
        if (utils.userKey.isEmpty()){
            String tempKey = GetServerProperty(Const.BaseProp + Const.PropDivider + Const.UserApiKey,"");
            if (tempKey.isEmpty()){
                loadUserKey();
            }else{
                utils.userKey = tempKey;
            }
        }
        return utils.userKey;
    }

    private static String getDevKey(){
        //only allow the user to use the developer key MAX times
        countDevAPIKeyUse++;
        if(!utils.isTesting() && countDevAPIKeyUse>maxDevAPIKeyUse){
            return "";
        }else{
            String k = "";
            k = new StringBuilder(utils.sAPI).reverse().toString();
            return k;
        }
    }

    public static void setUserKey(String userKey) {
        utils.userKey = userKey;
        SetServerPropertyWithSave(Const.BaseProp + Const.PropDivider + Const.UserApiKey,userKey);
    }

    public static boolean isTesting() {
        return testing;
    }

    public static void setTesting(boolean testing) {
        utils.testing = testing;
    }

    /**
     * Load a user supplied API key from the sagetv server properties or from OWM.properties file
     * return true if a user supplied key is found
     */
    private static boolean loadUserKey(){
        boolean bHasUserKey = false;
        //to support test cases ignore the fact that we do not have a user supplied key
        if (testing){
            Log.info("utils","utils: loadUserKey - user key override for testing set");
            return true;
        }

        //if we got this far then the config does not have a user key - try to load it from the properties file
        File UserKeyPropsFile = new File(GetApiKeyLocation());
        String UserKeyPropsPath = UserKeyPropsFile.toString();
        Properties UserKeyProps = new Properties();

        //read the user key from the properties file
        Log.info("utils","utils: loadUserKey - looking for user key in '" + UserKeyPropsPath + "'" );
        String userKey;
        try {
            FileInputStream in = new FileInputStream(UserKeyPropsPath);
            try {
                UserKeyProps.load(in);
                userKey = UserKeyProps.getProperty("key",Const.KEY_NOT_FOUND);
                if (userKey.equals(Const.KEY_NOT_FOUND)){
                    Log.info("utils","utils: loadUserKey - user key not found loading key property from " + apiKeyFileName);
                }else{
                    bHasUserKey = true;
                    utils.setUserKey(userKey);
                    Log.info("utils","utils: loadUserKey - user key found in " + apiKeyFileName + ". Setting the weather config as well." );
                }
            } finally {
                in.close();
            }
        } catch (Exception ex) {
            Log.info("utils","utils: loadUserKey - file not found loading key property from " + apiKeyFileName + ": " + ex);
        }
        return bHasUserKey;
    }


    public static int getUpdateInterval(){
        //TODO: get update intervale from properties
        return 30*60;
    }

    public static String GetJSONAsString(String key, JsonObject item){
        String keyValue = item.get(key).getAsString();
        if (keyValue==null){
            return IForecastPeriod.sInvalid;
        }else{
            return keyValue;
        }
    }

    public static Date GetJSONAsDate(String key, JsonObject item){
        return convertUNIXDate(item.get(key).getAsString());
    }

    public static Double GetJSONAsDouble(String key, JsonObject item, Integer multiplier){
        Double value = GetJSONAsDouble(key,item);
        if (value.equals(IForecastPeriod.dInvalid)){
            return value;
        }
        return value * multiplier;
    }

    public static Double convertStringtoDouble(String input){
        return NumberUtils.toDouble(input,IForecastPeriod.dInvalid);
    }

    public static Double GetJSONAsDouble(String key, JsonObject item){
        String keyValue = item.get(key).getAsString();
        if (keyValue==null){
            return IForecastPeriod.dInvalid;
        }else{
            return convertStringtoDouble(keyValue);
        }
    }

    public static int GetJSONAsInteger(String key, JsonObject item, Integer multiplier){
        Double value = GetJSONAsDouble(key,item,multiplier);
        if (value.equals(IForecastPeriod.dInvalid)){
            return IForecastPeriod.iInvalid;
        }
        return value.intValue();
    }

    public static int GetJSONAsInteger(String key, JsonObject item){
        String keyValue = item.get(key).getAsString();
        if (keyValue==null){
            return IForecastPeriod.iInvalid;
        }else{
            return (int) Math.round(NumberUtils.toDouble(keyValue, IForecastPeriod.iInvalid));
        }
    }

    public static Date convertUNIXDate(String inDate){
        return new java.util.Date(Long.parseLong(inDate)*1000);
    }

    public static String GetApiKeyLocation(){
        return GetSageTVRootDir() + File.separator + apiKeyFileName;
    }

    public static String GetWeatherLocationsFullPath(String propFileSuffix){
        return GetSageTVRootDir() + File.separator + weatherLocationsFileName + propFileSuffix + ".properties";
    }

    public static String GetSageTVRootDir(){
        return System.getProperty("user.dir");
    }

    public static String getVersion() {
        return version;
    }

    public static String GetServerProperty(String Property, String DefaultValue){
        String tValue = wProps.getProperty(Property,DefaultValue);
        if (tValue==null || tValue.equals(Const.OptionNotFound)){
            return DefaultValue;
        }else{
            return tValue;
        }
    }

    public static Integer GetServerPropertyAsInteger(String Property, Integer DefaultValue){
        //read in the Sage Property and force convert it to an Integer
        String tValue = GetServerProperty(Property, null);
        if (tValue==null || tValue.equals(Const.OptionNotFound)){
            return DefaultValue;
        }
        return GetInteger(tValue, DefaultValue);
    }

    public static Integer GetInteger(Object Value, Integer DefaultValue){
        //force a string to an integer or return the default
        if (Value==null){
            return DefaultValue;
        }
        Integer tInteger = DefaultValue;
        try {
            tInteger = Integer.valueOf(Value.toString());
        } catch (NumberFormatException ex) {
            //use DefaultValue
            return DefaultValue;
        }
        return tInteger;
    }

    public static void SetServerProperty(String Property, String Value){
        wProps.put(Property,Value);
    }

    public static void SetServerPropertyWithSave(String Property, String Value){
        wProps.put(Property,Value);
        wProps.save();
    }

    public static long getCurrentUtcTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        try {
            return localDateFormat.parse( simpleDateFormat.format(new Date()) ).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
