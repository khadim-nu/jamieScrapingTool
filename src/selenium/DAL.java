package selenium;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author raath
 */
public class DAL {

    Connection connection;

    public DAL() {

        String username = "root";
        String password = "raath@aws";
        String databaseName = "jamieeb";

//        String url = "jdbc:mysql://localhost:3306/" + databaseName + "?useUnicode=true&characterEncoding=UTF-8";
        String url = "jdbc:mysql://52.11.26.183:3306/" + databaseName + "?useUnicode=true&characterEncoding=UTF-8";
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("connected successfully");;
        } catch (Exception e) {
            System.out.println("DB connection error");
            System.exit(0);
        }

    }

    public String[] getConfigs() {
        String[] params = new String[5];
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("SELECT * FROM configs WHERE id=1 order by id");

            while (rs.next()) {
                params[0] = "" + rs.getInt("price");
                params[1] = "" + rs.getInt("section");
                params[2] = "" + rs.getInt("category");
                params[3] = "" + rs.getInt("wanted");
                params[4] = rs.getString("label");
            }
        } catch (Exception e) {
            System.out.println("Get error " + e.getMessage());
        }
        return params;
    }

    public String[] getSearchParams() {
        String[] params = new String[3];
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("SELECT * FROM scrapeParams WHERE id=1 order by id");

            while (rs.next()) {
                params[0] = rs.getString("string");
                params[1] = rs.getString("label");
                params[2] = rs.getString("greaterThan");
                params[3] = rs.getString("lessThan");
            }
        } catch (Exception e) {
            System.out.println("Get error " + e.getMessage());
        }
        return params;
    }

    public ResultSet getItems(String label) {
        ResultSet rs = null;
        try {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM items WHERE status = 1 AND label like '%" + label + "%'  order by id ");
        } catch (Exception e) {
            System.out.println("Get error " + e.getMessage());
        }
        return rs;
    }

    private void save(String url) {

    }

    private void delete(String param) {
        try {
            String queryCheck = "DELETE FROM items WHERE tag = ?";
            PreparedStatement st = connection.prepareStatement(queryCheck);
            st.setString(1, param);
            int rs = st.executeUpdate();

        } catch (Exception e) {
        }
    }

    void saveItem(String itemId, String title, String pricetxt, String itemUrl, String images, String desc, String techDesc, String category, String label) {
        try {
            Statement stmtInsert = connection.createStatement();
            stmtInsert.execute("set names 'utf8'");
            String sql = "INSERT INTO items "
                    + "(p_id,title,price,link,image_url,description,specification,category,label)"
                    + "VALUES(?,?,?,? ,?,?,?,?,?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            // Set the values
            pstmt.setString(1, itemId);
            pstmt.setString(2, title);
            pstmt.setString(3, pricetxt);
            pstmt.setString(4, itemUrl);
            pstmt.setString(5, images);
            pstmt.setString(6, desc);
            pstmt.setString(7, techDesc);
            pstmt.setString(8, category);
            pstmt.setString(9, label);
            // Insert 
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("DB Error.");
        }
    }

    void postedSuccess(int aInt) {
        try {
            String queryCheck = "UPDATE items set status=0 WHERE id = ?";
            PreparedStatement st = connection.prepareStatement(queryCheck);
            st.setInt(1, aInt);
            int rs = st.executeUpdate();

        } catch (Exception e) {
        }
    }
}
