package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class User {

    private Connection connection;
    private Integer id;
    private String name;
    private String username;
    private Boolean isTeacher;
    private Boolean isLibrarian;
    private String address;
    private String phone;
    private String postalCode;

    protected User(Integer id, String name, String username, Boolean isTeacher, Boolean isLibrarian,
         String address, String phone, String postalCode) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.isTeacher = isTeacher;
        this.isLibrarian = isLibrarian;
        this.address = address;
        this.phone = phone;
        this.postalCode = postalCode;
    }

    protected static User getFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new User(
                    resultSet.getInt(1),     // id
                    resultSet.getString(2),  // name
                    resultSet.getString(3),  // username
                    resultSet.getBoolean(4), // is_teacher
                    resultSet.getBoolean(5), // is_librarian
                    resultSet.getString(6),  // address
                    resultSet.getString(7),  // phone
                    resultSet.getString(8)   // postal_code
            );
        } else return null;
    }

    public static User get(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name, username, is_teacher, is_librarian, address, phone, postal_code " +
                        "FROM users WHERE id = ?");

        preparedStatement.setInt(1,id);

        ResultSet resultSet = preparedStatement.executeQuery();

        User user = getFromResultSet(resultSet);

        if (user != null) {
            user.setConnection(connection);
        }

        return user;
    }

    public static User get(Connection connection, String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name, username, is_teacher, is_librarian, address, phone, postal_code " +
                        "FROM users WHERE username = ?");

        preparedStatement.setString(1,username);

        ResultSet resultSet = preparedStatement.executeQuery();

        User user = getFromResultSet(resultSet);

        if (user != null) {
            user.setConnection(connection);
        }

        return user;
    }

    public boolean setPasswordAndSave(Connection connection, String password) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE users SET password = md5(?) WHERE id = ?"
        );

        preparedStatement.setInt(1,getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public boolean save() throws SQLException {
        return save(connection);
    }

    public boolean save(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET name = ?, " +
                "is_teacher = ?, is_librarian = ?, address = ?, phone = ?, postal_code = ? WHERE id = ?");

        preparedStatement.setString(1,getName());
        preparedStatement.setBoolean(2, getIsTeacher());
        preparedStatement.setBoolean(3,getIsLibrarian());
        preparedStatement.setString(4,getAddress());
        preparedStatement.setString(5,getPhone());
        preparedStatement.setString(6,getPostalCode());
        preparedStatement.setInt(7,getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public List<Fine> getFines() throws SQLException {
        return getFines(connection);
    }

    public List<Fine> getFines(Connection connection) throws SQLException {
        return Fine.getByUser(connection,getId());
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getIsTeacher() {
        return isTeacher;
    }

    public void setIsTeacher(Boolean teacher) {
        isTeacher = teacher;
    }

    public Boolean getIsLibrarian() {
        return isLibrarian;
    }

    public void setIsLibrarian(Boolean librarian) {
        isLibrarian = librarian;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
