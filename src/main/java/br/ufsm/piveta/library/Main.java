package br.ufsm.piveta.library;

import java.sql.*;
import java.lang.*;

@SuppressWarnings("WeakerAccess")
public class Main {
    public static void main(String[] args) {

        Library library;

        try {
            library = new Library();
            library.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
