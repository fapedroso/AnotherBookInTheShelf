package br.ufsm.piveta.system;

import br.ufsm.piveta.system.entities.LiteraryWork;
import br.ufsm.piveta.system.entities.User;

import java.io.Console;
import java.sql.*;
import java.lang.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class Main {
    private final Scanner scanner;
    private final Console console;
    private final Connection connection;

    private User loggedUser = null;
    private boolean userExiting;

    public static void main(String[] args) {
        try {
            Main main = new Main();
            main.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    Main() throws SQLException {
        scanner = new Scanner(java.lang.System.in);
        console = java.lang.System.console();

        final String DB_CONNECTION_STRING = "jdbc:postgresql://emilio.pedrollo.nom.br/postgres";
        final String DB_PASSWORD = "piveta";
        final String DB_USER = "teste";

        connection = DriverManager.getConnection(DB_CONNECTION_STRING, DB_USER, DB_PASSWORD);
    }


    public void start() {

        int remainingTries = 3;

        do {
            if (!login(getCredentials())) {
                if (--remainingTries > 0){
                    java.lang.System.out.println("Bad credentials. Try again");
                } else {
                    java.lang.System.out.println("Too many attempts.");
                }
            }
        } while (loggedUser == null && remainingTries > 0);

        if (loggedUser != null) {
            greetUser();
            showOptions();
        }
    }

    private void greetUser() {
        System.out.printf("Welcome %s",loggedUser.getName());
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected boolean login(Credentials credentials) {

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id FROM users WHERE username = ? AND password = md5(?)");

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

    protected Credentials getCredentials() {
        Main.Credentials credentials;
        String username;
        String password;

        java.lang.System.out.print("Username: ");
        username = scanner.nextLine();

        if (console != null) {
            password = new String(console.readPassword("Password: "));
        }else{
            java.lang.System.out.print("Password: ");
            password = scanner.nextLine();
        }
        credentials = new Credentials(username,password);
        return credentials;
    }

    @SuppressWarnings("WeakerAccess")
    static class Credentials {
        protected final String username;
        protected final String password;

        public Credentials(String username, String password){
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    protected void showOptions(){
        while (!userExiting) {
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
                userExiting = true;
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
                userExiting = true;
                return;
            case 1:
                newRegistration();
                break;
            case 2:
                break;
        }
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
