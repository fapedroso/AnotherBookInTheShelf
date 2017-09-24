package br.ufsm.piveta.system;

import br.ufsm.piveta.system.entities.Book;
import br.ufsm.piveta.system.entities.LiteraryWork;
import br.ufsm.piveta.system.entities.User;
import br.ufsm.piveta.system.forms.Login;
import org.omg.CORBA.TIMEOUT;

import javax.swing.*;
import java.io.Console;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class System {

    public final static int ENVIRONMENT_CLI = 0;
    @SuppressWarnings("unused")
    public final static int ENVIRONMENT_GUI = 1;

    private final int PREPARED_STATEMENT_LOGIN = 0;

    private final Map<Integer,PreparedStatement> preparedStatements = new HashMap<>();

    private final Scanner scanner;
    private final Console console;
    private Connection connection = null;
    private int environment = ENVIRONMENT_CLI;
    private User loggedUser = null;
    private boolean exiting;

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
                if (environment == ENVIRONMENT_CLI) {
                    showOptions();
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

    protected int getValidOption(int maxValid) {
        java.lang.System.out.printf("\nSelect [0-%d]: ",maxValid);

        int choice;

        while ((choice = scanner.nextInt()) > maxValid || choice < 0){
            java.lang.System.out.printf("Invalid choice, please enter a number between 0 and %d",maxValid);
        }

        return choice;
    }

    protected void clearConsole(){
        java.lang.System.out.print("\033[H\033[2J");
        java.lang.System.out.flush();
    }

    protected void showOptions(){

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!exiting) {
            clearConsole();
            if (loggedUser.getIsLibrarian()){
                showLibrarianMainOptions();
            } else {
                showNonLibrarianMainOptions();
            }
        }
    }

    protected void showLibrarianMainOptions(){
        java.lang.System.out.println("Choose an action:\n");
        java.lang.System.out.println("[1] Manage Books");
        java.lang.System.out.println("[0] Exit");

        switch (getValidOption(1)){
            case 0:
                exiting = true;
                return;
            case 1:
                break;
        }
    }

    protected void showNonLibrarianMainOptions(){
        java.lang.System.out.println("Choose an action:\n");
        java.lang.System.out.println("[1] New Reservations");
        java.lang.System.out.println("[2] List Reservations");
        java.lang.System.out.println("[0] Exit");

        switch (getValidOption(2)){
            case 0:
                exiting = true;
                return;
            case 1:
                newRegistration();
                break;
            case 2:
                break;
        }
    }

    protected void newRegistration(){

        clearConsole();
        java.lang.System.out.print("Enter a book title: ");

        String bookTitle;

        do {
            bookTitle = scanner.nextLine();
        } while (bookTitle.isEmpty());

        try {
            List<LiteraryWork> literaryWorks = LiteraryWork.getByTitle(connection,bookTitle);

            if (literaryWorks.isEmpty()) {
                java.lang.System.out.println("Could not find any book. Please try again or leave blank to return.");
            } else {
                java.lang.System.out.println("Those are the books I've found:\n");
            }

            for (int i = 1; i <= literaryWorks.size(); i++) {
                java.lang.System.out.printf("[%d] %s\n", i, literaryWorks.get(i-1).getTitle());
            }
            java.lang.System.out.println("[0] Cancel\n");

            java.lang.System.out.printf("Choose a book to reserve [0-%d]: ",literaryWorks.size());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
