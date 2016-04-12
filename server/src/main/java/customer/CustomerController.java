package customer;

import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

    //Get customer details using the email address 
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

    //Get customer cart details
    @RequestMapping(value = "/{email}/cart", method = RequestMethod.GET)
    public String GetCartDetails(@PathVariable String email) {
        Customer customer = readFromDb(email);
        Cart cart  = customer.getCart();
        Gson gson = new Gson();
        return gson.toJson(cart); 
    }

	
    @RequestMapping(method = RequestMethod.POST,
                    headers = {"Content-type=application/json"})
    public String sample_post(@RequestBody String record,
                              HttpServletRequest request) {
        System.out.println("Post request received  : "  + record);
        Gson gson = new Gson();
        Customer customer = gson.fromJson(record, Customer.class);
        customer.setCart();
        writeToDb(customer);
        return gson.toJson(customer);
    }

                    
    @RequestMapping(value = "/{email}/cart",
                    method = RequestMethod.PUT,
                    headers = {"Content-type=application/json"})
    public String sample_put(@RequestBody String record,
                             @PathVariable String email,
                             HttpServletRequest request) {
        System.out.println("Put request received for : "  + email + " record : " + record);
        Customer customer = readFromDb(email);
        Cart cart = customer.getCart();
        Gson gson = new Gson();
        HashMap<String, Integer> deltas = gson.fromJson(record, new TypeToken<HashMap<String, Integer>>(){}.getType());

        // First, iterate through the deltas, and update the cart with the new values
        // Then iterate through the cart and collect all the keys where value is less than 0
        // Then remove those keys form the cart.

        // Example: initial cart = {}
        // PUT request with {"kitkat": 1, "perk": -1}
        // cart = {"kitkat": 1, "perk": -1}
        // then iterate and prune keys with values <= 0
        // cart = {"kitkat": 1}
        for (Map.Entry<String, Integer> entry : deltas.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Key: " + key + " Value: "  + String.valueOf(value));
            if(cart.getItems().containsKey(key)) {
                cart.getItems().put(key, cart.getItems().get(key) + value);
            } else {
                cart.getItems().put(key, value);
            }
        }
        List <String> keysToRemove = new ArrayList<String>();
        for (Map.Entry <String, Integer> entry: cart.getItems().entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value <= 0) {
                keysToRemove.add(key);
            }
        
        }
        for (String key: keysToRemove) {
            cart.getItems().remove(key);
        }
        return gson.toJson(cart);
    }

    public void writeToDb(Customer customer) {
        this.allCustomers.put(customer.getEmail(), customer);
    }
}
