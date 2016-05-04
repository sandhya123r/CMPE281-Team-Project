package customer;


import customer.model.Customer;
import customer.dao.CustomerDAO;

import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


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

    private ApplicationContext context;
    private CustomerDAO customerDAO;

    public CustomerController(){
        this.allCustomers = new HashMap <String,Customer>();
        Customer u1 = new Customer(
            "sandhya@gmail", "Sandhya R", "sandhya address",
            "sandhya phone", "sandya cc");
        Customer u2 = new Customer(
            "tom@gmail", "Tom Tom", "tom address",
            "tom phone", "tom cc");
        Customer u3 = new Customer(
            "cmpe281@gmail","cmpe281","San Jose State University,San Jose",
            "4086127689","4322-0022-2422-2242");
        
        this.allCustomers.put(u1.getEmail(), u1);
        this.allCustomers.put(u2.getEmail(), u2);
        this.allCustomers.put(u3.getEmail(),u3);
        System.out.println(this.allCustomers);

        context = new ClassPathXmlApplicationContext("Spring-Module.xml");
        customerDAO = (CustomerDAO) context.getBean("customerDAO");
        customerDAO.insert(u1);
        customerDAO.insert(u2);
        customerDAO.insert(u3);
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
        // if(this.allCustomers.containsKey(email)) { 
        //    return this.allCustomers.get(email); 
        // }
        try {
            Customer customer = this.customerDAO.findByCustomerEmail(email);
            return customer;
        } catch(RuntimeException r) {
            throw new ResourceNotFoundException(email);
        }
    }

    //Get customer cart details
    @RequestMapping(value = "/{email}/cart", method = RequestMethod.GET)
    public String GetCartDetails(@PathVariable String email) {
        Customer customer = readFromDb(email);
        Cart cart  = customer.getCart();
        Gson gson = new Gson();
        System.out.println("GET on Cart returning " + gson.toJson(cart));
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
        Cart c = gson.fromJson(record, Cart.class);
        System.out.println(gson.toJson(c));
        System.out.println(c.getCart_to_display());
        System.out.println(c.getItems());
        cart.setItems(c.getItems());
        cart.setCart_to_display(c.getCart_to_display());
        customer.setCart(cart);
        updateDb(customer);
        System.out.println("Cart is now " + gson.toJson(cart));
        return gson.toJson(cart);
    }


    @RequestMapping(value = "/{email}/cart1",
                    method = RequestMethod.PUT,
                    headers = {"Content-type=application/json"})
    public String sample_put1(@RequestBody String record,
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
                System.out.println("Incrementing value ");
                cart.getItems().put(key, cart.getItems().get(key) + value);
            } else {
                System.out.println("Inserting for the first time ");
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
	
	cart.setItems(cart.getItems());
	customer.setCart(cart);
	System.out.println("new cart is : " + cart.getItems() );
    updateDb(customer);
	return gson.toJson(cart);
    }

    public void updateDb(Customer customer) {
    	this.customerDAO.update(customer);	
    }
    public void writeToDb(Customer customer) {
        this.customerDAO.insert(customer);
        // this.allCustomers.put(customer.getEmail(), customer);
    }
}

