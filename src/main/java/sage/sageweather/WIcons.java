package sageweather;

import java.io.File;
import java.util.*;

/**
 * Created by jusjoken on 7/28/2021.
 */
public class WIcons {

    public static final String OptionNotFound = "Option not Found";
    public static final String WIconProp = "WIcons/";
    public static Map<String,String> IconsForDaytime = new HashMap<String, String>();
    public static Map<String,String> IconsForNighttime = new HashMap<String, String>();
    public static Boolean IconsBuilt = Boolean.FALSE;
    public static void BuildWeatherIconLists(){
        //start google Icons here
        AddIcon("sunny", "32", "31");
        AddIcon("mostly_sunny", "34", "33");
        AddIcon("partly_cloudy", "30", "29");
        AddIcon("mostly_cloudy", "28", "27");
        AddIcon("chance_of_storm", "37", "47");
        AddIcon("rain", "12", "12");
        AddIcon("chance_of_rain", "39", "45");
        AddIcon("chance_of_snow", "41", "46");
        AddIcon("cloudy", "26", "26");
        AddIcon("mist", "11", "11");
        AddIcon("storm", "35", "35");
        AddIcon("thunderstorm", "35", "35");
        AddIcon("chance_of_tstorm", "37", "47");
        AddIcon("sleet", "5", "5");
        AddIcon("snow", "16", "16");
        AddIcon("icy", "10", "10");
        AddIcon("dust", "19", "19");
        AddIcon("fog", "20", "20");
        AddIcon("smoke", "22", "22");
        AddIcon("haze", "21", "21");
        AddIcon("flurries", "14", "14");
        //start NWS Icons here - those begining with a "n" are the night icons (if one exists)
        //NWS is only used for Forecasts - so only the "day" or first entry is used
        AddIcon("nbkn", "27", "27");
        AddIcon("bkn", "28", "27");
        AddIcon("nra", "12", "12");
        AddIcon("ra", "12", "12");
        AddIcon("nskc", "31", "31");
        AddIcon("skc", "32", "31");
        AddIcon("nfew", "33", "33");
        AddIcon("few", "34", "33");
        AddIcon("nsct", "29", "29");
        AddIcon("sct", "30", "29");
        AddIcon("hi_nshwrs", "45", "45");
        AddIcon("hi_shwrs", "39", "45");
        AddIcon("novc", "26", "26");
        AddIcon("ovc", "26", "26");
        AddIcon("nrasn", "46", "46");
        AddIcon("rasn", "41", "46");
        AddIcon("sn", "16", "16");
        AddIcon("nsn", "16", "16");
        AddIcon("ntsra", "35", "35");
        AddIcon("tsra", "35", "35");
        AddIcon("nscttsra", "35", "35");
        AddIcon("scttsra", "35", "35");
        AddIcon("hi_tsra", "37", "37");
        AddIcon("hi_ntsra", "47", "47");
        AddIcon("nwind", "24", "24");
        AddIcon("wind", "23", "24");
        AddIcon("sctfg", "20", "20");
        AddIcon("nfg", "20", "20");
        AddIcon("fg", "20", "20");
        AddIcon("cold", "15", "15");
        AddIcon("blizzard", "43", "43");
        AddIcon("ntor", "24", "24");
        AddIcon("tor", "23", "24");
        AddIcon("fzra", "5", "5");
        AddIcon("du", "19", "19");
        AddIcon("nshra", "12", "12");
        AddIcon("shra", "12", "12");
        AddIcon("nfu", "22", "22");
        AddIcon("fu", "22", "22");
        AddIcon("hot", "32", "31");
    }
    private static void AddIcon(String IconSource, String IconForDay, String IconForNight){
        IconsForDaytime.put(IconSource, IconForDay);
        IconsForNighttime.put(IconSource, IconForNight);
    }

    public static void init(){
    }

    public static String ConvertURLtoCondition(String ConditionURL, Boolean ForceDay){
        String Condition = "";
        Integer tIcon = ConditionURL.lastIndexOf("/");
        if (tIcon==-1){
            return ConditionURL;
        }else{
            Condition = ConditionURL.substring(tIcon+1);
            Condition = Condition.replaceAll(".gif", "");
            Condition = Condition.replaceAll(".jpg", "");
            Condition = Condition.replaceAll(".png", "");
            //remove any % that are part of the image string
            if (Condition.contains("0")){
                Condition = Condition.replaceAll("10", "");
                Condition = Condition.replaceAll("20", "");
                Condition = Condition.replaceAll("30", "");
                Condition = Condition.replaceAll("40", "");
                Condition = Condition.replaceAll("50", "");
                Condition = Condition.replaceAll("60", "");
                Condition = Condition.replaceAll("70", "");
                Condition = Condition.replaceAll("80", "");
                Condition = Condition.replaceAll("90", "");
            }

            String tCondition = GetWeatherIcon(Condition, ForceDay);
            //Log.info("WIcons","ConvertURLtoCondition: Condition '" + Condition + "' tCondition '" + tCondition + "' URL '" + ConditionURL + "'");
            if (tCondition.equals(Condition)){
                return ConditionURL;
            }else{
                return tCondition;
            }

        }
    }

    public static String GetWeatherIconURLDay(String ConditionURL){
        String tCondition = ConvertURLtoCondition(ConditionURL, Boolean.TRUE);
        if (tCondition.equals(ConditionURL)){
            Log.info("WIcons","WIcons: GetWeatherIconURLDay - unhandled url - please report '" + ConditionURL + "'");
            return ConditionURL;
        }else{
            return GetWeatherPath() + tCondition + ".png";
        }
    }
    public static String GetWeatherIconURL(String ConditionURL){
        //Log.info("WIcons","GetWeatherIconURL: ConditionURL '" + ConditionURL + "'");
        String tCondition = ConvertURLtoCondition(ConditionURL, Boolean.FALSE);
        if (tCondition.equals(ConditionURL)){
            Log.info("WIcons","WIcons: GetWeatherIconURL - unhandled url - please report '" + ConditionURL + "'");
            return ConditionURL;
        }else{
            return GetWeatherPath() + tCondition + ".png";
        }
    }
    public static String GetWeatherIconByNumber(String ConditionNumber){
        //Log.info("WIcons","GetWeatherIconByNumber: returning '" + GetWeatherPath() + ConditionNumber + ".png'");
        return GetWeatherPath() + ConditionNumber + ".png";
    }
    public static String GetWeatherIconByNumber(int ConditionNumber){
        //Log.info("WIcons","GetWeatherIconByNumber: returning '" + GetWeatherPath() + ConditionNumber + ".png'");
        return GetWeatherPath() + ConditionNumber + ".png";
    }

    public static String GetWeatherIconByNumber(String ConditionNumber, String WeatherSet){
        //Log.info("WIcons","GetWeatherIconByNumber: returning '" + GetWeatherPath() + ConditionNumber + ".png'");
        return GetWeatherPath(WeatherSet) + ConditionNumber + ".png";
    }

    public static String GetWeatherIconByNumber(int ConditionNumber, String WeatherSet){
        //Log.info("WIcons","GetWeatherIconByNumber: returning '" + GetWeatherPath() + ConditionNumber + ".png'");
        return GetWeatherPath(WeatherSet) + ConditionNumber + ".png";
    }

    public static String GetWeatherIconNoURLDay(String ConditionURL){
        String tCondition = ConvertURLtoCondition(ConditionURL, Boolean.TRUE);
        if (tCondition.equals(ConditionURL)){
            Log.info("WIcons","WIcons: GetWeatherIconNoURLDay - unhandled url - please report '" + ConditionURL + "'");
            return "-1";
        }else{
            return tCondition;
        }
    }
    public static String GetWeatherIconNoURL(String ConditionURL){
        String tCondition = ConvertURLtoCondition(ConditionURL, Boolean.FALSE);
        if (tCondition.equals(ConditionURL)){
            Log.info("WIcons","WIcons: GetWeatherIconNoURL - unhandled url - please report '" + ConditionURL + "'");
            return "-1";
        }else{
            return tCondition;
        }
    }

    public static String GetWeatherIconDay(String Condition){
        return GetWeatherIcon(Condition, Boolean.TRUE);
    }
    public static String GetWeatherIcon(String Condition){
        return GetWeatherIcon(Condition, Boolean.FALSE);
    }
    public static String GetWeatherIcon(String Condition, Boolean ForceDay){
        if (!IconsBuilt){
            BuildWeatherIconLists();
            IconsBuilt = Boolean.TRUE;
        }
        String returnIcon = Condition;
        String DefaultIcon = "";
        if (IsDaytime() || ForceDay){
            if (IconsForDaytime.containsKey(Condition)){
                DefaultIcon = IconsForDaytime.get(Condition);
            }else{
                DefaultIcon = Condition;
            }
            returnIcon = DefaultIcon;
        }else{
            if (IconsForNighttime.containsKey(Condition)){
                DefaultIcon = IconsForNighttime.get(Condition);
            }else{
                DefaultIcon = Condition;
            }
            returnIcon = DefaultIcon;
        }
        //Log.info("WIcons","GetWeatherIcon: returning '" + returnIcon + "' IsDaytime '" + IsDaytime() + "' ForceDay '" + ForceDay + "'");
        return returnIcon;
    }

    public static Boolean IsDaytime(){
        return IsDaytime(null);
    }
    public static Boolean IsDaytime(IForecastPeriod currentForecastPeriod){
        if(currentForecastPeriod==null){
            //as we do not have a currentForecast then use old hardcoded method
            Calendar myCalendar = Calendar.getInstance();
            Integer currentHour = myCalendar.get(Calendar.HOUR_OF_DAY);
            Integer DayStartHour = 7;
            Integer DayEndHour = 19;
            if (currentHour>=DayStartHour && currentHour<DayEndHour){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }else{
            long DayStart = currentForecastPeriod.getDayStart();
            long DayEnd = currentForecastPeriod.getDayEnd();
            long Now = System.currentTimeMillis();
            if(Now>=DayStart && Now<DayEnd){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }
    }

    public static String GetWeatherPath(){
        String tSet = utils.getWeatherIconSet();
        if (tSet.equals(Const.OptionNotFound)){
            return "WeatherIcons\\Images\\";
        }else{
            return GetWeatherPath(tSet);
        }
    }
    public static String GetWeatherPath(String WeatherSet){
        return utils.WeatherLocation() + File.separator + "Icons" + File.separator + WeatherSet + File.separator;
    }

    //functions for managing sets of icons
    public static String GetIconSet(){
        String retVal = utils.getWeatherIconSet();
        if(retVal.isEmpty() || retVal.equals(Const.OptionNotFound)){
            return "Not Set";
        }else{
            return retVal;
        }
    }

    public static void SetIconSet(String iconSet){
        utils.setWeatherIconSet(iconSet);
    }

    public static Collection<String> GetIconSets(){
        SortedSet<String> tList = new TreeSet<String>();
        File IconSetLoc = new File(new File(utils.WeatherLocation()), "Icons");
        File[] files = IconSetLoc.listFiles();
        for (File file : files){
            if (file.isDirectory()){
                tList.add(file.getName());
            }
        }
        Log.info("WIcons","GetIconSets: found '" + tList + "'");
        return tList;
    }

    public static Collection<String> GetIconList(){
        Collection<String> tList = new LinkedHashSet<String>();
        for ( Integer i = 0 ; i < 48 ; i++ ) {
            tList.add(i.toString());
        }
        tList.add("na");
        return tList;
    }




}
