package hello;

import db.*;
import com.google.gson.Gson;
import java.util.*;
import java.util.Date;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.lang.*;

public class Entry{

    private final String key;
    private String value;
    private Date time;  
    private String servicingHost;
    private String version;

    Entry(String key, String value) {
        this.key = key;
        this.value = value;
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

    public void mangleValue() {
        this.value += this.servicingHost;
        this.setVersion();
    }

    public String getServicingHost() {
        return servicingHost;
    }

    public void setServicingHost(String ipAddress) {
        // Despite the name, this function will also set version
        this.servicingHost = ipAddress;
        this.setVersion();
    }

    public void setVersion() {
        System.out.println(this.version);
        if(this.version == null || this.version.equals("")) {
            System.out.println("Setting version!");
            System.out.println("Version before : " + this.version);
            UUID idOne = UUID.randomUUID();
            this.version = String.valueOf(idOne);
            System.out.println("Version after: " + this.version);
        }
    }

    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
