//package fit.hutech.spring.repositories;
//
//import fit.hutech.spring.entities.Book;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface IBookRepository extends PagingAndSortingRepository<Book, Long>, JpaRepository<Book, Long> {
//    @Query("""
//            SELECT b FROM Book b
//            WHERE b.title LIKE %?1%
//            OR b.author LIKE %?1%
//            OR b.category.name LIKE %?1%
//            """)
//    List<Book> searchBook(String keyword);
//
//    default List<Book> findAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
//        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy)))
//                    .getContent();
//    }
//}

package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Book;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


//@Repository
//public interface IBookRepository extends PagingAndSortingRepository<Book, Long>, JpaRepository<Book, Long> {
//    @Query("""
//            SELECT b FROM Book b
//            WHERE b.title LIKE %?1%
//            OR b.author LIKE %?1%
//            OR b.category.name LIKE %?1%
//            """)
//    List<Book> searchBook(String keyword);
//
//    default List<Book> findAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
//        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy))).getContent();
//    }
//
//    @Query("SELECT COUNT(b) FROM Book b")
//    long countAllBooks();
//}
@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {
    @Query("""
            SELECT b FROM Book b
            WHERE b.title LIKE %?1%
            OR b.author LIKE %?1%
            OR b.category.name LIKE %?1%
            """)
    List<Book> searchBook(String keyword);

    default List<Book> findAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Slice<Book> bookSlice = findAll(pageable);
        return bookSlice.getContent();
    }

    @Query("SELECT COUNT(b) FROM Book b")
    long countAllBooks();
}