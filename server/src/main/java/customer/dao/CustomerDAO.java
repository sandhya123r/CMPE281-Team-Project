package customer.dao;

import customer.model.Customer;

public interface CustomerDAO 
{
	public void insert(Customer customer);
	public Customer findByCustomerEmail(String email);
	public void update(Customer customer);
}




