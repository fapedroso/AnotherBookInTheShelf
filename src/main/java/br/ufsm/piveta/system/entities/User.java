package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private String newPassword;
    private boolean passwordHasChanged = false;
    private boolean needUpdate = false;

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

    @Override
    public String toString() {
        return getName();
    }

    @Nullable
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

    protected static List<User> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<User> users = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        User user;

        while ((user = getFromResultSet(resultSet)) != null) {
            user.setConnection(preparedStatement.getConnection());
            users.add(user);
        }

        return users;
    }

    protected static User getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        User user = getFromResultSet(resultSet);

        if (user != null) {
            user.setConnection(preparedStatement.getConnection());
        }

        return user;
    }

    public static User get(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name, username, is_teacher, is_librarian, address, phone, postal_code " +
                        "FROM users WHERE id = ?");

        preparedStatement.setInt(1,id);

        return getFromPreparedStatement(preparedStatement);
    }

    public static User get(Connection connection, String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name, username, is_teacher, is_librarian, address, phone, postal_code " +
                        "FROM users WHERE username = ?");

        preparedStatement.setString(1,username);

        return getFromPreparedStatement(preparedStatement);
    }

    public static List<User> getAll(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name, username, is_teacher, is_librarian, address, phone, postal_code FROM users");

        return getListFromPreparedStatement(preparedStatement);
    }

    @Nullable
    public static User create(Connection connection, String name, String username, Boolean isTeacher, Boolean isLibrarian,
                              String address, String phone, String postalCode, String password) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO users (name,username,is_teacher,is_librarian,address,phone,postal_code,password) "+
                        "values (?,?,?,?,?,?,?,md5(?))");

        preparedStatement.setString(1,name);
        preparedStatement.setString(2,username);
        preparedStatement.setBoolean(3, isTeacher);
        preparedStatement.setBoolean(4, isLibrarian);
        preparedStatement.setString(5, address);
        preparedStatement.setString(6, phone);
        preparedStatement.setString(7, postalCode);
        preparedStatement.setString(8, password);

        if (!preparedStatement.execute()) return null;

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()){
            int id = resultSet.getInt(1);
            return new User(id,name,username,isTeacher,isLibrarian,address,phone,postalCode);
        } else return null;
    }

    public void setPassword(String password) {
        passwordHasChanged = true;
        needUpdate = true;
        newPassword = password;
    }

    public boolean save() throws SQLException {

        boolean success;

        if (passwordHasChanged){
            PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE users SET name = ?, " +
                    "is_teacher = ?, is_librarian = ?, address = ?, phone = ?, postal_code = ?, password = md5(?) "+
                    "WHERE id = ?");

            preparedStatement.setString(1,getName());
            preparedStatement.setBoolean(2, getIsTeacher());
            preparedStatement.setBoolean(3,getIsLibrarian());
            preparedStatement.setString(4,getAddress());
            preparedStatement.setString(5,getPhone());
            preparedStatement.setString(6,getPostalCode());
            preparedStatement.setString(7,newPassword);

            preparedStatement.setInt(8,getId());

            success = (preparedStatement.executeUpdate() == 1);

        } else if (needUpdate) {
            PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE users SET name = ?, " +
                    "is_teacher = ?, is_librarian = ?, address = ?, phone = ?, postal_code = ? WHERE id = ?");

            preparedStatement.setString(1,getName());
            preparedStatement.setBoolean(2, getIsTeacher());
            preparedStatement.setBoolean(3,getIsLibrarian());
            preparedStatement.setString(4,getAddress());
            preparedStatement.setString(5,getPhone());
            preparedStatement.setString(6,getPostalCode());

            preparedStatement.setInt(7,getId());

            success = (preparedStatement.executeUpdate() == 1);
        } else success = true;

        if (success) {
            needUpdate = false;
            passwordHasChanged = false;
        }

        return success;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM users WHERE id = ?");

        preparedStatement.setInt(1, getId());

        return preparedStatement.execute();
    }

    public List<Fine> getFines() throws SQLException {
        return Fine.getByUser(getConnection(),getId());
    }

    public List<Fine> getNonPaidFines() throws SQLException {
        return Fine.getNonPaidByUser(getConnection(),getId());
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!this.name.equals(name)) needUpdate = true;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getIsTeacher() {
        return isTeacher;
    }

    public void setIsTeacher(Boolean teacher) {
        if (!this.isTeacher.equals(teacher)) needUpdate = true;
        isTeacher = teacher;
    }

    public Boolean getIsLibrarian() {
        return isLibrarian;
    }

    public void setIsLibrarian(Boolean librarian) {
        if (!this.isLibrarian.equals(librarian)) needUpdate = true;
        isLibrarian = librarian;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (!this.address.equals(address)) needUpdate = true;
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (!this.phone.equals(phone)) needUpdate = true;
        this.phone = phone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        if (!this.postalCode.equals(postalCode)) needUpdate = true;
        this.postalCode = postalCode;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public int getLoanPeriod() {
        return (isTeacher)? 15: 7;
    }
}
