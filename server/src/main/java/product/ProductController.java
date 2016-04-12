package product;

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
        if(this.allProducts.containsKey(name)){
            return new Gson().toJson(this.allProducts.get(name));
     	} 
        throw new ResourceNotFoundException(name); 	
     }
	
}
