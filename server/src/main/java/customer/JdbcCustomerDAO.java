package customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;


public class JdbcCustomerDAO {
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        System.out.println("setting datasource to: " + dataSource);
        this.dataSource = dataSource;
    }
    
    public void insert(Customer customer){
        System.out.println("hello,insertining into db");
        String sql = "INSERT INTO CUSTOMER " +
                "(DETAILS) VALUES (?)";
        Connection conn = null;
        
        try {
            System.out.println("Conn: " + conn);
            System.out.println("datasource: " + dataSource);
            conn = dataSource.getConnection();
            System.out.println("Conn: " + conn);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customer.getDetails());
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
   /* 
    public Customer findByCustomerId(int custId){
        
        String sql = "SELECT * FROM CUSTOMER WHERE CUST_ID = ?";
        
        Connection conn = null;
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, custId);
            Customer customer = null;
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customer = new Customer(
                    rs.getInt("CUST_ID"),
                    rs.getString("NAME"), 
                    rs.getInt("Age")
                );
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
 */
}

