package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Loan {
    private Connection connection;
    private Integer id;
    private Integer book_id;
    private Integer user_id;
    private LocalDateTime withdrawn_in;
    private LocalDateTime returned_at;
    private LocalDate due_to;

    protected Loan(Integer id, Integer book_id, Integer user_id, LocalDateTime withdrawn_in,
                   LocalDateTime returned_at, LocalDate due_to){
        this.id = id;
        this.book_id = book_id;
        this.user_id = user_id;
        this.withdrawn_in = withdrawn_in;
        this.returned_at = returned_at;
        this.due_to = due_to;

    }

    protected static Loan getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new Loan(
                    resultSet.getInt(1),
                    resultSet.getInt(2),
                    resultSet.getInt(3),
                    resultSet.getTimestamp(4).toLocalDateTime(),
                    resultSet.getTimestamp(5).toLocalDateTime(),
                    resultSet.getDate(6).toLocalDate()
            );
        }else return null;
    }

    protected static Loan getFromPreparedStatement(PreparedStatement preparedStatement)throws SQLException{
        ResultSet resultSet = preparedStatement.executeQuery();

        Loan loan = getFromResultSet(resultSet);

        if(loan != null){
            loan.setConnection(preparedStatement.getConnection());
        }

        return loan;
    }

    protected static List<Loan> getListFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        List<Loan> loans = new ArrayList<>();

        Loan loan;

        while ((loan = getFromResultSet(resultSet)) != null){
            loan.setConnection(preparedStatement.getConnection());
            loans.add(loan);
        }

        return loans;
    }

    protected static Loan get(Connection connection, Integer id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, book_id, user_id, withdrawn_in, returned_at, due_to FROM loans WHERE id = ?");

        preparedStatement.setInt(1,id);

        return getFromPreparedStatement(preparedStatement);
    }

    protected static List<Loan> getAll(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, book_id, user_id, withdrawn_in, returned_at, due_to FROM loans");

        return getListFromPreparedStatement(preparedStatement);
    }

    protected static List<Loan> getByBook(Connection connection, int book_id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, book_id, user_id, withdrawn_in, returned_at, due_to FROM loans WHERE book_id = ?");

        preparedStatement.setInt(1, book_id);

        return getListFromPreparedStatement(preparedStatement);
    }

    protected static List<Loan> getByUser(Connection connection, int user_id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, book_id, user_id, withdrawn_in, returned_at, due_to FROM loans WHERE user_id = ?");

        preparedStatement.setInt(1, user_id);

        return getListFromPreparedStatement(preparedStatement);
    }

    protected static List<Loan> getLate(Connection connection, LocalDate returned_at, LocalDate due_to) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, book_id, user_id, withdrawn_in, returned_at, due_to FROM loans " +
                        "WHERE returned_at ISNULL AND due_to < current_date");

        return getListFromPreparedStatement(preparedStatement);
    }

    public boolean save(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE loans SET returned_at = ?, due_to = ? WHERE id = ?");

        preparedStatement.setTimestamp(1,java.sql.Timestamp.valueOf(getReturnedAt()));
        preparedStatement.setDate(2,java.sql.Date.valueOf(getDueTo()));
        preparedStatement.setInt(3,getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public static Loan create(Connection connection, int book_id, int user_id, LocalDateTime withdrawn_in,
                               LocalDateTime returned_at, LocalDate due_to) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO loans (book_id, user_id, withdrawn_in, returned_at, due_to) values (?,?,?,?,?)");

        preparedStatement.setInt(1,book_id);
        preparedStatement.setInt(2,user_id);
        preparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(withdrawn_in));
        preparedStatement.setTimestamp(4, java.sql.Timestamp.valueOf(returned_at));
        preparedStatement.setDate(5, java.sql.Date.valueOf(due_to));

        preparedStatement.execute();

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()) {
            int id = resultSet.getInt(1);
            return new Loan(id, book_id, user_id, withdrawn_in, returned_at, due_to);
        } else return null;
    }

    public Integer getId() {
        return id;
    }

    public Integer getBookId() {
        return book_id;
    }

    public Integer getUserId() {
        return user_id;
    }

    public LocalDateTime getWithdrawnIn() {
        return withdrawn_in;
    }

    public LocalDateTime getReturnedAt() {
        return returned_at;
    }

    public void setReturnedAt(LocalDateTime returned_at) {
        this.returned_at = returned_at;
    }

    public LocalDate getDueTo() {
        return due_to;
    }

    public void setDueTo(LocalDate due_to) {
        this.due_to = due_to;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
