package sageweather;


import sageweather.*;

/**
 * Created by jusjoken on 7/13/2021.
 */
public class geoLocation implements Comparable<geoLocation> {
    private String ID;
    private String name;
    private  String country;
    private Double latitude;
    private Double longitude;
    private IWeatherLocation.Units units;
    private OWMWeatherLocation weather;

    public geoLocation() {

    }

    public geoLocation(String name, String country, Double latitude, Double longitude) {
        //no units so default to standard (imperial)
        this(name,country,latitude,longitude, IWeatherLocation.Units.Standard);
    }

    public geoLocation(String name, String country, String latitude, String longitude, String units) {
        this(name,country,utils.convertStringtoDouble(latitude),utils.convertStringtoDouble(longitude), IWeatherLocation.Units.valueOf(units));
    }

    public geoLocation(String name, String country, Double latitude, Double longitude, IWeatherLocation.Units units) {
        this.name = name;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.units = units;
        this.ID = utils.getUniqueId(latitude,longitude);
        this.weather = new OWMWeatherLocation(this);
    }

    public OWMWeatherLocation getWeather() {
        return weather;
    }

    public Boolean isValid(){
        if (latitude==null || longitude==null){
            return false;
        }
        return true;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public IWeatherLocation.Units getUnits() {
        return units;
    }

    public void setUnits(IWeatherLocation.Units units) {
        if(this.weather!=null && this.getWeather().hasWeather()){
            this.getWeather().setLastChecked(0);
            this.getWeather().setUnits(units);
        }
        this.units = units;
    }

    public String getFullName(){
        return this.name + "," + this.country + " lat:" + this.latitude + " lon:" + this.longitude;
    }

    public String getFullNameWithTemp(){
        if (!weather.hasWeather()){
            weather.update();
        }
        String temp = weather.getCurrentWeather().getTempFormatted(units);
        return this.name + "," + this.country + " " + temp + " lat:" + this.latitude + " lon:" + this.longitude;
    }

    @Override
    public String toString() {
        return "geoLocation{" +
                "name=" + name +
                ", country=" + country +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", units=" + units +
                ", id=" + ID;
    }

    @Override
    public int compareTo(geoLocation o) {
        return this.getName().compareTo(o.getName());
    }

    public static Country findByCountryCode(String abbr){
        for(Country v : Country.values()){
            if( v.shortCode.equals(abbr)){
                return v;
            }
        }
        return Country.NONE;
    }

    /**
     * Country that can be set for getting data from OpenWeatherMap.org
     */
    public static enum Country {
                UNITED_STATES("US"),
                CANADA("CA"),
                NONE("XX"),
                AFGHANISTAN("AF"),
                ALAND_ISLANDS("AX"),
                ALBANIA("AL"),
                ALGERIA("DZ"),
                AMERICAN_SAMOA("AS"),
                ANDORRA("AD"),
                ANGOLA("AO"),
                ANGUILLA("AI"),
                ANTARCTICA("AQ"),
                ANTIGUA_AND_BARBUDA("AG"),
                ARGENTINA("AR"),
                ARMENIA("AM"),
                ARUBA("AW"),
                AUSTRALIA("AU"),
                AUSTRIA("AT"),
                AZERBAIJAN("AZ"),
                BAHAMAS("BS"),
                BAHRAIN("BH"),
                BANGLADESH("BD"),
                BARBADOS("BB"),
                BELARUS("BY"),
                BELGIUM("BE"),
                BELIZE("BZ"),
                BENIN("BJ"),
                BERMUDA("BM"),
                BHUTAN("BT"),
                BOLIVIA("BO"),
                BOSNIA_AND_HERZEGOVINA("BA"),
                BOTSWANA("BW"),
                BOUVET_ISLAND("BV"),
                BRAZIL("BR"),
                BRITISH_INDIAN_OCEAN_TERRITORY("IO"),
                BRITISH_VIRGIN_ISLANDS("VG"),
                BRUNEI("BN"),
                BULGARIA("BG"),
                BURKINA_FASO("BF"),
                BURUNDI("BI"),
                CAMBODIA("KH"),
                CAMEROON("CM"),
                CAPE_VERDE("CV"),
                CARIBBEAN_NETHERLANDS("BQ"),
                CAYMAN_ISLANDS("KY"),
                CENTRAL_AFRICAN_REPUBLIC("CF"),
                CHAD("TD"),
                CHILE("CL"),
                CHINA("CN"),
                CHRISTMAS_ISLAND("CX"),
                COCOS_KEELING_ISLANDS("CC"),
                COLOMBIA("CO"),
                COMOROS("KM"),
                CONGO_BRAZZAVILLE("CG"),
                CONGO_KINSHASA("CD"),
                COOK_ISLANDS("CK"),
                COSTA_RICA("CR"),
                CROATIA("HR"),
                CUBA("CU"),
                CURACAO("CW"),
                CYPRUS("CY"),
                CZECH_REPUBLIC("CZ"),
                COTE_D_IVOIRE("CI"),
                DENMARK("DK"),
                DJIBOUTI("DJ"),
                DOMINICA("DM"),
                DOMINICAN_REPUBLIC("DO"),
                ECUADOR("EC"),
                EGYPT("EG"),
                EL_SALVADOR("SV"),
                EQUATORIAL_GUINEA("GQ"),
                ERITREA("ER"),
                ESTONIA("EE"),
                ETHIOPIA("ET"),
                FALKLAND_ISLANDS("FK"),
                FAROE_ISLANDS("FO"),
                FIJI("FJ"),
                FINLAND("FI"),
                FRANCE("FR"),
                FRENCH_GUIANA("GF"),
                FRENCH_POLYNESIA("PF"),
                FRENCH_SOUTHERN_TERRITORIES("TF"),
                GABON("GA"),
                GAMBIA("GM"),
                GEORGIA("GE"),
                GERMANY("DE"),
                GHANA("GH"),
                GIBRALTAR("GI"),
                GREECE("GR"),
                GREENLAND("GL"),
                GRENADA("GD"),
                GUADELOUPE("GP"),
                GUAM("GU"),
                GUATEMALA("GT"),
                GUERNSEY("GG"),
                GUINEA("GN"),
                GUINEA_BISSAU("GW"),
                GUYANA("GY"),
                HAITI("HT"),
                HEARD_AND_MCDONALD_ISLANDS("HM"),
                HONDURAS("HN"),
                HONG_KONG_SAR_CHINA("HK"),
                HUNGARY("HU"),
                ICELAND("IS"),
                INDIA("IN"),
                INDONESIA("ID"),
                IRAN("IR"),
                IRAQ("IQ"),
                IRELAND("IE"),
                ISLE_OF_MAN("IM"),
                ISRAEL("IL"),
                ITALY("IT"),
                JAMAICA("JM"),
                JAPAN("JP"),
                JERSEY("JE"),
                JORDAN("JO"),
                KAZAKHSTAN("KZ"),
                KENYA("KE"),
                KIRIBATI("KI"),
                KUWAIT("KW"),
                KYRGYZSTAN("KG"),
                LAOS("LA"),
                LATVIA("LV"),
                LEBANON("LB"),
                LESOTHO("LS"),
                LIBERIA("LR"),
                LIBYA("LY"),
                LIECHTENSTEIN("LI"),
                LITHUANIA("LT"),
                LUXEMBOURG("LU"),
                MACAU_SAR_CHINA("MO"),
                MACEDONIA("MK"),
                MADAGASCAR("MG"),
                MALAWI("MW"),
                MALAYSIA("MY"),
                MALDIVES("MV"),
                MALI("ML"),
                MALTA("MT"),
                MARSHALL_ISLANDS("MH"),
                MARTINIQUE("MQ"),
                MAURITANIA("MR"),
                MAURITIUS("MU"),
                MAYOTTE("YT"),
                MEXICO("MX"),
                MICRONESIA("FM"),
                MOLDOVA("MD"),
                MONACO("MC"),
                MONGOLIA("MN"),
                MONTENEGRO("ME"),
                MONTSERRAT("MS"),
                MOROCCO("MA"),
                MOZAMBIQUE("MZ"),
                MYANMAR_BURMA("MM"),
                NAMIBIA("NA"),
                NAURU("NR"),
                NEPAL("NP"),
                NETHERLANDS("NL"),
                NEW_CALEDONIA("NC"),
                NEW_ZEALAND("NZ"),
                NICARAGUA("NI"),
                NIGER("NE"),
                NIGERIA("NG"),
                NIUE("NU"),
                NORFOLK_ISLAND("NF"),
                NORTH_KOREA("KP"),
                NORTHERN_MARIANA_ISLANDS("MP"),
                NORWAY("NO"),
                OMAN("OM"),
                PAKISTAN("PK"),
                PALAU("PW"),
                PALESTINIAN_TERRITORIES("PS"),
                PANAMA("PA"),
                PAPUA_NEW_GUINEA("PG"),
                PARAGUAY("PY"),
                PERU("PE"),
                PHILIPPINES("PH"),
                PITCAIRN_ISLANDS("PN"),
                POLAND("PL"),
                PORTUGAL("PT"),
                PUERTO_RICO("PR"),
                QATAR("QA"),
                ROMANIA("RO"),
                RUSSIA("RU"),
                RWANDA("RW"),
                REUNION("RE"),
                SAMOA("WS"),
                SAN_MARINO("SM"),
                SAUDI_ARABIA("SA"),
                SENEGAL("SN"),
                SERBIA("RS"),
                SEYCHELLES("SC"),
                SIERRA_LEONE("SL"),
                SINGAPORE("SG"),
                SINT_MAARTEN("SX"),
                SLOVAKIA("SK"),
                SLOVENIA("SI"),
                SOLOMON_ISLANDS("SB"),
                SOMALIA("SO"),
                SOUTH_AFRICA("ZA"),
                SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS("GS"),
                SOUTH_KOREA("KR"),
                SOUTH_SUDAN("SS"),
                SPAIN("ES"),
                SRI_LANKA("LK"),
                ST_BARTHELEMY("BL"),
                ST_HELENA("SH"),
                ST_KITTS_AND_NEVIS("KN"),
                ST_LUCIA("LC"),
                ST_MARTIN("MF"),
                ST_PIERRE_AND_MIQUELON("PM"),
                ST_VINCENT_AND_GRENADINES("VC"),
                SUDAN("SD"),
                SURINAME("SR"),
                SVALBARD_AND_JAN_MAYEN("SJ"),
                SWAZILAND("SZ"),
                SWEDEN("SE"),
                SWITZERLAND("CH"),
                SYRIA("SY"),
                SAO_TOME_AND_PRINCIPE("ST"),
                TAIWAN("TW"),
                TAJIKISTAN("TJ"),
                TANZANIA("TZ"),
                THAILAND("TH"),
                TIMOR_LESTE("TL"),
                TOGO("TG"),
                TOKELAU("TK"),
                TONGA("TO"),
                TRINIDAD_AND_TOBAGO("TT"),
                TUNISIA("TN"),
                TURKEY("TR"),
                TURKMENISTAN("TM"),
                TURKS_AND_CAICOS_ISLANDS("TC"),
                TUVALU("TV"),
                US_OUTLYING_ISLANDS("UM"),
                US_VIRGIN_ISLANDS("VI"),
                UGANDA("UG"),
                UKRAINE("UA"),
                UNITED_ARAB_EMIRATES("AE"),
                UNITED_KINGDOM("GB"),
                URUGUAY("UY"),
                UZBEKISTAN("UZ"),
                VANUATU("VU"),
                VATICAN_CITY("VA"),
                VENEZUELA("VE"),
                VIETNAM("VN"),
                WALLIS_AND_FUTUNA("WF"),
                WESTERN_SAHARA("EH"),
                YEMEN("YE"),
                ZAMBIA("ZM"),
                ZIMBABWE("ZW")
        ;

        private final String shortCode;

        Country(String code) {
            this.shortCode = code;
        }

        public String getCountryCode() {
            return this.shortCode;
        }

        public String getCountryFullName() {
            return this.name() + " (" + this.shortCode + ")";
        }

    }
}
