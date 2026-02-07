package com.example.rewards.repository;

import com.example.rewards.model.Customer;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryTest {

    private final DataRepository repo = new DataRepository();

    @Test
    void customers_returnsTwoCustomers() {
        StepVerifier.create(repo.customers().collectList())
                .assertNext(customers -> {
                    assertEquals(2, customers.size());

                    Set<String> ids = customers.stream().map(Customer::id).collect(Collectors.toSet());
                    assertEquals(Set.of("c1", "c2"), ids);

                    assertTrue(customers.stream().allMatch(c -> c.name() != null && !c.name().isBlank()));
                })
                .verifyComplete();
    }

    @Test
    void transactions_returnsAllTransactions() {
        StepVerifier.create(repo.transactions().collectList())
                .assertNext(txns -> {
                    assertEquals(7, txns.size());
                    assertTrue(txns.stream().allMatch(t -> t.id() != null && !t.id().isBlank()));
                    assertTrue(txns.stream().allMatch(t -> t.customerId() != null && !t.customerId().isBlank()));
                })
                .verifyComplete();
    }

    @Test
    void transactionsForCustomer_c1_returnsOnlyC1Transactions() {
        StepVerifier.create(repo.transactionsForCustomer("c1").collectList())
                .assertNext(txns -> {
                    assertEquals(4, txns.size());
                    assertTrue(txns.stream().allMatch(t -> "c1".equals(t.customerId())));
                })
                .verifyComplete();
    }

    @Test
    void transactionsForCustomer_c2_returnsOnlyC2Transactions() {
        StepVerifier.create(repo.transactionsForCustomer("c2").collectList())
                .assertNext(txns -> {
                    assertEquals(3, txns.size());
                    assertTrue(txns.stream().allMatch(t -> "c2".equals(t.customerId())));
                })
                .verifyComplete();
    }

    @Test
    void transactionsForCustomer_unknownCustomer_returnsEmpty() {
        StepVerifier.create(repo.transactionsForCustomer("nope").collectList())
                .assertNext(txns -> assertTrue(txns.isEmpty()))
                .verifyComplete();
    }
}