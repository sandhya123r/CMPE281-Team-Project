package hello;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.google.gson.Gson;


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="non-existent record")
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String key) {
        System.out.println("Resource not found" + key);
    }
}

@RestController
@RequestMapping("/greeting")
public class DatabaseController {
    private Map<String, Entry> numbers;
    public String myaddress;
    
    public DatabaseController() {
        this.numbers = new HashMap<String, Entry>();

        // fill numbers with some stuff
        Map<String,Entry> d = new HashMap<String,Entry>();
        Entry e1 = new Entry("one", "abcde");
        Entry e2 = new Entry("two", "pqrst");
        Entry e3 = new Entry("three", "klmno");
        Gson gson = new Gson();
        this.numbers.put(e1.getKey(),e1);
        this.numbers.put(e2.getKey(),e2);
        this.numbers.put(e3.getKey(),e3);
    }

    @RequestMapping(value="/{name}", method = RequestMethod.GET)
    public Entry sample_get(@PathVariable String name) {
        if (this.numbers.containsKey(name)) {
            return this.numbers.get(name);
        }
        throw new ResourceNotFoundException(name);
    }
    
    @RequestMapping(method = RequestMethod.POST, headers = {"Content-type=application/json"})
    @ResponseBody
    public Entry sample_post(@RequestBody String record) {
        // store the key and value in k and v and update the hashtable
        Gson gson = new Gson();
        Entry entry = gson.fromJson(record,Entry.class);
        entry.setTime();
        entry.setServicingHost();
        this.numbers.put(entry.getKey(),entry);
        return entry;
    }

    @RequestMapping(method = RequestMethod.PUT, headers = {"Content-type=application/json"})
    @ResponseBody
    public Entry sample_put(@RequestBody String record){
        Gson gson = new Gson();
        Entry entry = gson.fromJson(record,Entry.class);

        //If key is present in hashtable ,update the record else throw exception
        if(this.numbers.containsKey(entry.getKey())) {
            entry.setTime();
            entry.setServicingHost();
            this.numbers.put(entry.getKey(),entry);
            return entry;
        }
            throw new ResourceNotFoundException(entry.getKey());
    }
    
    @RequestMapping(value ="/{name}", method = RequestMethod.DELETE)
     public String sample_delete(@PathVariable String name){
        if(this.numbers.containsKey(name)) {
            this.numbers.remove(name);
            return "SUCCESS";
        } 
        throw new ResourceNotFoundException(name);
    }
    
}
