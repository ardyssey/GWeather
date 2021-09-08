package sageweather;

import java.util.*;

/**
 * Created by jusjoken on 7/15/2021.
 */
public class WeatherLocations {

    //create a map of the geoLocations using the locationID as the key
    Map<String, geoLocation> mapLocations = new HashMap<>();
    private String defaultLocation = "";

    public WeatherLocations() {
    }

    public Map<String, geoLocation> getMapLocations() {
        return mapLocations;
    }

    public void setMapLocations(Map<String, geoLocation> mapLocations) {
        this.mapLocations = mapLocations;
    }

    //perform an update on a specific location
    public boolean update(String id){
        if (id==null){
            return update();
        }else{
            return mapLocations.get(id).getWeather().update();
        }
    }

    //perform an update on all locations
    public boolean update(){
        boolean anyTrue = false;
        for (geoLocation item:sortedList() ) {
            if (item.getWeather().update()){
                anyTrue = true;
            }
        }
        return anyTrue;
    }

    public long getLastChecked() {
        long minChecked = System.currentTimeMillis();
        for (geoLocation item:sortedList() ) {
            minChecked = Math.min(minChecked,item.getWeather().getLastChecked());
        }
        return minChecked;
    }



    public void add(geoLocation location){
        mapLocations.put(location.getID(),location);
        //ensure there is always a default
        if (defaultLocation.isEmpty()){
            defaultLocation = location.getID();
        }
    }

    public void add(geoLocation location, boolean isDefault){
        if (isDefault){
            defaultLocation = location.getID();
        }
        add(location);
    }

    public boolean remove(String id){
        if (mapLocations.containsKey(id)){
            mapLocations.remove(id);
            if (id.equals(getDefaultID())){
                if (mapLocations.size()>0){
                    //select the first entry and set it as the default
                    setDefaultID(sortedList().get(0).getID());
                }else{
                    defaultLocation = "";
                }
            }
            return true;
        }else{
            return false;
        }
    }

    public boolean hasLocation(String id){
            return mapLocations.containsKey(id);
    }

    public boolean hasLocations(){
        return !mapLocations.isEmpty();
    }

    public geoLocation get(String id){
        return mapLocations.get(id);
    }

    public OWMWeatherLocation getWeather(String id){
        return mapLocations.get(id).getWeather();
    }

    public geoLocation getDefault(){
        return mapLocations.get(defaultLocation);
    }

    public String getDefaultID(){
        return defaultLocation;
    }
    public void setDefaultID(String id){
        defaultLocation = id;
    }

    public void clear(){
        mapLocations.clear();
        defaultLocation = "";
    }

    public List<geoLocation> sortedList() {
        List<geoLocation> tempList = new ArrayList<>(mapLocations.values());
        Collections.sort(tempList);
        return tempList;
    }
}
