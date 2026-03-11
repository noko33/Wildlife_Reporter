package team15.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import team15.backend.entity.TaxonomyOrder;

/*
 * This JPA repository automatically maps any instance of User into MySQL DB. 
 * The configuration for DB is done in application.properties.
 */
@Repository
public interface OrderRepository extends JpaRepository<TaxonomyOrder, Long> {
    
    /*
     This would return an Optional object which might have null or non-null value as its content.
     In order to access its content, write object.get() which would return the content.
     The below function automatically maps into query of "SELECT u FROM User u WHERE email=:email".
     
     More information for auto generated query can be found in Spring Documentation of JPA Repository.

     In order to add any custom query, use @Query annotation.
    */
    public Optional<TaxonomyOrder> findByOrderId(String orderId); 
}