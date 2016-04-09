package customer;

import java.util.UUID;

public class Customer {

    private String email;
    private String name;
    private String address ;
    private String phone;
    private String creditCard;
    // later, link this to a cart object
    private String cartId;
	

    public Customer(String email, String name, String address,
                    String phone, String creditCard) {
        this.email = email;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.creditCard = creditCard;
    }

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
    public String getCartId() {
        return cartId;
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

    public void setCardId() {
        // generate and set cart id
        this.cartId = String.valueOf(UUID.randomUUID());
    }
}
