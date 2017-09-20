package br.ufsm.piveta.library;

import java.io.Console;
import java.sql.*;
import java.util.Scanner;

@SuppressWarnings("WeakerAccess")
public class Library {

    protected Scanner scanner;
    protected Connection connection;


    Library() throws SQLException {
        scanner = new Scanner(System.in);
        connection = DriverManager.getConnection("jdbc:postgresql://emilio.pedrollo.nom.br/postgres",
                "teste", "piveta");
    }

    protected boolean login() throws SQLException {

        Console console = System.console();
        int tries = 0;
        String username = null;
        String password = null;

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT count(1) FROM users WHERE username = ? AND password = md5(?)"
        );

        while (tries < 3){
            System.out.print("Username: ");
            username = scanner.nextLine();

            if (console != null) {
                password = new String(console.readPassword("Password: "));
            }else{
                System.out.print("Password: ");
                password = scanner.nextLine();
            }

            preparedStatement.setString(1,username);
            preparedStatement.setString(2,password);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            boolean success = resultSet.getInt(1) > 0;

            resultSet.close();

            if (success) return true;

            System.out.println("Bad Credentials."+((++tries < 3)?" Try Again.":""));
        }

        return false;
    }



    public void start() throws SQLException {

        if (!this.login()){
            return;
        }

        System.out.println("You are logged on");


    }
}
