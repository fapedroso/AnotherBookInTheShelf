package br.ufsm.piveta.system;

import java.sql.*;
import java.lang.*;

@SuppressWarnings("WeakerAccess")
public class Main {
    public static void main(String[] args) {

        System system;

        try {
            system = new System(System.ENVIRONMENT_GUI);
            system.start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
