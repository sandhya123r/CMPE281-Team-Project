package product;

import java.util.*;

public class Product{

//    private String id;
    private String name;
    private double price;

    // Add photo
	
    public Product(String name) {
        this.name = name;
        // For now, everything costs 10 bucks
        this.price = 10.00;
//        this.id = String.valueOf(UUID.randomUUID());
    }

//    public String getId() {
//        return id;
//    } 

    public String getName() {
    	return name; 
    }
}
