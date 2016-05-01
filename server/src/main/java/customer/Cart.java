package customer;

import java.util.*;

public class Cart {

    private String cartId;
    private Map <String, Integer> items;

    private List<Map<String, String>> cart_to_display;

    public Cart() {
        this.setCartId();
        this.setItems();
        this.setCart_to_display();
    }

    public Map<String, Integer> getItems() {
    	return items; 
    }

    public String getCartId() {
        return cartId;
    }

    public List<Map<String, String>> getCart_to_display() {
        return cart_to_display;
    }

    public void setItems(){
        this.items = new HashMap <String, Integer>();	
    }
    public void setCart_to_display(){
        this.cart_to_display = new ArrayList <Map<String, String>>();	
    }

    public void setCart_to_display(List<Map<String, String>> newCart_to_display){
        this.cart_to_display = newCart_to_display;
    }

    public void setItems(Map<String,Integer> items) {
    	this.items = items;
    }

    public void setCartId() {
        this.cartId = String.valueOf(UUID.randomUUID());
    }

}
