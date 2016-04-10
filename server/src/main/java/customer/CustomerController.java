package customer;

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
@RequestMapping("/customers")
public class CustomerController {

	
    private  Map <String,Customer> allCustomers; 
     
    public CustomerController(){
        this.allCustomers = new HashMap <String,Customer>(); 
        Customer u1 = new Customer(
            "sandhya@gmail", "Sandhya R", "sandhya address",
            "sandhya phone", "sandya cc");
        Customer u2 = new Customer(
            "tom@gmail", "Tom Tom", "tom address",
            "tom phone", "tom cc");
        this.allCustomers.put(u1.getEmail(), u1);
        this.allCustomers.put(u2.getEmail(), u2);
        System.out.println(this.allCustomers);
    } 

    @RequestMapping("/{email}")
    public String GetDetails(@PathVariable String email) {
        Gson gson = new Gson();
        System.out.println("Got email: " +  email);
        if(this.allCustomers.containsKey(email)){
            return gson.toJson(this.allCustomers.get(email)).toString();
     	} 
        throw new ResourceNotFoundException(email); 	
     }
	
}
