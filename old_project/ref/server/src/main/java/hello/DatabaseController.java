package hello;

import db.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.properties.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.lang.*;
import java.security.MessageDigest;

import org.springframework.http.client.SimpleClientHttpRequestFactory;



@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="non-existent record")
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String key) {}
}

@RestController
@RequestMapping("/greeting")
public class DatabaseController {

    private ArrayList<String> hostIps;
    private String myIp;
    private Map<String,SM.OID> OIDMap;
    SMImplVersion2 s_smInstance ;

    private Map<String, ArrayList<String> > index;
    // version map
    // name -> vmap
    // vmap: version -> entry
    private Map <String, Map<String, Entry> > versionMap;
    RestTemplate restTemplate;

    private ConfigurationProjectProperties cp;

    public DatabaseController() {
        this.s_smInstance = new SMImplVersion2();
        this.OIDMap = new HashMap<String,SM.OID>();        
        this.versionMap = new HashMap<String, Map<String, Entry> >();
        this.index = new HashMap<String, ArrayList<String> >();
        this.restTemplate = new RestTemplate();
        ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(5);
    }

    @Autowired
    void setConfigurationProjectProperties(ConfigurationProjectProperties cp) {
        this.cp = cp;
        this.setHostIps();
        this.setMyIp();
        System.out.println("**********************************************");
        System.out.println("We have been called!!!");
        Gson gson = new Gson();
        System.out.println("configs:  " + gson.toJson(this.cp));
        System.out.println("**********************************************");
    }
    
    public void setMyIp() {
        this.myIp = this.cp.getMyIp();
    }

    public void setHostIps() {
        hostIps = this.cp.getServers();
    }

    public ArrayList<String> getHostIps() {
        return this.hostIps;
    }

    public SM.OID SetOID(String record) {   
        try {
            SM.Record recordToStore = new SM.Record(record.length());
            recordToStore.setBytes(record.getBytes());
            SM.OID rec_oid = (SM.OID)this.s_smInstance.store(recordToStore);
            return rec_oid;
        } catch( Exception e) {
            throw new ResourceNotFoundException(record); 
        }
    }

    
    
    public void CreateOIDMap(Entry entry)
    {
        SM.OID new_oid;
        new_oid = SetOID(entry.toString());
        this.OIDMap.put(entry.getKey(),new_oid);
    }
   
/*    public Entry getEntryFromVersion(String Version)
    {
        String myEntry = this.versionMap.get(Version);
        Gson gson = new Gson();
        Entry entry = gson.fromJson(myEntry,Entry.class);
        return entry;
    }
      
*/
    
    public String getHelper(String key) {  
        SM.OID oid = this.OIDMap.get(key); 
        try {
            SM.Record recordToFetch = this.s_smInstance.fetch(oid);
            byte[] byterecord = recordToFetch.getBytes(0,0);
            String myrecord = new String(byterecord,"UTF-8");
            Gson gson = new Gson();
 	        Entry entry = gson.fromJson(myrecord,Entry.class);
            return entry.toString();
        } catch(Exception e) {
            return e.getMessage();
        }
    }
   
    public void updateHelper(String key,String record)
    {
        try {
            SM.OID updateOID = this.OIDMap.get(key); 
            SM.Record recordToUpdate = new SM.Record(record.length()) ;
            recordToUpdate.setBytes(record.getBytes());
            this.s_smInstance.update(updateOID,recordToUpdate);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
   
    public void deleteHelper(String key)
    {
       SM.OID oid_delete = this.OIDMap.get(key);
            try {
                this.s_smInstance.delete(oid_delete);    
            }
           catch(Exception e) {
                System.out.println(e.getMessage());
            } 
       this.OIDMap.remove(key);
    } 
    
    public String resolveConflict(String name, String myVersion, ArrayList<String> otherVersions) {
        ArrayList<Entry> allUniqueVersions = new ArrayList<Entry>();
        Set<String> knownValues = new HashSet<String>();
       
        // First put myversion in the unique version list 
        Gson gson = new Gson();
		Entry entry;
        if (!"".equals(myVersion)) {
            entry = gson.fromJson(myVersion,Entry.class);
            knownValues.add(entry.getValue());
            allUniqueVersions.add(entry);
        }

        for(String otherVersion: otherVersions) {
            gson = new Gson();
            entry = gson.fromJson(otherVersion, Entry.class);
            if (!knownValues.contains(entry.getValue())) {
                knownValues.add(entry.getValue());
                allUniqueVersions.add(entry);
            }
        }

        // Create an entry in version map
        Map <String, Entry> vMap =  new HashMap<String, Entry>();
        this.versionMap.put(name, vMap);
        
        for(Entry e: allUniqueVersions) {
            e.setServicingHost(this.myIp);
            vMap.put(e.getVersion(), e);
        }

        System.out.println(allUniqueVersions);
        System.out.println(versionMap);
        return gson.toJson(allUniqueVersions).toString();
    }

    public String resolveConflictOld(String name,String myVersion,ArrayList<String>otherVersions)
    {	
		if(myVersion == ""){
			return resolveConflictOld(name,otherVersions.get(0),otherVersions);
		}
		String finalVersion = myVersion;
		Gson gson = new Gson();
		Entry entry = gson.fromJson(myVersion,Entry.class);
		String myKey = entry.getKey();
		String myValue = entry.getValue();
		Date latestTime = entry.getTime();
		for(String otherVersion : otherVersions)
		{
			gson = new Gson();
			Entry remoteEntry = gson.fromJson(otherVersion,Entry.class);
			if((remoteEntry.getKey()!=name) || ((remoteEntry.getKey().equals(name)) && (remoteEntry.getValue().equals(myValue))))
				continue;
			if(remoteEntry.getTime().compareTo(latestTime) >0) {
				finalVersion = otherVersion;
				latestTime = remoteEntry.getTime();
			}	
		}
	
        entry = gson.fromJson(finalVersion, Entry.class);
        entry.setServicingHost(this.myIp);

	if(entry.getKey().equals(name)){
		return entry.toString();
	}
	throw new ResourceNotFoundException(name);
    }
    
    @RequestMapping(value = {"/search/", "/search"}, method = RequestMethod.GET)
    public String search(@RequestParam Map<String,String> allRequestParams) {
        Gson gson = new Gson();
        System.out.println(allRequestParams);
        String search_param = allRequestParams.get("key");

        // Fetch all names in index
        if(!this.index.containsKey(search_param)) {
            return gson.toJson("");
        }
        ArrayList <String> names = this.index.get(search_param);
        return gson.toJson(names).toString();

    }

    @RequestMapping(value="/{name}", method = RequestMethod.GET)
    public String sample_get(@PathVariable String name, HttpServletRequest request) {

        // If the request is from a peer, just return the data and exit
        if (this.hostIps.contains(request.getRemoteAddr())){
            System.out.println("Servicing request from remote GET ");	 
            if(this.OIDMap.containsKey(name)){
                System.out.println("My machine data is :" + getHelper(name));
                return getHelper(name);
            }	
            else
                return "";	
        }

        // If request is from user, collect from everyone
        String myGetData = "";
        ArrayList<String> remoteGetData = this.getRemoteData(name);
        if(this.OIDMap.containsKey(name)){
            myGetData = getHelper(name);	
        }

        // If none of my peers had this record, in the absence of partitions, it 
        // probably means replication is yet to reach. JUst return what we have,
        // else raise 404
        
        if(remoteGetData.size() == 0) {
            if(! "".equals(myGetData)){
                return myGetData;
            }
            throw new ResourceNotFoundException(name);
        }
        /* If you dont have the data but peer nodes have the data populate your OID Map 
        if(remoteGetData.size()!=0 && this.OIDMap.size()==0)
        */	

        // Try to resolve conflicts, if any in myGetData and remoteGetData
        return resolveConflict(name,myGetData,remoteGetData);
    }
    
    @RequestMapping(method = RequestMethod.POST, headers = {"Content-type=application/json"})
    @ResponseBody
    public String sample_post(@RequestBody String record, HttpServletRequest request) {
        System.out.println("Post request received by : " + this.myIp + " from : "  + request.getRemoteAddr());
        Gson gson = new Gson();
        Entry entry = gson.fromJson(record,Entry.class);
        if(this.OIDMap.containsKey(entry.getKey()))
            throw new ResourceNotFoundException(entry.getKey());

        if(this.hostIps.contains(request.getRemoteAddr())) {
            entry.setServicingHost(request.getRemoteAddr());
        } else {
            // Call replication logic
            this.replicate(record, "post", "");
            entry.setServicingHost(this.myIp);

            // For inducing conflict
            //entry.mangleValue();
        }
        entry.setTime();
        CreateOIDMap(entry);
        UpdateIndex(entry);
        return entry.toString();
    }

    public void UpdateIndex(Entry entry) {
        String key = entry.getKey();
        String value = entry.getValue();
        String[] splited = value.split("\\s+");
        System.out.println(splited);
        for(String word: splited) {
            // check if there is an entry in index for this word.
            ArrayList<String> names;
            if(!this.index.containsKey(word)) {
                names = new ArrayList<String>();
                this.index.put(word, names);
            } else {
                names = this.index.get(word);
            }
            if(!Arrays.asList(names).contains(key)) {
                names.add(key);
            }
        }
        System.out.println("Index is  " + this.index);
    }
    @RequestMapping(value="/resolve/{name}/{version}", method = RequestMethod.PUT, headers = {"Content-type=application/json"})
    @ResponseBody
    public String special_put(@PathVariable String name, @PathVariable String version, HttpServletRequest request){
        System.out.println("name: " + name + " version: " + version);
        //If key is not present database ,throw exception
        if(!this.OIDMap.containsKey(name)) 
            throw new ResourceNotFoundException(name);

        if(this.versionMap.containsKey(name) && versionMap.get(name).containsKey(version)) {
            Entry entry = versionMap.get(name).get(version);

            System.out.println("got entry: " + versionMap.get(name));
            System.out.println("got entry: " + entry.toString());

            this.replicate(entry.toString(), "put", name);
            entry.setServicingHost(this.myIp);
            entry.setTime();
            updateHelper(entry.getKey(),entry.toString());
            this.versionMap.remove(name);
            return entry.toString();
        } else {
            // If key is not present in versionMap, call remote get
            ArrayList<String> remoteGetData = this.getRemoteData(name);
            System.out.println(remoteGetData);
            for(String otherVersion: remoteGetData) {
                Gson gson = new Gson();
                Entry entry = gson.fromJson(otherVersion, Entry.class);
                if(version.equals(entry.getVersion())) {
                    this.replicate(entry.toString(), "put", name);
                    entry.setServicingHost(this.myIp);
                    entry.setTime();
                    updateHelper(entry.getKey(),entry.toString());
                    this.versionMap.remove(name);
                    return entry.toString();
                }
            }
        }
        throw new ResourceNotFoundException(name);
    }
/*
    @RequestMapping(value="/resolve/{name}/{version}", method = RequestMethod.PUT, headers = {"Content-type=application/json"})
    @ResponseBody
    public String another_special_put(@PathVariable String name, @PathVariable String version, HttpServletRequest request){
        System.out.println("name: " + name + " version: " + version);

        // First get from yourself.

        // Then from everyone else

        // Iterate through, and replicate if version matches

        // 

    }
*/
    @RequestMapping(value="/{name}", method = RequestMethod.PUT, headers = {"Content-type=application/json"})
    @ResponseBody
    public String sample_put(@PathVariable String name, @RequestBody String record, HttpServletRequest request){
        //If key is present in hashtable ,update the record else throw exception
        if(!this.OIDMap.containsKey(name)) 
            throw new ResourceNotFoundException(name);

        Gson gson = new Gson();
        Entry entry = gson.fromJson(record,Entry.class);


        if(this.hostIps.contains(request.getRemoteAddr())) {
            entry.setServicingHost(request.getRemoteAddr());
        } else {
            // Call replication logic
            this.replicate(entry.toString(), "put", name);
            entry.setServicingHost(this.myIp);
        }
        entry.setTime();
        updateHelper(entry.getKey(),entry.toString());
        return entry.toString();
    }
    
    @RequestMapping(value ="/{name}", method = RequestMethod.DELETE)
    public String sample_delete(@PathVariable String name, HttpServletRequest request) {
        if(!this.OIDMap.containsKey(name))
            throw new ResourceNotFoundException(name);

        deleteHelper(name);
        if(this.hostIps.contains(request.getRemoteAddr())) {
            return "SUCCESS";
        } else {
            // Call replication logic
            this.replicate("", "delete", name);
            return "SUCCESS";
        }
    }

    public ArrayList<String> getRemoteData(String name) {
        String uri = "";
        ArrayList<String> remoteData = new ArrayList<String>();
        System.out.println("before calling remote hosts");
        System.out.println(remoteData);
        System.out.println(this.myIp);
        System.out.println(this.hostIps);
        for (String ipAddr: this.hostIps) {
            System.out.println("Now processing ip: " + ipAddr);
            System.out.println(ipAddr.equals(this.myIp));
            if(!ipAddr.equals(this.myIp)) {
                uri = "http://ec2-"+ ipAddr.replace(".", "-") + ".us-west-2.compute.amazonaws.com:8080/greeting/" + name;
                try {
                    String result = this.restTemplate.getForObject(uri, String.class);
                    System.out.println("Host : " + ipAddr + " result: " + result);
                    if(! "".equals(result) && result != null){
                        remoteData.add(result);
                    }
                } catch(Exception e) {
                    System.out.println("Remote GET for name: " + name + " on host: " + ipAddr + " failed. Got " + e.getMessage());
                }
            }
        }
        System.out.println("Collected remoteData: " + remoteData);
        return remoteData;
    }

    public void replicate(String record, String mode, String name) {
        String uri = "";
        for (String ipAddr: this.hostIps) {
            if(!ipAddr.equals(this.myIp)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<String>(record, headers);
                try {
                    if("post".equals(mode)) {
                        uri = "http://ec2-"+ ipAddr.replace(".", "-") + ".us-west-2.compute.amazonaws.com:8080/greeting";
                        String result = this.restTemplate.postForObject(uri, entity, String.class);
                    } else if("put".equals(mode)) {
                        uri = "http://ec2-"+ ipAddr.replace(".", "-") + ".us-west-2.compute.amazonaws.com:8080/greeting/" + name;
                        this.restTemplate.put(uri, entity);
                    } else if("delete".equals(mode)) {
                        uri = "http://ec2-"+ ipAddr.replace(".", "-") + ".us-west-2.compute.amazonaws.com:8080/greeting/" + name;
                        this.restTemplate.delete(uri);
                    }
                } catch(Exception e) {
                    System.out.println("Replication for mode: " + mode + " failed on " + ipAddr + ". Error was " + e.getMessage());
                }
            }
        }
    }
} 
