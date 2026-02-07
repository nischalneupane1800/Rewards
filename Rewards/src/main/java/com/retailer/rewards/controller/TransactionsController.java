package com.retailer.rewards.controller;

import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> transactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{customerId}")
    public List<Transaction> transactionsForCustomer(
            @PathVariable String customerId
    ) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must be provided");
        }
        return transactionService.getTransactionsForCustomer(customerId);
    }
}
