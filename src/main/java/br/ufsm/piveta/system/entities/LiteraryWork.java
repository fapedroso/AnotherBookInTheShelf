package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LiteraryWork {

    private Connection connection;
    private Integer id;
    private Integer author_id;
    private Author author = null;
    private Integer publisher_id;
    private Publisher publisher = null;
    private String title;
    private String isbn;
    private Integer edition;
    private Integer year;

    LiteraryWork(Integer id, Integer author_id, Integer publisher_id, String title, String isbn,
                 Integer edition, Integer year){
        this.id = id;
        this.author_id = author_id;
        this.publisher_id = publisher_id;
        this.title = title;
        this.isbn = isbn;
        this.edition = edition;
        this.year = year;
    }



    @Nullable
    protected static LiteraryWork getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new LiteraryWork(
                    resultSet.getInt(1),    // id
                    resultSet.getInt(2),    // author_id
                    resultSet.getInt(3),    // publisher_id
                    resultSet.getString(4), // title
                    resultSet.getString(5), // isbn
                    resultSet.getInt(6),    // edition
                    resultSet.getInt(7)     // year
            );
        }else return null;
    }

    protected static List<LiteraryWork> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<LiteraryWork> literaryWorks = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        LiteraryWork literaryWork;

        while ((literaryWork = getFromResultSet(resultSet)) != null) {
            literaryWork.setConnection(preparedStatement.getConnection());
            literaryWorks.add(literaryWork);
        }

        return literaryWorks;
    }

    protected static LiteraryWork getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        LiteraryWork literaryWork = getFromResultSet(resultSet);

        if (literaryWork != null) {
            literaryWork.setConnection(preparedStatement.getConnection());
        }

        return literaryWork;
    }

    @Nullable
    public static LiteraryWork create(Connection connection, Integer author_id, Integer publisher_id, String title,
                                      String isbn, Integer edition, Integer year)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO literary_works (author_id, publisher_id, title, isbn, edition, year) "+
                        "values (?,?,?,?,?,?)");

        preparedStatement.setInt(1,author_id);
        preparedStatement.setInt(1,publisher_id);
        preparedStatement.setString(1,title);
        preparedStatement.setString(1,isbn);
        preparedStatement.setInt(1,edition);
        preparedStatement.setInt(1,year);

        if (!preparedStatement.execute()) return null;

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()){
            int id = resultSet.getInt(1);
            return new LiteraryWork(id, author_id,publisher_id,title,isbn,edition,year);
        } else return null;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM literary_works WHERE id = ?");

        preparedStatement.setInt(1, getId());

        return preparedStatement.execute();
    }

    public Integer getId() {
        return id;
    }

    public Integer getAuthor_id() {
        return author_id;
    }

    public Author getAuthor() {
        return author;
    }

    public Integer getPublisher_id() {
        return publisher_id;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getEdition() {
        return edition;
    }

    public Integer getYear() {
        return year;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
