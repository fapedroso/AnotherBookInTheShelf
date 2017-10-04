package br.ufsm.piveta.system;

import br.ufsm.piveta.system.entities.*;

import java.io.Console;
import java.sql.*;
import java.time.LocalDate;
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
        scanner = new Scanner(System.in);
        console = System.console();

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
                    System.out.println("Bad credentials. Try again");
                } else {
                    System.out.println("Too many attempts.");
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

        System.out.print("Username: ");
        username = scanner.nextLine();

        if (console != null) {
            password = new String(console.readPassword("Password: "));
        }else{
            System.out.print("Password: ");
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
            if (userExiting) {
                System.out.print("Do you really want to exit [yN]? ");
                userExiting = getYesOfNoOption(false);
            }
        }
    }

    protected void showLibrarianMainOptions(){
        System.out.println("Choose an action:\n");
        System.out.println("[1] Manage Books");
        System.out.println("[0] Exit");

        switch (getValidOption(1)){
            case 0:
                userExiting = true;
                return;
            case 1:
                break;
        }
    }

    protected void showNonLibrarianMainOptions(){
        System.out.println("Choose an action:\n");
        System.out.println("[1] New Reservations");
        System.out.println("[2] Cancel Reservations");
        System.out.println("[3] Renew Loan");
        System.out.println("[0] Exit");

        switch (getValidOption(3)){
            case 0:
                userExiting = true;
                return;
            case 1:
                newReservation();
                break;
            case 2:
                cancelReservation();
                break;
            case 3:
                renewLoans();
                break;
        }
    }

    public int getValidOption(int maxValid) {
        return getValidOption(scanner,maxValid);
    }

    public static int getValidOption(Scanner scanner,int maxValid) {
        System.out.printf("\nSelect [0-%d]: ", maxValid);

        int choice;

        while ((choice = scanner.nextInt()) > maxValid || choice < 0){
            System.out.printf("Invalid choice, please enter a number between 0 and %d: ",maxValid);
        }

        return choice;
    }

    public boolean getYesOfNoOption(boolean defaultChoice) {
        return getYesOfNoOption(scanner,defaultChoice);
    }

    public static boolean getYesOfNoOption(Scanner scanner, boolean defaultChoice) {
        String next;

        do {
            next = scanner.nextLine();
        } while (next.isEmpty());

        char c = next.charAt(0);

        boolean positive = (c == 'y' || c == 'Y' || c == 's' || c == 'S');
        boolean negative = (c == 'n' || c == 'N');

        return positive || !negative && defaultChoice;
    }

    public static void clearConsole(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    protected LiteraryWork searchForLiteraryWorkByISBN() {
        Locator<LiteraryWork, String> locator = new Locator<LiteraryWork, String>(scanner) {
            @Override
            protected LiteraryWork search(String searchable) throws SQLException {
                return LiteraryWork.getByISBN(connection,searchable);
            }

            @Override
            protected String getSearchablePart(String userEntry) {
                return userEntry;
            }

            @Override
            protected String getEntity() {
                return "book";
            }

            @Override
            protected String getPrompt() {
                return "Enter a book ISBN: ";
            }
        };
        return locator.consoleSearch();
    }

    protected LiteraryWork searchForLiteraryWorkByTitle() {
        LocatorAndSelector<LiteraryWork, String> locator = new LocatorAndSelector<LiteraryWork, String>(scanner) {
            @Override
            protected String getSearchablePart(String userEntry) { return userEntry; }

            @Override
            protected String getEntity() {
                return "book";
            }

            @Override
            protected String getPrompt() {
                return "Enter a book title or part of it: ";
            }

            @Override
            protected List<LiteraryWork> searchSeveral(String searchable) throws SQLException {
                return LiteraryWork.getByTitle(connection, searchable);
            }

            @Override
            protected String getEntities() {
                return "books";
            }
        };
        return locator.consoleSearch();
    }
    protected Publisher searchForPublisherByName() {
        LocatorAndSelector<Publisher,String> locator = new LocatorAndSelector<Publisher, String>(scanner) {
            @Override
            protected List<Publisher> searchSeveral(String searchable) throws SQLException {
                return Publisher.getByName(connection,searchable);
            }

            @Override
            protected String getEntities() { return "publishers"; }

            @Override
            protected String getSearchablePart(String userEntry) { return userEntry; }

            @Override
            protected String getEntity() { return "publisher"; }

            @Override
            protected String getPrompt() { return "Enter a publisher name: "; }
        };
        return locator.consoleSearch();
    }

    protected LiteraryWork searchForLiteraryWorkByPublisher() {
        LocatorAndSelector<LiteraryWork,Publisher> locator = new LocatorAndSelector<LiteraryWork, Publisher>(scanner) {
            @Override
            protected List<LiteraryWork> searchSeveral(Publisher searchable) throws SQLException {
                return LiteraryWork.getByPublisher(connection,searchable.getId());
            }

            @Override
            protected String getEntities() { return "books";}

            @Override
            protected Publisher getSearchablePartWithoutUserInput() {
                return searchForPublisherByName();
            }

            @Override
            protected String getEntitySearchable(LiteraryWork entity) {
                return entity.getPublisher().toString();
            }

            @Override
            protected String getEntity() {
                return "book";
            }

            @Override
            protected String getParameterName() {
                return "publisher";
            }
        };
        return locator.consoleSearch();
    }

    protected Author searchForAuthorByName() {
        LocatorAndSelector<Author,String> locator = new LocatorAndSelector<Author, String>(scanner) {
            @Override
            protected List<Author> searchSeveral(String searchable) throws SQLException {
                return Author.getByName(connection,searchable);
            }

            @Override
            protected String getEntities() { return "authors"; }

            @Override
            protected String getSearchablePart(String userEntry) { return userEntry; }

            @Override
            protected String getEntity() { return "author"; }

            @Override
            protected String getParameterName() {
                return "name";
            }

            @Override
            protected String getPrompt() { return "Enter an author name: "; }
        };
        return locator.consoleSearch();
    }
    protected LiteraryWork searchForLiteraryWorkByAuthor() {
        LocatorAndSelector<LiteraryWork,Author> locator = new LocatorAndSelector<LiteraryWork, Author>(scanner) {
            @Override
            protected List<LiteraryWork> searchSeveral(Author searchable) throws SQLException {
                return LiteraryWork.getByAuthor(connection,searchable.getId());
            }

            @Override
            protected Author getSearchablePartWithoutUserInput() {
                return searchForAuthorByName();
            }

            @Override
            protected String getEntities() {
                return "books";
            }

            @Override
            protected String getEntitySearchable(LiteraryWork entity) {
                return entity.getAuthor().toString();
            }

            @Override
            protected String getEntity() {
                return "book";
            }

            @Override
            protected String getParameterName() {
                return "author";
            }
        };
        return locator.consoleSearch();
    }

    protected LiteraryWork searchForLiteraryWork() {
        clearConsole();
        System.out.println("Choose a method of book selection:\n");
        System.out.println("[1] ISBN");
        System.out.println("[2] Title");
        System.out.println("[3] Publisher");
        System.out.println("[4] Author");
        System.out.println("[0] Cancel");

        switch (getValidOption(4)){
            case 0:
                return null;
            case 1:
                return searchForLiteraryWorkByISBN();
            case 2:
                return searchForLiteraryWorkByTitle();
            case 3:
                return searchForLiteraryWorkByPublisher();
            case 4:
                return searchForLiteraryWorkByAuthor();
            default:
                return null;
        }
    }

    private void renewLoans() {
        clearConsole();
        try {
            int i = 0;
            List<Loan> loans = Loan.getRenewableByUser(connection, loggedUser);
            if (loans.isEmpty()) {
                System.out.println("\nThere are no loans available to renew.");
                return;
            }
            System.out.printf("Choose a loan: \n\n");
            for (Loan loan : loans) {
                System.out.printf("[%d] %s\n",++i,loan.getBook().getLiteraryWork().toString()
                        + " - "+ loan.getDueTo());
            }
            System.out.print("[0] Cancel");

            int choice = getValidOption(i);

            if (choice == 0) return;

            Loan loan = loans.get(choice-1);

            loan.setDueTo(LocalDate.now().plusDays(loggedUser.getLoanPeriod()));
            if (loan.save()){
                System.out.println("Your loan was renewed successfully.");
            } else {
                System.out.println("Some error has happened and your loan could not be renewed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cancelReservation() {
        clearConsole();
        try {
            int i = 0;
            List<Reservation> reservations = Reservation.getByAfterDateByUser(connection, LocalDate.now(), loggedUser);
            if (reservations.isEmpty()) {
                System.out.println("\nThere are no reservation to cancel.");
                return;
            }
            System.out.printf("Choose a reservation: \n\n");
            for (Reservation reservation : reservations) {
                System.out.printf("[%d] %s\n",++i,reservation.getBook().getLiteraryWork().toString()
                        + " - "+ reservation.getReservedFor());
            }
            System.out.print("[0] Cancel");

            int choice = getValidOption(i);

            if (choice == 0) return;

            reservations.get(choice-1).remove();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    protected void newReservation(){
        clearConsole();
        try {
            if (loggedUser.getNonPaidFines().isEmpty()){
                LiteraryWork literaryWork = searchForLiteraryWork();
                if (literaryWork == null) return;
                System.out.println("Choose a date to reserve:\n");
                LocalDate currentDate = LocalDate.now();
                LocalDate dateToReserve;
                for (int i = 1; i <= 7; i++){
                    System.out.printf("[%d] %s\n",i, currentDate.plusDays(i).toString());
                }
                System.out.println("[0] Cancel");

                int option = getValidOption(7);

                if (option > 0){
                    dateToReserve = currentDate.plusDays(option);
                } else return;

                List<Book> books = Book.getAvailableBooksForDateByLiteraryWork(connection,literaryWork,dateToReserve);

                if (books.isEmpty()) {
                    System.out.println("Sorry but all copies of %s are unavailable to withdraw in this date.");
                } else {
                    Book reservedBook = books.get(0);
                    Reservation.create(connection,loggedUser.getId(),reservedBook.getId(),dateToReserve);
                    System.out.println("Your reservation has been registered with success.");
                }
                while (scanner.hasNext()) {scanner.next();}
                System.out.println("Press [Enter] to continue.");
                scanner.nextLine();
            } else {
                System.out.print("You cannot reserve a book while having an unpaid fine.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
