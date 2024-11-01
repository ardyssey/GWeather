package sageweather;

/**
 * Created by jusjoken on 8/28/2021.
 */
public class Log {
    private static String DefaultLevel = "INFO";

    public static void info(String message){
        info(null,message);
    }
    public static void info(String caller, String message){
        logMessage("INFO", caller, message);
    }

    private static void logMessage(String type, String caller, String message){
        String currentType = DefaultLevel;
        if(type.equals(currentType) || type.equals("ERROR") || type.equals("INFO") || type.equals("FATAL") || type.equals("WARN")){
            if(caller == null){
                System.out.println("GWEATHER: " + type + ": " + message);
            }else{
                System.out.println("GWEATHER: " + type + ": " + caller + "; " + message);
            }
        }
    }

}
