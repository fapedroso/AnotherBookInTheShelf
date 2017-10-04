package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Publisher {

    private Connection connection;
    private Integer id;
    private String name;

    Publisher(Integer id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Nullable
    protected static Publisher getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new Publisher(
                    resultSet.getInt(1),   // id
                    resultSet.getString(2) // name
            );
        }else return null;
    }

    protected static List<Publisher> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<Publisher> publishers = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Publisher publisher;

        while ((publisher = getFromResultSet(resultSet)) != null) {
            publisher.setConnection(preparedStatement.getConnection());
            publishers.add(publisher);
        }

        return publishers;
    }

    protected static Publisher getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        Publisher publisher = getFromResultSet(resultSet);

        if (publisher != null) {
            publisher.setConnection(preparedStatement.getConnection());
        }

        return publisher;
    }

    public static Publisher get(Connection connection,int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name FROM publishers WHERE id = ?");

        preparedStatement.setInt(1, id);

        return getFromPreparedStatement(preparedStatement);
    }

    public static List<Publisher> getByName(Connection connection, String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name FROM publishers WHERE name ILIKE concat('%',?,'%')");

        preparedStatement.setString(1, name);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Publisher> getAll(Connection connection,String name) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, name FROM publishers");

        return getListFromPreparedStatement(preparedStatement);
    }

    @Nullable
    public static Publisher create(Connection connection, String name)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO publishers (name) values (?)");

        preparedStatement.setString(1,name);

        if (!preparedStatement.execute()) return null;

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()){
            int id = resultSet.getInt(1);
            return new Publisher(id, name);
        } else return null;
    }

    public boolean save() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "UPDATE publishers SET name = ? WHERE id = ?");

        preparedStatement.setString(1, getName());

        preparedStatement.setInt(2, getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM publishers WHERE id = ?");

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
