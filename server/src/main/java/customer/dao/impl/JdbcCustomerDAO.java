package customer.dao.impl;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import customer.dao.CustomerDAO;
import customer.model.Customer;

public class JdbcCustomerDAO implements CustomerDAO
{
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void insert(Customer customer){
		
		String sql = "INSERT INTO customer" +
				"(email, details) VALUES ( ?, ?)";
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
		    ps.setString(1, customer.getEmail());
			ps.setString(2, customer.getDetails());

			//ps.setInt(3, customer.getAge());
			ps.executeUpdate();
			ps.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
			
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
	}
	
	
	public void update(Customer customer) {
		
		String updateSql = "UPDATE customer SET details = ? WHERE email = ?";
		Connection conn = null ; 
		try {
			conn = dataSource.getConnection();
                	PreparedStatement ps = conn.prepareStatement(updateSql);
               		ps.setString(1, customer.getDetails());
                	ps.setString(2, customer.getEmail());
			ps.executeUpdate();
			ps.close();	
	        } catch (SQLException e) {
                        throw new RuntimeException(e);

                } finally {
                        if (conn != null) {
                                try {
                                        conn.close();
                                } catch (SQLException e) {}
                        }
                }

	}

	public Customer findByCustomerEmail(String email){
		
		String sql = "SELECT * FROM customer WHERE email = ?";
		
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			Customer customer = null;
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
                String details = rs.getString("details");
                Gson gson = new Gson();
                customer = gson.fromJson(details, Customer.class);
			}
			rs.close();
			ps.close();
			return customer;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {}
			}
		}
	}

}




