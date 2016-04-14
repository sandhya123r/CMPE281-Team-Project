package customer;

import java.util.UUID;
import com.google.gson.Gson;

public class Customer {

    private String email;
    private String name;
    private String address ;
    private String phone;
    private String creditCard;
    // later, link this to a cart object
    private Cart cart;
	

    public Customer(String email, String name, String address,
                    String phone, String creditCard) {
        this.email = email;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.creditCard = creditCard;
        this.setCart();
    }
/*
    public Customer() {
        Cart cart = new Cart();
        this.setCartId(cart.getCartId());
    }
*/
    public String getEmail() {
        return email;
    } 
    public String getName() {
    	return name; 
    }
    public String getAddress() {
    	return address; 
    }
    public String getPhone() {
    	return phone; 
    }
    public String getCreditCard() {
    	return creditCard; 
    }
    public Cart getCart() {
        return cart;
    }

    public String getDetails() {
        Gson gson = new Gson();
        return  gson.toJson(this);
    }
    public void setEmail(String email){
        this.email = email;	
    }  
    public void setName(String name){
        this.name = name;	
    }
    public void setAddress(String address){
        this.address = address ;	
    }
    public void setPhone(String phone){
        this.phone = phone;	
    }
    public void setCreditCard(String creditCard){
        this.creditCard = creditCard;	
    }

    public void setCart() {
        this.cart = new Cart();
    }
}
