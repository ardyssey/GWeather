package sageweather;

import java.util.Date;

/**
 * Interface to access the Forecast data for a Period of a day or the Current
 * Weather A Period is typically a Day or a Night where a day will have 2
 * periods A period could be the Current Period or Current Weather note: not all
 * fields will be populated depending on the implementation - non-implemented
 * string fields should return null - non-implemented integer fields should
 * return
 *
 * @author jusjoken
 */
public interface IForecastPeriod {
    /**
     * Used to determine if the period is Day or Night
     */
    public static enum Type {
        Day, Night, Current
    }

    /**
     * Used to configure the PrecipType for a weather source.
     */
    public static enum PrecipType {
        Rain, Snow, None
    }


    public static final String sNotSupported = "sNotSupported";
    public static final int iNotSupported = Integer.MAX_VALUE - 1;
    public static final Double dNotSupported = Double.MAX_VALUE - 1;
    public static final String sInvalid = "sInvalid";
    public static final int iInvalid = Integer.MAX_VALUE;
    public static final Double dInvalid = Double.MAX_VALUE;
    public static final Date dateInvalid = new Date(0);
    public static final String WindCalm = "Calm";
    public static final String PercentNone = "None";

    /**
     * Returns the {@link Date} for this specific forecast period
     *
     * @return {@link Date}
     */
    public Date getDate();

    /**
     * Returns the type of this period (Day/Night/Current)
     *
     * @return period type (Day or Night or Current)
     */
    public IForecastPeriod.Type getType();

    /**
     * Returns the temperature for the current sageweather forecast period with units
     *
     * @return current temp with units
     */
    public String getTempFormatted(IWeatherLocation.Units units);

    /**
     * Returns the temperature for the current weather forecast period.
     *
     * @return current temp
     */
    public int getTemp();

    /**
     * Returns a short descriptive text for this specific forecast period. It
     * will be something like, "Mostly Cloudy", or "Partly Sunny"
     *
     * @return short description of the periods weather condition.
     */
    public String getCondition();

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
    public int getCode();

    /**
     * Return the Formatted probability of precipitation for the forecast period in %
     * should be fully formatted for display
     *
     * @return Period Precipitation
     */
    public String getPrecip();

    /**
     * Return the PrecipAccumulation (Rain or Snow) for the forecast period
     *  - this string should be full formatted for display including the units
     *
     * @return PrecipAccumulation
     */
    public String getPrecipAccumulation();

    /**
     * Return the humidity for the forecast Period - should be -1 if invalid or
     * unavailable
     * Should be fully formatted for display
     *
     * @return Period Humidity
     */
    public String getHumid();

    /**
     * Returns the wind speed for the forecast period. - should be 0 for CALM -
     * should be -1 if invalid or unavailable
     *
     * @return wind speed
     */
    public int getWindSpeed();

    /**
     * Returns the degrees wind direction for the forecast period.
     *
     * @return wind direction in degrees
     */
    public int getWindDir();

    /**
     * Returns the wind direction for the forecast period.
     *
     * @return wind direction as text
     */
    public String getWindDirText();

    /**
     * Return the Long Description of the forecast Period
     *
     * @return long Period Description
     */
    public String getDescription();

    /**
     * Return the Formatted cloud cover for the forecast period
     *
     * @return formatted Cloud Cover
     */
    public String getCloudCover();

    /**
     * Returns the start of the Day based on sunrise
     *
     * @return Date
     */
    public long getDayStart();

    /**
     * Returns the end of the Day based on sunset
     *
     * @return Date
     */
    public long getDayEnd();

    /**
     * Returns the Sunrise time, such as, "7:28 am", if known
     *
     * @return formatted time of sunrise
     */
    public String getSunrise();

    /**
     * Return the Sunset time, such as, "4:53 pm", if known
     *
     * @return formatted time of sunset
     */
    public String getSunset();

    /**
     * Return the Visibility for the forecast period
     *
     * @return Visibility
     */
    public int getVisibility();

    /**
     * Return the Windchill or Feels like temperature for the forecast period
     *
     * @return Feel Like Temp
     */
    public int getFeelsLike();

    /**
     * Return the Barometric Pressure for the forecast period
     *
     * @return Pressure
     */
    public String getPressure();

    /**
     * Return an indicator of the Barometric Pressure rising/falling for the
     * forecast period.
     *  - 0 steady (default) 1 rising -1 falling
     *
     * @return Pressure Direction
     */
    public int getPressureDir();

    /**
     * Return the DewPoint for the forecast period
     *
     * @return DewPoint
     */
    public String getDewPoint();

    /**
     * Return the UVIndex for the forecast period
     *
     * @return UVIndex
     */
    public String getUVIndex();

    /**
     * Return the MoonPhase for the forecast period
     *  - an integer between 0 and 29 indicating the phase of the moon
     *  - to be used to display a specific moon phase image in the UI
     *  - 0 indicates a New Moon
     *
     * @return MoonPhase
     */
    public int getMoonPhase();

    /**
     * Return the PrecipType for the forecast period
     *  - either Snow or Rain
     *  - to be used to determine if the precipaccumulation is snow or rain
     *
     * @return PrecipType
     */
    public PrecipType getPrecipType();

}
