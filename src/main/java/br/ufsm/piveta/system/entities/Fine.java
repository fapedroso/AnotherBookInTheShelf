package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Fine {

    private Connection connection;
    private Integer id;
    private User user = null;
    private Integer user_id;
    private Loan loan = null;
    private Integer loan_id;
    private Integer value;

    protected Fine(Integer id, Integer user_id, Integer loan_id, Integer value) {
        this.id = id;
        this.user_id = user_id;
        this.loan_id = loan_id;
        this.value = value;
    }

    public static Fine getFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new Fine(
                    resultSet.getInt(1), // id
                    resultSet.getInt(2), // user_id
                    resultSet.getInt(3), // loan_id
                    resultSet.getInt(4)  // value
            );
        } else return null;
    }

    public static Fine get(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, loan_id, value FROM fines WHERE id = ?");

        preparedStatement.setInt(1,id);

        ResultSet resultSet = preparedStatement.executeQuery();

        Fine fine = getFromResultSet(resultSet);

        if (fine != null) {
            fine.setConnection(connection);
        }

        return fine;
    }

    public static List<Fine> getByUser(Connection connection, int user_id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, loan_id, value FROM fines WHERE user_id = ?");

        preparedStatement.setInt(1,user_id);

        List<Fine> fines = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Fine fine;

        while ((fine = getFromResultSet(resultSet)) != null) {
            fines.add(fine);
        }

        return fines;
    }

    public static List<Fine> getByLoan(Connection connection, int loan_id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, loan_id, value FROM fines WHERE loan_id = ?");

        preparedStatement.setInt(1,loan_id);

        List<Fine> fines = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Fine fine;

        while ((fine = getFromResultSet(resultSet)) != null) {
            fines.add(fine);
        }

        return fines;
    }

    public boolean save() throws SQLException {
        return save(connection);
    }

    public boolean save(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE fines SET user_id = ?, loan_id = ?, value = ? WHERE id = ?");

        preparedStatement.setInt(1,getUserId());
        preparedStatement.setInt(2,getLoanId());
        preparedStatement.setInt(3,getValue());

        return preparedStatement.executeUpdate() == 1;
    }

    public Integer getId() {
        return id;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public User getUser() throws SQLException {
        return getUser(connection);
    }

    public Integer getUserId() {
        return user_id;
    }

    public User getUser(Connection connection) throws SQLException {
        if (user == null) {
            user = User.get(connection,getUserId());
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.user_id = user.getId();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Integer getLoanId() {
        return loan_id;
    }

    public Loan getLoan() throws SQLException {
        return getLoan(connection);
    }

    public Loan getLoan(Connection connection) throws SQLException {
        return null;
//        if (user == null){
//            user = Loan.get(connection,user_id);
//        }
//        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
//        this.loan_id = loan.getId;
    }
}
