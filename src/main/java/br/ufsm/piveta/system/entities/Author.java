package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Author {

    private Connection connection;
    private Integer id;
    private String name;

    Author(Integer id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Nullable
    protected static Author getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new Author(
                    resultSet.getInt(1),   // id
                    resultSet.getString(2) // name
            );
        }else return null;
    }

    protected static List<Author> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<Author> authors = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Author author;

        while ((author = getFromResultSet(resultSet)) != null) {
            author.setConnection(preparedStatement.getConnection());
            authors.add(author);
        }

        return authors;
    }

    protected static Author getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        Author author = getFromResultSet(resultSet);

        if (author != null) {
            author.setConnection(preparedStatement.getConnection());
        }

        return author;
    }

    public static Author get(Connection connection,int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name FROM authors WHERE id = ?");

        preparedStatement.setInt(1, id);

        return getFromPreparedStatement(preparedStatement);
    }

    public static List<Author> getByName(Connection connection, String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name FROM authors WHERE name ILIKE concat('%',?,'%')");

        preparedStatement.setString(1, name);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Author> getAll(Connection connection,String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name FROM authors");

        return getListFromPreparedStatement(preparedStatement);
    }

    @Nullable
    public static Author create(Connection connection, String name)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO authors (name) values (?)");

        preparedStatement.setString(1,name);

        if (!preparedStatement.execute()) return null;

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()){
            int id = resultSet.getInt(1);
            return new Author(id, name);
        } else return null;
    }

    public boolean save() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "UPDATE authors SET name = ? WHERE id = ?");

        preparedStatement.setString(1, getName());

        preparedStatement.setInt(2, getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM authors WHERE id = ?");

        preparedStatement.setInt(1, getId());

        return preparedStatement.execute();
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

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
