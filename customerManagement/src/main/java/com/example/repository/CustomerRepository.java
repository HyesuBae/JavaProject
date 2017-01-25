package com.example.repository;

import com.example.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by hyesubae on 16. 7. 12.
 */
public interface CustomerRepository extends JpaRepository<Customer, Integer>{
    @Query("SELECT x From Customer x ORDER BY x.firstName, x.lastName")
    List<Customer> findAllOrderByName();
}
