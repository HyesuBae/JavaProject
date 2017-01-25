package com.example;

import com.example.domain.Customer;
import com.example.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by hyesubae on 16. 7. 12.
 */
@EnableAutoConfiguration
@ComponentScan
public class App implements CommandLineRunner{
    @Autowired
    CustomerRepository customerRepository;


    @Override
    public void run(String... strings) throws Exception {
        Customer created = customerRepository.save(new Customer(null, "Hyesu", "Bae"));
        System.out.println(created + " is created!");

        customerRepository.findAllOrderByName().forEach(System.out::println);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
