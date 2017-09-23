package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Book {
    private Connection connection;
    private Integer id;
    private Integer literary_work_id;
    private String codigo;
    private String condition;

    protected Book(Integer id,Integer literary_work_id,String codigo,String condition){
        this.id = id;
        this.literary_work_id = literary_work_id;
        this.codigo = codigo;
        this.condition = condition;

    }

    protected static Book getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new Book(
                    resultSet.getInt(1), // id
                    resultSet.getInt(2), // literary_work_id
                    resultSet.getString(3), // codigo
                    resultSet.getString(4) // condition
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

    public boolean save(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE books SET cod = ?, condition = ? WHERE id = ?");

        preparedStatement.setString(1,getCodigo());
        preparedStatement.setString(2,getCondition());

        return preparedStatement.executeUpdate() == 1;
    }

    public Integer getId() {
        return id;
    }

    public Integer getLiterary_work_id() {
        return literary_work_id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
