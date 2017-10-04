package br.ufsm.piveta.system;

import java.sql.SQLException;
import java.util.Scanner;

public abstract class Locator<T,R> {

    protected Scanner scanner;

    protected String userEntry;

    protected abstract T search(R searchable) throws SQLException;

    protected abstract String getEntity();

    protected String getParameterName() {
        return null;
    }

    protected String getEntitySearchable(T entity){
        return entity.toString();
    }
    protected String getPrompt(){
        return null;
    }
    protected R getSearchablePart(String userEntry) {
        return null;
    }
    protected R getSearchablePartWithoutUserInput(){
        return null;
    }

    Locator(Scanner scanner){
        this.scanner = scanner;
    }

    protected R getSearchable() {
        R searchable = getSearchablePartWithoutUserInput();
        if (searchable == null && getPrompt() != null) {
            System.out.print(getPrompt());
            do {
                userEntry = scanner.nextLine();
            } while (userEntry.isEmpty());
            searchable = getSearchablePart(userEntry);
        }
        return searchable;
    }

    protected String getUserEntry(R searchable){
        return (userEntry != null)? userEntry : searchable.toString();
    }

    public T consoleSearch(){
        Main.clearConsole();

        R searchable = getSearchable();

        if (searchable == null) return null;

        try {
            T entity = search(searchable);
            if (entity == null) {
                System.out.printf("No %s was found with %s %s. Would you like to try again [yN]? ",
                        getEntity(),getParameterName(),getUserEntry(searchable));
                if (Main.getYesOfNoOption(scanner,false)) return consoleSearch(); else return null;
            } else {
                if (entity.toString().equals(getEntitySearchable(entity))){
                    System.out.printf("Found %s %s. Is that correct [Yn]? ",
                            getEntity(),entity.toString());
                } else System.out.printf("Found %s %s with %s %s. Is that correct [Yn]? ",
                        getEntity(),entity.toString(),getParameterName(),getEntitySearchable(entity));
                if (Main.getYesOfNoOption(scanner,true)) return entity; else {
                    System.out.print("Would you like to search again [Yn]? ");
                    return (Main.getYesOfNoOption(scanner,true))? consoleSearch(): null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
