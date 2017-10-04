package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Reservation {

    private Connection connection;
    private Integer id;
    private Integer user_id;
    private User user = null;
    private Integer book_id;
    private Book book = null;
    private LocalDate reserved_for;


    protected Reservation(Integer id, Integer user_id, Integer book_id, LocalDate reserved_for){
        this.id = id;
        this.user_id = user_id;
        this.book_id = book_id;
        this.reserved_for = reserved_for;
    }

    @Override
    public String toString() {
        String user;
        String book;
        String date = getReservedFor().toString();

        try {
            user = getUser().toString();
            book = getBook().toString();
        } catch (SQLException e) {
            user = "some user";
            book = "some book";
            e.printStackTrace();
        }

        return "Reservation of book "+book+" for user "+user+" to day "+date;
    }

    @Nullable
    public static Reservation getFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new Reservation(
                    resultSet.getInt(1), // id
                    resultSet.getInt(2), // user_id
                    resultSet.getInt(3), // book_id
                    resultSet.getDate(4).toLocalDate() // date
            );
        } else return null;
    }

    protected static List<Reservation> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<Reservation> reservations = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Reservation reservation;

        while ((reservation = getFromResultSet(resultSet)) != null) {
            reservation.setConnection(preparedStatement.getConnection());
            reservations.add(reservation);
        }

        return reservations;
    }

    protected static Reservation getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        Reservation reservation = getFromResultSet(resultSet);

        if (reservation != null) {
            reservation.setConnection(preparedStatement.getConnection());
        }

        return reservation;
    }

    public static Reservation get(Connection connection, int id) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE id = ?");
        preparedStatement.setInt(1,id);

        return getFromPreparedStatement(preparedStatement);
    }

    public static List<Reservation> getByUser(Connection connection, int user_id) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE user_id = ?");
        preparedStatement.setInt(1,user_id);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Reservation> getByBook(Connection connection, int book_id) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE book_id = ?");
        preparedStatement.setInt(1,book_id);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Reservation> getByBeforeDate(Connection connection, LocalDate date) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE reserved_for < ?");
        preparedStatement.setDate(1,java.sql.Date.valueOf(date));

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Reservation> getByAfterDate(Connection connection, LocalDate date) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE reserved_for > ?");
        preparedStatement.setDate(1,java.sql.Date.valueOf(date));

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Reservation> getByAfterDateByUser(Connection connection, LocalDate date, User user) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE reserved_for > ? AND user_id = ?");

        preparedStatement.setDate(1,java.sql.Date.valueOf(date));
        preparedStatement.setInt(2,user.getId());

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Reservation> getByDate(Connection connection, LocalDate date) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE reserved_for = ?");
        preparedStatement.setDate(1,java.sql.Date.valueOf(date));

        return getListFromPreparedStatement(preparedStatement);
    }

    @Nullable
    public static Reservation create(Connection connection, Integer user_id, Integer book_id, LocalDate date)
        throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO reservations (user_id, book_id, reserved_for) values (?,?,?)");

        preparedStatement.setInt(1,user_id);
        preparedStatement.setInt(2,book_id);
        preparedStatement.setDate(3,java.sql.Date.valueOf(date));

        if (!preparedStatement.execute()) return null;

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()) {
            int id = resultSet.getInt(1);
            return new Reservation(id, user_id, book_id, date);
        } else return null;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM reservations WHERE id = ?");

        preparedStatement.setInt(1, getId());

        return preparedStatement.execute();
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return user_id;
    }

    public User getUser() throws SQLException {
        if (user == null){
            user = User.get(getConnection(),getUserId());
        }
        return user;
    }

    public Integer getBookId() {
        return book_id;
    }

    public Book getBook() throws SQLException {
        if (book == null){
            book = Book.get(getConnection(),getBookId());
        }
        return book;
    }

    public LocalDate getReservedFor() {
        return reserved_for;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
