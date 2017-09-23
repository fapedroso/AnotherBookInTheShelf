package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Book {
    private Connection connection;
    private Integer id;
    private Integer literary_work_id;
    private String code;
    private String condition;

    protected Book(Integer id, Integer literary_work_id, String code, String condition){
        this.id = id;
        this.literary_work_id = literary_work_id;
        this.code = code;
        this.condition = condition;

    }

    protected static Book getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new Book(
                    resultSet.getInt(1),    // id
                    resultSet.getInt(2),    // literary_work_id
                    resultSet.getString(3), // code
                    resultSet.getString(4)  // condition
            );
        }else return null;
    }

    protected static Book get(Connection connection,int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, literary_work_id, cod, condition FROM books WHERE id = ?");

        preparedStatement.setInt(1, id);

        ResultSet resultSet = preparedStatement.executeQuery();

        Book book = getFromResultSet(resultSet);

        if(book != null){
            book.setConnection(connection);
        }

        return book;
    }

    protected  static Book get(Connection connection,String title) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "JOIN literary_works ON books.literary_work_id = literary_works.id " +
                        "WHERE title ILIKE CONCAT('%',?,'%')");

        preparedStatement.setString(1, title);

        ResultSet resultSet = preparedStatement.executeQuery();

        Book book = getFromResultSet(resultSet);

        if (book != null) {
            book.setConnection(connection);
        }

        return book;
    }

    public static Book create(Connection connection, Integer literaryWorkId, String code, String condition)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO books (literary_work_id, cod, condition) values (?,?,?)");

        preparedStatement.setInt(1,literaryWorkId);
        preparedStatement.setString(2,code);
        preparedStatement.setString(3,condition);

        preparedStatement.execute();

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()){
            int id = resultSet.getInt(1);
            return new Book(id, literaryWorkId, code, condition);
        } else return null;
    }

    public boolean save() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "UPDATE books SET cod = ?, condition = ? WHERE id = ?");

        preparedStatement.setString(1, getCode());
        preparedStatement.setString(2, getCondition());

        preparedStatement.setInt(3, getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM books WHERE id = ?");

        preparedStatement.setInt(1, getId());

        return preparedStatement.execute();
    }

    public Integer getId() {
        return id;
    }

    public Integer getLiteraryWorkId() {
        return literary_work_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection){
        this.connection = connection;
    }
}
