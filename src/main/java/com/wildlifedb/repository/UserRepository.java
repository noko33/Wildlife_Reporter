package com.wildlifedb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wildlifedb.entity.User;

/*
 * This JPA repository automatically maps any instance of User into MySQL DB. 
 * The configuration for DB is done in application.properties.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /*
     This would return an Optional object which might have null or non-null value as its content.
     In order to access its content, write object.get() which would return the content.
     The below function automatically maps into query of "SELECT u FROM User u WHERE email=:email".
     
     More information for auto generated query can be found in Spring Documentation of JPA Repository.

     In order to add any custom query, use @Query annotation.
    */
    public Optional<User> findByEmail(String email); 
    public Optional<User> findByUserId(String user_id);
    public Optional<User> findById(int id); 

    @Query(value="select * from user where user.verifier =?1 ",nativeQuery=true)
    List<User> findAllverfied(Boolean verifier);



}