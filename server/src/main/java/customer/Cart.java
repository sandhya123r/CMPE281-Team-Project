package customer;

import java.util.*;

public class Cart {

    private String cartId;
    private Map <String, Integer> items;
	

    public Cart() {
        this.setCartId();
        this.setItems();
    }

    public Map<String, Integer> getItems() {
    	return items; 
    }

    public String getCartId() {
        return cartId;
    }
    
    public void setItems(){
        this.items = new HashMap <String, Integer>();	
    }

    public void setCartId() {
        this.cartId = String.valueOf(UUID.randomUUID());
    }

}
