package br.ufsm.piveta.system;

import br.ufsm.piveta.system.entities.User;
import br.ufsm.piveta.system.forms.Login;

import javax.swing.*;
import java.io.Console;
import java.sql.*;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class System {

    public final static int ENVIRONMENT_CLI = 0;
    @SuppressWarnings("unused")
    public final static int ENVIRONMENT_GUI = 1;

    protected final int PREPARED_STATEMENT_LOGIN = 0;

    protected final Map<Integer,PreparedStatement> preparedStatements = new HashMap<>();

    protected final Scanner scanner;
    protected final Console console;
    protected Connection connection = null;
    protected int environment = ENVIRONMENT_CLI;
    protected User loggedUser = null;

    System(int environment) throws SQLException {
        this.environment = environment;
        scanner = new Scanner(java.lang.System.in);
        console = java.lang.System.console();
        connection = DriverManager.getConnection("jdbc:postgresql://emilio.pedrollo.nom.br/postgres",
                "teste", "piveta");
    }

    protected void getCredentials(Login.OnLoginFormIsDone onLoginFormIsDone) {
        Login.Credentials credentials;
        String username;
        String password;

        if (environment == ENVIRONMENT_CLI) {
            java.lang.System.out.print("Username: ");
            username = scanner.nextLine();

            if (console != null) {
                password = new String(console.readPassword("Password: "));
            }else{
                java.lang.System.out.print("Password: ");
                password = scanner.nextLine();
            }
            credentials = new Login.Credentials(username,password);
            onLoginFormIsDone.callback(null, credentials);
        } else {
            Login login = new Login(onLoginFormIsDone);
            login.setVisible(true);
        }
    }

    protected PreparedStatement getPreparedStatement(int which) {
        try {
            PreparedStatement preparedStatement = preparedStatements.get(which);

            if (preparedStatement == null){
                switch (which) {
                    case PREPARED_STATEMENT_LOGIN:
                        preparedStatements.put(PREPARED_STATEMENT_LOGIN,connection.prepareStatement(
                            "SELECT id FROM users WHERE username = ? AND password = md5(?)"
                        ));
                }
            }
            return preparedStatements.get(which);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean login(Login.Credentials credentials) {

        try {
            PreparedStatement preparedStatement = getPreparedStatement(PREPARED_STATEMENT_LOGIN);

            preparedStatement.setString(1, credentials.getUsername());
            preparedStatement.setString(2, credentials.getPassword());

            ResultSet resultSet = preparedStatement.executeQuery();

            boolean success = resultSet.next();

            if (success) loggedUser = User.get(connection,resultSet.getInt(1));

            resultSet.close();

            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }


    protected void showMessage(String message){
        if (environment == ENVIRONMENT_CLI) {
            java.lang.System.out.println(message);
        } else {
            JOptionPane.showMessageDialog(null,message);
        }
    }

    protected interface onLogin {
        void success();
        void error(int remainingTries);
        void canceled();
    }

    protected void tryToLogin(onLogin onLogin) {
        tryToLogin(0, 3,onLogin);
    }

    protected void tryToLogin(int tries, int maxTries, onLogin onLogin){

        getCredentials(new Login.OnLoginFormIsDone() {
            @Override
            public void callback(Login loginFrame, Login.Credentials credentials) {
                if (loginFrame != null){
                    loginFrame.setVisible(false);
                    loginFrame.dispose();
                }

                if (login(credentials)) {
                    onLogin.success();
                } else {
                    onLogin.error(maxTries - (tries+1));
                    if ((tries+1) < maxTries) tryToLogin(tries+1,maxTries, onLogin);
                }
            }

            @Override
            public void cancel(Login loginFrame) {
                if (loginFrame != null){
                    loginFrame.setVisible(false);
                    loginFrame.dispose();
                }
                onLogin.canceled();

            }
        });
    }


    public void start() {

        tryToLogin(new onLogin() {
            @Override
            public void success() {
                showMessage("Welcome " + loggedUser.getName());

                if (loggedUser.getIsLibrarian()){
                    showMessage("You are a Librarian");
                }
                if (loggedUser.getIsTeacher()){
                    showMessage("You are a Teacher");
                }
            }

            @Override
            public void error(int remainingTries) {
                if (remainingTries > 0)
                    showMessage("Bad credentials. Try again");
                else
                    showMessage("Too many attempts.");
            }

            @Override
            public void canceled() {
                java.lang.System.exit(0);
            }
        });

    }
}
