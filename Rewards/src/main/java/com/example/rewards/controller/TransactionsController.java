package com.example.rewards.controller;

import com.example.rewards.model.Transaction;
import com.example.rewards.repository.DataRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TransactionsController {

    private final DataRepository repo;

    public TransactionsController(DataRepository repo) {
        this.repo = repo;
    }

    // All transactions
    @GetMapping("/transactions")
    public Flux<Transaction> transactions() {
        return repo.transactions();
    }

    // Transactions for one customer by id
    @GetMapping("/transactions/{customerId}")
    public Flux<Transaction> transactionsForCustomer(@PathVariable String customerId) {
        return repo.transactionsForCustomer(customerId);
    }
}