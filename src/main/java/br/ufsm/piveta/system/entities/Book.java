package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Book {
    private Connection connection;
    private Integer id;
    private Integer literary_work_id;
    private LiteraryWork literary_work;
    private String code;
    private String condition;

    protected Book(Integer id, Integer literary_work_id, String code, String condition){
        this.id = id;
        this.literary_work_id = literary_work_id;
        this.code = code;
        this.condition = condition;

    }

    @Nullable
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

    protected static List<Book> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<Book> books = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Book book;

        while ((book = getFromResultSet(resultSet)) != null) {
            book.setConnection(preparedStatement.getConnection());
            books.add(book);
        }

        return books;
    }

    protected static Book getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        Book book = getFromResultSet(resultSet);

        if (book != null) {
            book.setConnection(preparedStatement.getConnection());
        }

        return book;
    }

    protected static Book get(Connection connection,int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, literary_work_id, cod, condition FROM books WHERE id = ?");

        preparedStatement.setInt(1, id);

        return getFromPreparedStatement(preparedStatement);
    }

    protected static Book get(Connection connection,String title) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "JOIN literary_works ON books.literary_work_id = literary_works.id " +
                        "WHERE title ILIKE CONCAT('%',?,'%')");

        preparedStatement.setString(1, title);

        return getFromPreparedStatement(preparedStatement);
    }

    @Nullable
    public static Book create(Connection connection, Integer literaryWorkId, String code, String condition)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO books (literary_work_id, cod, condition) values (?,?,?)");

        preparedStatement.setInt(1,literaryWorkId);
        preparedStatement.setString(2,code);
        preparedStatement.setString(3,condition);

        if (!preparedStatement.execute()) return null;

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

    public LiteraryWork getLiteraryWork(){
        if (this.literary_work == null){
//            literary_work = LiteraryWork.get(getConnection(),getLiteraryWorkId());
        }
        return literary_work;
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
