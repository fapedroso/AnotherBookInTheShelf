package br.ufsm.piveta.system;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public abstract class LocatorAndSelector<T,R> extends Locator<T,R>{

    protected abstract List<T> searchSeveral(R searchable) throws SQLException;
    protected abstract String getEntities();


    LocatorAndSelector(Scanner scanner) {
        super(scanner);
    }

    @Override
    protected T search(R searchable) throws SQLException {
        return null;
    }

    @Override
    public T consoleSearch(){
        Main.clearConsole();
        int i = 0;

        R searchable = getSearchable();

        if (searchable == null) return null;

        try {
            List<T> entities = searchSeveral(searchable);
            if (entities.isEmpty()){
                System.out.printf("No %s was found with %s %s. Would you like to try again [yN]? ",
                        getEntity(),getParameterName(),getUserEntry(searchable));
                if (Main.getYesOfNoOption(scanner,false)) return consoleSearch(); else return null;
            } else {
                System.out.printf("Those are the %s I've found:\n\n",getEntities());

                for (T entity: entities){
                    System.out.printf("[%d] %s\n",++i,entity.toString());
                }
                System.out.println("[0] Cancel\n");
                int index = Main.getValidOption(scanner,i);
                if (index == 0){
                    System.out.print("Would you like to search again [yN]? ");
                    if (Main.getYesOfNoOption(scanner,false)) return consoleSearch(); else return null;
                } else {
                    return entities.get(index-1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
