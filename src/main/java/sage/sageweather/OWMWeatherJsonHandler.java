package sageweather;

import com.google.gson.*;
import org.apache.commons.lang.math.NumberUtils;
import sageweather.IForecastPeriod.Type;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parses Weather Json for OpenWeatherMap
 */
public class OWMWeatherJsonHandler {
    private HashMap<String, String> CodesForDaytime = new HashMap<String, String>();
    private HashMap<String, String> CodesForNighttime = new HashMap<String, String>();

    private String sunrise, sunset;
    private Date recordedDate;
    private long dayStart, dayEnd;
    private IWeatherLocation.Units units;

    private ForecastPeriod current;
    private List<ILongRangeForecast> days = new ArrayList<ILongRangeForecast>();

    public OWMWeatherJsonHandler() {
        BuildWeatherIconLists();
    }

    private void BuildWeatherIconLists() {
        AddIcon("Thunderstorm", "37", "47");
        AddIcon("200", "37", "47");
        AddIcon("201", "37", "47");
        AddIcon("202", "37", "47");
        AddIcon("210", "37", "47");
        AddIcon("211", "37", "47");
        AddIcon("212", "37", "47");
        AddIcon("221", "37", "47");
        AddIcon("230", "37", "47");
        AddIcon("231", "37", "47");
        AddIcon("232", "37", "47");

        AddIcon("Drizzle", "9", "45");
        AddIcon("300", "9", "45");
        AddIcon("301", "9", "45");
        AddIcon("302", "9", "45");
        AddIcon("310", "9", "45");
        AddIcon("311", "9", "45");
        AddIcon("312", "9", "45");
        AddIcon("313", "9", "45");
        AddIcon("314", "9", "45");
        AddIcon("321", "9", "45");

        AddIcon("Rain", "40", "45");
        AddIcon("500", "11", "45");
        AddIcon("501", "12", "45");
        AddIcon("502", "40", "45");
        AddIcon("503", "40", "45");
        AddIcon("504", "40", "45");
        AddIcon("511", "7", "7");
        AddIcon("520", "11", "45");
        AddIcon("521", "12", "45");
        AddIcon("522", "40", "45");
        AddIcon("531", "40", "45");

        AddIcon("Snow", "41", "46");
        AddIcon("600", "13", "46");
        AddIcon("601", "14", "46");
        AddIcon("602", "16", "46");
        AddIcon("611", "7", "46");
        AddIcon("612", "6", "46");
        AddIcon("613", "5", "46");
        AddIcon("615", "5", "46");
        AddIcon("616", "7", "46");
        AddIcon("620", "13", "46");
        AddIcon("621", "14", "46");
        AddIcon("622", "16", "46");

        AddIcon("Mist", "11", "11");
        AddIcon("701", "11", "11");

        AddIcon("Smoke", "22", "22");
        AddIcon("711", "22", "22");

        AddIcon("Haze", "21", "21");
        AddIcon("721", "21", "21");

        AddIcon("Dust", "19", "19");
        AddIcon("731", "19", "19");
        AddIcon("761", "19", "19");

        AddIcon("Fog", "20", "20");
        AddIcon("741", "20", "20");

        AddIcon("Sand", "19", "19");
        AddIcon("751", "19", "19");

        AddIcon("Ash", "22", "22");
        AddIcon("762", "22", "22");

        AddIcon("Squall", "35", "35");
        AddIcon("771", "35", "35");

        AddIcon("Tornado", "23", "24");
        AddIcon("781", "23", "24");

        AddIcon("Clear", "32", "31");
        AddIcon("800", "32", "31");

        AddIcon("Clouds", "26", "26");
        AddIcon("801", "34", "33");
        AddIcon("802", "34", "33");
        AddIcon("803", "30", "29");
        AddIcon("804", "28", "27");

        AddIcon("unknown", "-1", "-1");
        AddIcon(IForecastPeriod.sInvalid, "-1", "-1");
    }

    private void AddIcon(String CodeSource, String CodeForDay, String CodeForNight) {
        CodesForDaytime.put(CodeSource, CodeForDay);
        CodesForNighttime.put(CodeSource, CodeForNight);
    }

    private Integer GetCodeFromName(String IconName, IForecastPeriod.Type DayType) {
        Integer tIcon = -1;
        if (IconName == null) {
            return -1;
        } else {
            if (DayType.equals(IForecastPeriod.Type.Current) || DayType.equals(IForecastPeriod.Type.Day)) {
                if (CodesForDaytime.containsKey(IconName)) {
                    return NumberUtils.toInt(CodesForDaytime.get(IconName), tIcon);
                } else {
                    return -1;
                }
            } else {
                if (CodesForNighttime.containsKey(IconName)) {
                    return NumberUtils.toInt(CodesForNighttime.get(IconName), tIcon);
                } else {
                    return -1;
                }
            }
        }
    }

    public void parse(String urlString) throws IOException, JsonIOException {
        this.parse(urlString, IWeatherLocation.Units.Standard);
    }

    public void parse(String urlString, IWeatherLocation.Units inUnits) throws IOException, JsonIOException {
        units = inUnits;
        final String requestTZ;

        //read the url and get the JSON results as a string
        String data = utils.getContentAsString(urlString);

        // Convert to a JSON object to get the elements
        JsonParser jp = new JsonParser(); //from gson
        JsonElement rootElement = jp.parse(data); //Convert the input stream to a json element
        JsonObject root = rootElement.getAsJsonObject(); //May be an array, may be an object.

        JsonObject currently = root.getAsJsonObject("current");
        if (currently == null) throw new IOException("JSON Response for 'currently' Weather did not contain a valid response");
        requestTZ = root.get("timezone").getAsString();

        current = new ForecastPeriod();

        recordedDate = utils.GetJSONAsDate("dt", currently);
        current.setDate(recordedDate);
        current.setType(Type.Current);
        current.setTemp(utils.GetJSONAsInteger("temp", currently));

        //get weather elements from currently
        JsonArray weatherDetailsArray = currently.getAsJsonArray("weather");
        JsonElement weatherDetailsItem = weatherDetailsArray.get(0);
        JsonObject weatherDetails = weatherDetailsItem.getAsJsonObject();
        current.setCode(GetCodeFromName(utils.GetJSONAsString("id", weatherDetails),Type.Current));
        current.setCondition(utils.GetJSONAsString("main", weatherDetails));
        current.setDescription(utils.GetJSONAsString("description", weatherDetails));

        current.setHumid(FormatIntegerasPercent("humidity", currently));

        //get rain details if available from current then get the 1h accumulation
        if (currently.has("rain")) {
            JsonObject rainDetails = currently.getAsJsonObject("rain");
            current.setPrecipAccumulation(FormatPrecipAccumulation("1h", rainDetails));
            current.setPrecipType(IForecastPeriod.PrecipType.Rain);
        }else if (currently.has("snow")){
            JsonObject snowDetails = currently.getAsJsonObject("snow");
            current.setPrecipAccumulation(FormatPrecipAccumulation("1h", snowDetails));
            current.setPrecipType(IForecastPeriod.PrecipType.Snow);
        }else{
            current.setPrecipAccumulation(IForecastPeriod.PercentNone);
            current.setPrecipType(IForecastPeriod.PrecipType.None);
        }

        current.setCloudCover(FormatIntegerasPercent("clouds",currently));

        current.setWindSpeed(utils.GetJSONAsInteger("wind_speed",currently));
        if (current.getWindSpeed() == 0) {
            current.setWindDir(IForecastPeriod.iInvalid);
            current.setWindDirText(IForecastPeriod.WindCalm);
        } else {
            current.setWindDir(utils.GetJSONAsInteger("wind_deg", currently));
            current.setWindDirText(formatCompassDirection(current.getWindDir()));
        }

        current.setDewPoint(String.valueOf(utils.GetJSONAsInteger("dew_point", currently)));
        current.setFeelsLike(utils.GetJSONAsInteger("feels_like", currently));
        current.setPressure(utils.GetJSONAsString("pressure", currently));
        current.setPressureDir(IForecastPeriod.iNotSupported);
        current.setUVIndex(utils.GetJSONAsString("uvi", currently));
        current.setVisibility(utils.GetJSONAsInteger("visibility", currently));

        JsonArray daily = root.getAsJsonArray("daily");
        int i=0;
        for (JsonElement itemElement: daily) {
            JsonObject item = itemElement.getAsJsonObject();
            LongRangForecast r = new LongRangForecast();
            ForecastPeriod day = new ForecastPeriod();
            r.setForecastPeriodDay(day);

            days.add(r);

            //Day related items
            day.setDate(utils.GetJSONAsDate("dt", item));
            day.setType(Type.Day);

            //get temp element from daily - use for max day and min night temp (below)
            JsonObject tempDetails = item.getAsJsonObject("temp");
            day.setTemp(utils.GetJSONAsInteger("max", tempDetails));

            //get weather elements from daily
            JsonArray dailyweatherDetailsArray = item.getAsJsonArray("weather");
            JsonElement dailyweatherDetailsItem = dailyweatherDetailsArray.get(0);
            JsonObject dailyweatherDetails = dailyweatherDetailsItem.getAsJsonObject();
            day.setCode(GetCodeFromName(utils.GetJSONAsString("id", dailyweatherDetails),Type.Day));
            day.setCondition(utils.GetJSONAsString("main", dailyweatherDetails));
            day.setDescription(utils.GetJSONAsString("description", dailyweatherDetails));

            day.setHumid(FormatIntegerasPercent("humidity", item));
            day.setPrecip(FormatDoubleasPercent("pop",item));

            //get rain details if available from current then get the 1h accumulation
            if (item.has("rain")) {
                day.setPrecipAccumulation(FormatPrecipAccumulation("rain", item));
                day.setPrecipType(IForecastPeriod.PrecipType.Rain);
            }else if (item.has("snow")){
                day.setPrecipAccumulation(FormatPrecipAccumulation("snow", item));
                day.setPrecipType(IForecastPeriod.PrecipType.Snow);
            }else{
                day.setPrecipAccumulation(IForecastPeriod.PercentNone);
                day.setPrecipType(IForecastPeriod.PrecipType.None);
            }

            day.setCloudCover(FormatIntegerasPercent("clouds",item));

            day.setWindSpeed(utils.GetJSONAsInteger("wind_speed",item));
            if (day.getWindSpeed() == 0) {
                day.setWindDir(IForecastPeriod.iInvalid);
                day.setWindDirText(IForecastPeriod.WindCalm);
            } else {
                day.setWindDir(utils.GetJSONAsInteger("wind_deg", item));
                day.setWindDirText(formatCompassDirection(day.getWindDir()));
            }

            day.setDewPoint(String.valueOf(utils.GetJSONAsInteger("dew_point", item)));

            //get feels_like element from daily - use for day and night feelslike (below)
            JsonObject feelsDetails = item.getAsJsonObject("feels_like");
            day.setFeelsLike(utils.GetJSONAsInteger("day", feelsDetails));

            day.setPressure(utils.GetJSONAsString("pressure", item));
            day.setPressureDir(IForecastPeriod.iNotSupported);
            day.setMoonPhase(GetMoonPhase(utils.GetJSONAsDouble("moon_phase",item)));

            dayStart = formatStartEnd(utils.GetJSONAsString("sunrise", item),requestTZ);
            dayEnd = formatStartEnd(utils.GetJSONAsString("sunset", item),requestTZ);
            sunrise = formatSunriseSunset(utils.GetJSONAsString("sunrise", item),requestTZ);
            sunset = formatSunriseSunset(utils.GetJSONAsString("sunset", item),requestTZ);
            day.setSunrise(sunrise);
            day.setSunset(sunset);
            //set the sunrise and sunset and moonPhase from the first day to the current forecast
            if (i==0){
                current.setDayStart(dayStart);
                current.setDayEnd(dayEnd);
                current.setSunrise(sunrise);
                current.setSunset(sunset);
                current.setMoonPhase(day.getMoonPhase());
                current.setPrecip(day.getPrecip());
            }

            day.setUVIndex(utils.GetJSONAsString("uvi", item));
            day.setVisibility(IForecastPeriod.iNotSupported);

            //Copy the day forecast to the night as most fields are the same
            ForecastPeriod night = new ForecastPeriod(day);
            r.setForecastPeriodNight(night);

            //Night related items that are different from the day items
            night.setType(Type.Night);

            night.setTemp(utils.GetJSONAsInteger("min", tempDetails)); //tempDetail created above when getting day temp
            night.setFeelsLike(utils.GetJSONAsInteger("night", feelsDetails));
            night.setCode(GetCodeFromName(utils.GetJSONAsString("id", dailyweatherDetails),Type.Night));

            i++;
        }

    }

    private String FormatIntegerasPercent(String key, JsonObject item){
        Integer value = utils.GetJSONAsInteger(key,item);
        if (value.equals(IForecastPeriod.iInvalid)){
            return IForecastPeriod.sInvalid;
        }
        if (value==0)return IForecastPeriod.PercentNone;
        return String.valueOf(value.intValue()) + "%";
    }

    private String FormatDoubleasPercent(String key, JsonObject item){
        Double value = utils.GetJSONAsDouble(key,item,100);
        if (value.equals(IForecastPeriod.dInvalid)){
            return IForecastPeriod.sInvalid;
        }
        if (value==0)return IForecastPeriod.PercentNone;
        return String.valueOf(value.intValue()) + "%";
    }

    private int GetMoonPhase(Double moonPhasePercent){
        if (moonPhasePercent.equals(IForecastPeriod.dInvalid))return IForecastPeriod.iInvalid;
        return (int) Math.floor(moonPhasePercent*30);
    }

    private String FormatPrecipAccumulation(String key, JsonObject item){
        String tPA = item.get(key).getAsString();
        if (tPA==null){
            return IForecastPeriod.PercentNone;
        }else{
            Double dPA = NumberUtils.toDouble(tPA);
            //OWM provides mm so convert if using Standard measures
            if (units.equals(IWeatherLocation.Units.Standard)){
                dPA = dPA / 25.4;
            }
            if (dPA<1){
                if (units.equals(IWeatherLocation.Units.Standard)){
                    return "<1 inch";
                }else{
                    return "<1 mm";
                }
            }else{
                String suffix = "inches";
                if (units.equals(IWeatherLocation.Units.Metric)) suffix = "mm";
                Double roundedPA = Math.round(dPA * 2) / 2.0;
                if (roundedPA == Math.floor(roundedPA)){
                    //integer so show range
                    return (roundedPA.intValue()-1) + "-" +  (roundedPA.intValue()+1) + " " + suffix;
                }else{
                    return (roundedPA.intValue()) + "-" +  (roundedPA.intValue()+1) + " " + suffix;
                }
                //return roundedPA.toString();
            }
        }
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public List<ILongRangeForecast> getDays() {
        return days;
    }

    public ForecastPeriod getCurrent() {
        return current;
    }

    private String formatCompassDirection(int degrees) {
        if (degrees==IForecastPeriod.iInvalid) return IForecastPeriod.sInvalid;
        String[] directions = new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW",
                "NW", "NNW"};
        int index = (int) ((degrees / 22.5) + .5);
        return directions[index % 16];
    }

    private String formatSunriseSunset(String sDate, String TZ) {
        if (sDate.equals(IForecastPeriod.sInvalid)){
            return IForecastPeriod.sInvalid;
        }
        Date dDate = utils.convertUNIXDate(sDate);
        SimpleDateFormat sdfRequestTZ = new SimpleDateFormat("h:mm");
        TimeZone tzRequestTZ = TimeZone.getTimeZone(TZ);
        sdfRequestTZ.setTimeZone(tzRequestTZ);
        return sdfRequestTZ.format(dDate);
    }

    private long formatStartEnd(String sDate, String TZ) {
        if (sDate.equals(IForecastPeriod.sInvalid)){
            return IForecastPeriod.iInvalid;
        }
        Date dDate = utils.convertUNIXDate(sDate);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TZ));
        SimpleDateFormat thisDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        try {
            return thisDateFormat.parse( simpleDateFormat.format(dDate) ).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


}
