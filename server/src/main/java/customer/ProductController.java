package customer;

import java.util.*;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;



@RestController
@RequestMapping("/products")
public class ProductController {

	
    private  Map <String, Product> allProducts; 
     
    public ProductController(){
        this.allProducts = new HashMap <String, Product>(); 
        Product p1 = new Product("KitKat");
        Product p2 = new Product("Perk");
        this.allProducts.put(p1.getName(), p1);
        this.allProducts.put(p2.getName(), p2);
    } 

    @RequestMapping("/{name}")
    public String GetDetails(@PathVariable String name) {
        System.out.println("calling for "+name);
        Product product = readFromProductDb(name);
        Gson gson = new Gson();
        return gson.toJson(product); 
     }

    public Product readFromProductDb(String name) {
        if(this.allProducts.containsKey(name)) {
               return this.allProducts.get(name);
        }    
        throw new ResourceNotFoundException(name);
    }

    
	
}
