package edu.sdccd.cisc191;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class BookManager {

    private BookLinkedList bookList = new BookLinkedList();
    private BookDAO bookDAO = new BookDAO();
    private Runnable bookLoaded;

    public BookManager()
    {
        loadBooksConcurrently();
    }

    public void setBookLoaded(Runnable bookLoaded)
    {
        this.bookLoaded = bookLoaded;
    }

    public void loadBooksConcurrently() {
        javafx.concurrent.Task<ObservableList<BookOrder>> loadBook = new javafx.concurrent.Task()
        {
            @Override
            protected ObservableList<BookOrder> call()
            {
                return FXCollections.observableArrayList(bookDAO.getAllBooks());
            }
        };

        loadBook.setOnSucceeded(event -> {
            ObservableList<BookOrder> books = loadBook.getValue();

            bookList = new BookLinkedList();
            books.forEach(bookList::add);

            books.forEach(book -> { System.out.println(book.getBookName()); });

            if (bookLoaded != null) {
                bookLoaded.run();
            }
        });

        loadBook.setOnFailed(event -> {
            System.err.println("Failed to load books.");
            loadBook.getException().printStackTrace();
        });

        new Thread(loadBook).start();
    }


    public ObservableList<BookOrder> getBooks()
    {
        return bookList.toObservableList();
    }

    public void addBook(BookOrder book) {
        bookList.add(book);
        bookDAO.addBook(book);
    }

    public void removeBook(BookOrder book) {
        bookList.remove(book);
        bookDAO.deleteBook(book.getId());
    }

    public void sortBookByName(){
        ObservableList<BookOrder> sortedBooks = FXCollections.observableArrayList(
                getBooks().stream()
                        .sorted(Comparator.comparing(BookOrder::getBookName))
                        .collect(Collectors.toList()));

        bookList = new BookLinkedList();
        sortedBooks.forEach(bookList::add);
    }

}
