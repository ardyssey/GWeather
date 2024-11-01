package sageweather;

import java.util.Enumeration;
import java.util.Map;

/**
 * Created by jusjoken on 7/31/2021.
 */
public class WeatherProperties extends java.util.Properties {
    private String propFileSuffix = "";

    public WeatherProperties() {
        this("");
    }
    public WeatherProperties(String propFileSuffix) {
        //one time load of properties from file
        this.propFileSuffix = propFileSuffix;
        load();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String tValue = super.getProperty(key, defaultValue);
        if (tValue==null || tValue.equals(Const.OptionNotFound)){
            return defaultValue;
        }else{
            return tValue;
        }
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return sortedKeys();
    }

    public java.util.Enumeration sortedKeys()
    {
        java.util.Enumeration keysEnum = super.keys();
        java.util.Vector keyList = new java.util.Vector();

        while(keysEnum.hasMoreElements())
        {
            keyList.add((String)keysEnum.nextElement());
        }
        java.util.Collections.sort(keyList);
        return keyList.elements();
    }

    public Map getPropertiesWithPrefix(String prefix)
    {
        java.util.Enumeration walker = super.propertyNames();
        java.util.Map rv = new java.util.HashMap();
        while (walker.hasMoreElements())
        {
            Object elem = walker.nextElement();
            if (elem != null && elem.toString().startsWith(prefix))
            {
                rv.put(elem.toString().substring(prefix.length()), super.getProperty(elem.toString()));
            }
        }
        return rv;
    }

    public void load()
    {

        java.io.File locationFile = new java.io.File(utils.GetWeatherLocationsFullPath(this.propFileSuffix));
        java.io.InputStream in = null;
        try
        {
            in = new java.io.BufferedInputStream(new java.io.FileInputStream(locationFile));
            super.load(in);
        }
        catch (Exception e)
        {
            Log.info("WeatherProperties","WeatherProperties: load - Error reading weather properties data of:" + e);
        }
        finally
        {
            if (in != null)
            {
                try{in.close();}catch(Exception e){}
                in = null;
            }
        }
    }

    public void save(){
        java.io.File locationFile = new java.io.File(utils.GetWeatherLocationsFullPath(this.propFileSuffix));
        java.io.OutputStream out = null;

        try
        {
            out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(locationFile));
            Log.info("WeatherProperties","WeatherProperties: save - out file '" + locationFile + "'");
            super.store(out, "Google Weather Plugin version " + utils.getVersion() + ": SageTV Weather Data provided by OWM");
        }
        catch (Exception e)
        {
            Log.info("WeatherProperties","WeatherProperties: save - Error writing weather properties data of:" + e);
        }
        finally
        {
            if (out != null)
            {
                try{out.close();}catch(Exception e){}
                out = null;
            }
        }
    }

}


