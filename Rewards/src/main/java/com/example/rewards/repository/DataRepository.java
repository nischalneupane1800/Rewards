package com.example.rewards.repository;

import com.example.rewards.model.Customer;
import com.example.rewards.model.Transaction;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

@Repository
public class DataRepository {

    private final List<Customer> customers = List.of(
            new Customer("c1", "David"),
            new Customer("c2", "Alex")
    );

    private final List<Transaction> transactions = List.of(
            new Transaction("t1", "c1", 49.99, LocalDate.now().minusMonths(2).withDayOfMonth(5)),
            new Transaction("t2", "c1", 50.00, LocalDate.now().minusMonths(2).withDayOfMonth(6)),
            new Transaction("t3", "c1", 100.00, LocalDate.now().minusMonths(1).withDayOfMonth(10)),
            new Transaction("t4", "c1", 120.00, LocalDate.now().withDayOfMonth(15)),

            new Transaction("t5", "c2", 75.00, LocalDate.now().minusMonths(2).withDayOfMonth(3)),
            new Transaction("t6", "c2", 200.00, LocalDate.now().minusMonths(1).withDayOfMonth(12)),
            new Transaction("t7", "c2", 51.00, LocalDate.now().withDayOfMonth(20))
    );

    public Flux<Customer> customers() {
        return Flux.fromIterable(customers);
    }

    public Flux<Transaction> transactions() {
        return Flux.fromIterable(transactions);
    }

    public Flux<Transaction> transactionsForCustomer(String customerId) {
        return transactions().filter(t -> t.customerId().equals(customerId));
    }
}