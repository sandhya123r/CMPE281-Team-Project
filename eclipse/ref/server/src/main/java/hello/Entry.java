package hello;

import com.google.gson.Gson;
import java.util.*;
import java.util.Date;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Entry{

    private final String key;
    private final String value;
    private Date time;  
    private String servicingHost;

    Entry(String key, String value) {
        this.key = key;
        this.value = value;
        this.setServicingHost();
        this.setTime();
    }

    public String getKey() {
        return key;
    }
   
    public Date getTime() {
        return time;
    }
    public void setTime(){
        this.time = new Date();
    }

    public String getValue() {
        return value;
    }

    public String getServicingHost() {
        return servicingHost;
    }
    public void setServicingHost() {
        try {
            InetAddress my_ip = InetAddress.getLocalHost();
            this.servicingHost = my_ip.getHostAddress() ;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
