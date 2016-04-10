package customer;

import java.util.*;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


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

    @RequestMapping(value = "/{email}", method = RequestMethod.GET)
    public String GetDetails(@PathVariable String email) {
        System.out.println("Get on email: " +  email);
        Customer customer = readFromDb(email); 
        Gson gson = new Gson();
        return gson.toJson(customer);
     }

    public Customer readFromDb(String email) {
        if(this.allCustomers.containsKey(email)){
            return this.allCustomers.get(email);
     	} 
        throw new ResourceNotFoundException(email); 	
    }
	
    @RequestMapping(method = RequestMethod.POST,
                    headers = {"Content-type=application/json"})
    public String sample_post(@RequestBody String record,
                                           HttpServletRequest request) {
        System.out.println("Post request received  : "  + record);
        Gson gson = new Gson();
        Customer customer = gson.fromJson(record, Customer.class);
        customer.setCartId();
        writeToDb(customer);
        return gson.toJson(customer).toString();
    }

    public void writeToDb(Customer customer) {
        this.allCustomers.put(customer.getEmail(), customer);
    }
}
