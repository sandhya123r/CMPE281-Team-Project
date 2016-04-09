package hello;

public class User {

    private String  email;
    private String address ;
    private String name;
	
    public User(String email,String name) {
        this.email = email;
        this.name = name;
    }

    public String getemailId() {
        return this.email;
    } 

    public String getName() {
    	return name; 
    }
    
    public void setemailId(String email){
	  this.email = email;	
    }  

    public void setAddress(String address){
  	this.address = address ;	
    }

    public String getAddress() {
        return address;
    }
	
}
