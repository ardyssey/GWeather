package sageweather;

import java.util.Date;
import java.util.List;

/**
 * @author jusjoken
 */
public interface IWeatherLocation {
    /**
     * Used to configure the Units for a weather source.
     */
    public static enum Units {
        Metric, Standard
    }

    /**
     * Returns the name for this specific Weather Source
     *
     * @return Source name
     */
    public String getSourceName();

    /**
     * Returns true if the sageweather source update was successful.
     *
     * @return true if the update was retrieved
     */
    public boolean update();

    /**
     * Returns the geoLocation class used by the weather source
     * this will contain location details such as name, lat, long, units etc.
     *
     * @return specific location
     */
    public geoLocation getLocation();

    /**
     * Returns the location name used by the weather source. typically a City
     * Name
     *
     * @return specific location code
     */
    public String getLocationName();

    /**
     * Returns the units the source uses during updates.
     *
     * @return {@link Units}
     */
    public Units getUnits();

    /**
     * Sets the units the source uses during updates.
     *
     */
    public void setUnits(Units units);

    /**
     * Returns the CurrentWeather retrieved from the last update. the
     * implementation should ensure an update is called prior to this.
     *
     * @return {@link IForecastPeriod}
     */
    public IForecastPeriod getCurrentWeather();

    /**
     * Returns a list of all available days of Long Range Forecasts. the
     * implementation should ensure an update is called prior to this.
     *
     * @return list of {@link ILongRangeForecast}
     */
    public List<ILongRangeForecast> getForecasts();

    /**
     * Returns the number of days available in the Long Range Forecast
     *
     * @return count of Long Range Forecast Days
     */
    public int getForecastDays();

    /**
     * Verifies if the weather source has a valid configuration
     *
     * @return true if a valid configuration is set
     */
    public boolean isConfigured();

    /**
     * Returns the {@link Date} this source was last refreshed this is the date
     * an update to the weather data was requested
     *
     * @return {@link Date} last updated
     */
    public Date getLastUpdated();

    /**
     * Returns the {@link Date} the weather data was recorded by the source this
     * is the date the source API returns if available
     *
     * @return {@link Date} recorded by source
     */
    public Date getRecordedDate();

    /**
     * Returns if any error has occurred updating or changing settings of the
     * weather source
     *
     * @return true if an error has occurred
     */
    public boolean hasError();

    /**
     * Returns the error message from the last update or change of settings
     *
     * @return error message if any
     */
    public String getError();

    /**
     * Returns if the weather provider needs a User Key for full functionality
     *
     * @return true if a User Provided Key is needed
     */
    public boolean needUserKey();

    /**
     * Returns if a User Provided key has been set
     *
     * @return true if a User Provided Key has been set
     */
    public boolean hasUserKey();

    /**
     * Returns the User Provided key
     *
     * @return the User Provided Key
     */
    public String getUserKey();

}
