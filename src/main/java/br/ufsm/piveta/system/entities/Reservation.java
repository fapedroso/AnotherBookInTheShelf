package br.ufsm.piveta.system.entities;

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
    private LocalDate reservedFor;


    protected Reservation(Integer id, Integer user_id, Integer book_id, LocalDate reservedFor){
        this.id = id;
        this.user_id = user_id;
        this.book_id = book_id;
        this.reservedFor = reservedFor;
    }

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

    public static List<Reservation> getByDate(Connection connection, LocalDate date) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, book_id, reserved_for FROM reservations WHERE reserved_for = ?");
        preparedStatement.setDate(1,java.sql.Date.valueOf(date));

        return getListFromPreparedStatement(preparedStatement);
    }
    public boolean save() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "UPDATE reservations SET user_id = ?, book_id = ?, reserved_for = ? WHERE id = ?");

        preparedStatement.setInt(1,getUserId());
        preparedStatement.setInt(2,getBookId());
        preparedStatement.setDate(3,java.sql.Date.valueOf(getReservedFor()));

        preparedStatement.setInt(4,getId());

        return preparedStatement.executeUpdate() == 1;
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

    public void setUser(User user){
        this.user = user;
        this.user_id = user.getId();
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

    public void setBook(Book book) {
        this.book = book;
        this.book_id = book.getId();
    }

    public LocalDate getReservedFor() {
        return reservedFor;
    }

    public void setReservedFor(LocalDate reservedFor) {
        this.reservedFor = reservedFor;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
