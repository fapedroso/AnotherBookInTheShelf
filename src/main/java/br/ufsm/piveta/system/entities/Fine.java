package br.ufsm.piveta.system.entities;

import java.sql.Connection;
import java.sql.SQLException;

public class Fine {

    private Connection connection;
    private Integer id;
    private User user = null;
    private Integer user_id;
    private Loan loan = null;
    private Integer loan_id;
    private Integer value;

    public Fine(Integer id, Integer user_id, Integer loan_id, Integer value) {
        this.id = id;
        this.user_id = user_id;
        this.loan_id = loan_id;
        this.value = value;
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
        if (user == null){
            if (connection != null)
            user = User.get(connection,user_id);
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

    public Loan getLoan() throws SQLException {
        return null;
//        if (user == null){
//            if (connection != null)
//                user = Loan.get(connection,user_id);
//        }
//        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
//        this.loan_id = loan.getId;
    }
}
