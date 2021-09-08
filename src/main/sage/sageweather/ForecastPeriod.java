package sageweather;

import java.io.Serializable;
import java.util.Date;

public class ForecastPeriod implements IForecastPeriod, Serializable {
    private static final long serialVersionUID = 1L;

    private Date date;
    private Type type;
    private int temp;
    private int code;
    private String condition;
    private String humid;
    private String precip;
    private String precipAccumulation;
    private String cloudCover;
    private int windDir;
    private String windDirText;
    private int windSpeed;
    private String dewPoint;
    private int feelsLike;
    private String pressure;
    private int pressureDir;
    private int moonPhase;
    private String sunrise;
    private String sunset;
    private long dayStart;
    private long dayEnd;
    private String UVIndex;
    private int visibility;
    private String description;
    private PrecipType precipType;

    public ForecastPeriod() {
    }

    public ForecastPeriod(ForecastPeriod copyFrom) {
        this.date = copyFrom.date;
        this.type = copyFrom.type;
        this.temp = copyFrom.temp;
        this.code = copyFrom.code;
        this.condition = copyFrom.condition;
        this.humid = copyFrom.humid;
        this.precip = copyFrom.precip;
        this.precipAccumulation = copyFrom.precipAccumulation;
        this.precipType = copyFrom.precipType;
        this.cloudCover = copyFrom.cloudCover;
        this.windDir = copyFrom.windDir;
        this.windDirText = copyFrom.windDirText;
        this.windSpeed = copyFrom.windSpeed;
        this.dewPoint = copyFrom.dewPoint;
        this.feelsLike = copyFrom.feelsLike;
        this.pressure = copyFrom.pressure;
        this.pressureDir = copyFrom.pressureDir;
        this .moonPhase = copyFrom.moonPhase;
        this.sunrise = copyFrom.sunrise;
        this.sunset = copyFrom.sunset;
        this.UVIndex = copyFrom.UVIndex;
        this.visibility = copyFrom.visibility;
        this.description = copyFrom.description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ForecastPeriod{" +
                "date=" + date +
                ", type=" + type +
                ", temp=" + temp +
                ", code=" + code +
                ", condition='" + condition + '\'' +
                ", humid=" + humid +
                ", precip='" + precip + '\'' +
                ", precipAccumulation='" + precipAccumulation + '\'' +
                ", precipType='" + precipType.toString() + '\'' +
                ", cloudCover='" + cloudCover + '\'' +
                ", windDir=" + windDir +
                ", windDirText='" + windDirText + '\'' +
                ", windSpeed=" + windSpeed +
                ", dewPoint='" + dewPoint + '\'' +
                ", feelsLike=" + feelsLike +
                ", pressure='" + pressure + '\'' +
                ", pressureDir=" + pressureDir +
                ", moonPhase=" + moonPhase +
                ", sunrise='" + sunrise + '\'' +
                ", sunset='" + sunset + '\'' +
                ", UVIndex='" + UVIndex + '\'' +
                ", visibility=" + visibility +
                ", description='" + description + '\'' +
                '}';
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHumid() {
        return humid;
    }

    public void setHumid(String humid) {
        this.humid = humid;
    }

    public int getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(int moonPhase) {
        this.moonPhase = moonPhase;
    }

    public String getPrecip() {
        return precip;
    }

    public void setPrecip(String precip) {
        this.precip = precip;
    }

    public String getPrecipAccumulation() {
        return precipAccumulation;
    }

    public void setPrecipAccumulation(String precipAccumulation) {
        this.precipAccumulation = precipAccumulation;
    }

    @Override
    public PrecipType getPrecipType() {
        return precipType;
    }
    public void setPrecipType( PrecipType precipType){
        this.precipType = precipType;
    }

    public int getWindDir() {
        return windDir;
    }

    public void setWindDir(int windDir) {
        this.windDir = windDir;
    }

    public String getWindDirText() {
        return windDirText;
    }

    public void setWindDirText(String windDirText) {
        this.windDirText = windDirText;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(String cloudCover) {
        this.cloudCover = cloudCover;
    }

    public String getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(String dewPoint) {
        this.dewPoint = dewPoint;
    }

    public int getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(int feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public int getPressureDir() {
        return pressureDir;
    }

    public void setPressureDir(int pressureDir) {
        this.pressureDir = pressureDir;
    }

    @Override
    public long getDayStart() {
        return dayStart;
    }

    public void setDayStart(long dayStart) {
        this.dayStart = dayStart;
    }

    @Override
    public long getDayEnd() {
        return dayEnd;
    }

    public void setDayEnd(long dayEnd) {
        this.dayEnd = dayEnd;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getUVIndex() {
        return UVIndex;
    }

    public void setUVIndex(String uVIndex) {
        UVIndex = uVIndex;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public String getTempFormatted(IWeatherLocation.Units units){
        String tempUnits = "C";
        if (units.equals(IWeatherLocation.Units.Metric)){
            tempUnits = "C";
        }
        return this.temp + tempUnits;
    }

}
