package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Fine {

    private Connection connection;
    private Integer id;
    private User user = null;
    private Integer user_id;
    private Loan loan = null;
    private Integer loan_id;
    private Integer value;
    private Boolean paid;

    protected Fine(Integer id, Integer user_id, Integer loan_id, Integer value, Boolean paid) {
        this.id = id;
        this.user_id = user_id;
        this.loan_id = loan_id;
        this.value = value;
        this.paid = paid;
    }

    public static Fine getFromResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return new Fine(
                    resultSet.getInt(1),    // id
                    resultSet.getInt(2),    // user_id
                    resultSet.getInt(3),    // loan_id
                    resultSet.getInt(4),    // value
                    resultSet.getBoolean(5) // paid
            );
        } else return null;
    }

    public static Fine get(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, loan_id, value, paid FROM fines WHERE id = ?");

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
                "SELECT id, user_id, loan_id, value, paid FROM fines WHERE user_id = ?");

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
                "SELECT id, user_id, loan_id, value, paid FROM fines WHERE loan_id = ?");

        preparedStatement.setInt(1,loan_id);

        List<Fine> fines = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Fine fine;

        while ((fine = getFromResultSet(resultSet)) != null) {
            fines.add(fine);
        }

        return fines;
    }

    public static Fine create(Connection connection, Integer user_id, Integer loan_id, Integer value, Boolean paid)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO fines (user_id, loan_id, value, paid) values (?,?,?,?)");

        preparedStatement.setInt(1,user_id);
        preparedStatement.setInt(2,loan_id);
        preparedStatement.setInt(3,value);
        preparedStatement.setBoolean(4,paid);

        preparedStatement.execute();

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()) {
            int id = resultSet.getInt(1);
            return new Fine(id, user_id, loan_id, value, paid);
        } else return null;
    }

    public boolean save() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "UPDATE fines SET user_id = ?, loan_id = ?, value = ?, paid = ? WHERE id = ?");

        preparedStatement.setInt(1,getUserId());
        preparedStatement.setInt(2,getLoanId());
        preparedStatement.setInt(3,getValue());
        preparedStatement.setBoolean(4,getPaid());

        preparedStatement.setInt(5,getId());

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

    public Integer getUserId() {
        return user_id;
    }

    public User getUser() throws SQLException {
        if (user == null) {
            user = User.get(getConnection(),getUserId());
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.user_id = user.getId();
    }

    public Integer getLoanId() {
        return loan_id;
    }

    public Loan getLoan() throws SQLException {
        return null;
//        if (user == null){
//            user = Loan.get(getConnection(),getLoanId());
//        }
//        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
//        this.loan_id = loan.getId;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
