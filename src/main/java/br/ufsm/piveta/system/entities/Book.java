package br.ufsm.piveta.system.entities;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Book {
    private Connection connection;
    private Integer id;
    private Integer literary_work_id;
    private LiteraryWork literary_work;
    private String code;
    private String condition;

    protected Book(Integer id, Integer literary_work_id, String code, String condition){
        this.id = id;
        this.literary_work_id = literary_work_id;
        this.code = code;
        this.condition = condition;

    }

    @Override
    public String toString() {
        return getCode();
    }

    @Nullable
    protected static Book getFromResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new Book(
                    resultSet.getInt(1),    // id
                    resultSet.getInt(2),    // literary_work_id
                    resultSet.getString(3), // code
                    resultSet.getString(4)  // condition
            );
        }else return null;
    }

    protected static List<Book> getListFromPreparedStatement(PreparedStatement preparedStatement)
            throws SQLException {
        List<Book> books = new ArrayList<>();

        ResultSet resultSet = preparedStatement.executeQuery();

        Book book;

        while ((book = getFromResultSet(resultSet)) != null) {
            book.setConnection(preparedStatement.getConnection());
            books.add(book);
        }

        return books;
    }

    protected static Book getFromPreparedStatement(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();

        Book book = getFromResultSet(resultSet);

        if (book != null) {
            book.setConnection(preparedStatement.getConnection());
        }

        return book;
    }

    public static Book get(Connection connection, int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, literary_work_id, cod, condition FROM books WHERE id = ?");

        preparedStatement.setInt(1, id);

        return getFromPreparedStatement(preparedStatement);
    }

    public static List<Book> getByTitle(Connection connection, String title) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "JOIN literary_works ON books.literary_work_id = literary_works.id " +
                        "WHERE title ILIKE CONCAT('%',?,'%')");

        preparedStatement.setString(1, title);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Book> getByISBN(Connection connection, String isbn) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "JOIN literary_works ON books.literary_work_id = literary_works.id " +
                        "WHERE isbn = ?");

        preparedStatement.setString(1, isbn);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Book> getByLiteraryWork(Connection connection, LiteraryWork literaryWork) throws SQLException {
        return getByLiteraryWork(connection, literaryWork.getId());
    }
    public static List<Book> getByLiteraryWork(Connection connection, int literaryWorkId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "WHERE literary_work_id = ?");

        preparedStatement.setInt(1, literaryWorkId);

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Book> getAvailableBooksByLiteraryWork(Connection connection, LiteraryWork literaryWork)
            throws SQLException {
        return getAvailableBooksForDateByLiteraryWork(connection,literaryWork,LocalDate.now());
    }

    public static List<Book> getAvailableBooksForDateByLiteraryWork(Connection connection, LiteraryWork literaryWork,
                                                                    LocalDate date) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books " +
                        "LEFT JOIN reservations ON books.id = reservations.book_id " +
                        "LEFT JOIN loans ON books.id = loans.book_id " +
                        "WHERE books.literary_work_id = ? " +
                        "GROUP BY books.id " +
                        "HAVING (max(loans.due_to) < ? OR count(loans) = 0) " +
                        "AND (max(reservations.reserved_for) < current_date OR count(reservations) = 0)");

        preparedStatement.setInt(1, literaryWork.getId());
        preparedStatement.setDate(2, java.sql.Date.valueOf(date));

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Book> getByAuthor(Connection connection, Author author) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "JOIN literary_works ON books.literary_work_id = literary_works.id " +
                        "JOIN authors ON literary_works.author_id = authors.id " +
                        "WHERE authors.id = ?");

        preparedStatement.setInt(1, author.getId());

        return getListFromPreparedStatement(preparedStatement);
    }

    public static List<Book> getByPublisher(Connection connection, Publisher publisher) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT books.id, books.literary_work_id, books.cod, books.condition FROM books "+
                        "JOIN literary_works ON books.literary_work_id = literary_works.id " +
                        "JOIN publishers ON literary_works.publisher_id = publishers.id " +
                        "WHERE publishers.id = ?");

        preparedStatement.setInt(1, publisher.getId());

        return getListFromPreparedStatement(preparedStatement);
    }

    @Nullable
    public static Book create(Connection connection, Integer literaryWorkId, String code, String condition)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO books (literary_work_id, cod, condition) values (?,?,?)");

        preparedStatement.setInt(1,literaryWorkId);
        preparedStatement.setString(2,code);
        preparedStatement.setString(3,condition);

        if (!preparedStatement.execute()) return null;

        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()){
            int id = resultSet.getInt(1);
            return new Book(id, literaryWorkId, code, condition);
        } else return null;
    }

    public boolean save() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "UPDATE books SET cod = ?, condition = ? WHERE id = ?");

        preparedStatement.setString(1, getCode());
        preparedStatement.setString(2, getCondition());

        preparedStatement.setInt(3, getId());

        return preparedStatement.executeUpdate() == 1;
    }

    public boolean remove() throws SQLException {
        PreparedStatement preparedStatement = getConnection().prepareStatement(
                "DELETE FROM books WHERE id = ?");

        preparedStatement.setInt(1, getId());

        return preparedStatement.execute();
    }

    public Integer getId() {
        return id;
    }

    public Integer getLiteraryWorkId() {
        return literary_work_id;
    }

    public LiteraryWork getLiteraryWork() throws SQLException {
        if (this.literary_work == null){
            literary_work = LiteraryWork.get(getConnection(),getLiteraryWorkId());
        }
        return literary_work;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection){
        this.connection = connection;
    }
}
