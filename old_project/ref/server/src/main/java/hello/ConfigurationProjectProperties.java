package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@ConfigurationProperties("configuration")
class ConfigurationProjectProperties {

    private String projectName;
    private String someOtherKey;
    private String myIp;
    private ArrayList<String> servers;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSomeOtherKey() {
        return someOtherKey;
    }

    public void setSomeOtherKey(String someOtherKey) {
        this.someOtherKey = someOtherKey;
    }

    public String getMyIp() {
        return myIp;
    }

    public void setMyIp(String myIp) {
        this.myIp= myIp;
    }

    public ArrayList<String> getServers() {
        return servers;
    }

    public void setServers(ArrayList<String> servers) {
        this.servers = servers;
    }

}
