package hello;

import java.util.*;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="non-existent record")
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String key) {
        System.out.println("Resource not found" + key);
    }
}

@RestController
@RequestMapping("/users")
public class UserController {

	
    private  Map <String,User> allUsers; 
     
    public UserController(){
    	this.allUsers = new HashMap <String,User>(); 
	User u1 = new User("sandhya@gmail.com","Sandhya");
	User u2 = new User("tom@gmail.com","Tom");
	this.allUsers.put(u1.getemailId(),u1);
	this.allUsers.put(u2.getemailId(),u2);
    } 
    @RequestMapping("/{email}")
    public String GetDetails(@PathVariable String email) {
        if(this.allUsers.containsKey(email)){
                 return this.allUsers.get(email).getName();
     	} 
	throw new ResourceNotFoundException(email); 	
     }
	
}
