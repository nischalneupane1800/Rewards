package com.example.rewards.controller;

import com.example.rewards.model.Transaction;
import com.example.rewards.repository.DataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@WebFluxTest(controllers = TransactionsController.class)
class TransactionsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DataRepository repo;

    @Test
    void getAllTransactions_returnsList() {
        Mockito.when(repo.transactions()).thenReturn(Flux.just(
                new Transaction("t1", "c1", 120.0, LocalDate.of(2026, 2, 1)),
                new Transaction("t2", "c2", 75.0,  LocalDate.of(2026, 1, 10))
        ));

        webTestClient.get()
                .uri("/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("t1")
                .jsonPath("$[0].customerId").isEqualTo("c1")
                .jsonPath("$[0].amount").isEqualTo(120.0)
                .jsonPath("$[0].date").isEqualTo("2026-02-01")
                .jsonPath("$[1].id").isEqualTo("t2")
                .jsonPath("$[1].customerId").isEqualTo("c2");
    }

    @Test
    void getTransactionsByCustomerId_returnsOnlyThatCustomer() {
        Mockito.when(repo.transactionsForCustomer("c1")).thenReturn(Flux.just(
                new Transaction("t1", "c1", 120.0, LocalDate.of(2026, 2, 1)),
                new Transaction("t3", "c1", 100.0, LocalDate.of(2026, 1, 1))
        ));

        webTestClient.get()
                .uri("/transactions/c1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo("c1")
                .jsonPath("$[1].customerId").isEqualTo("c1")
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void getTransactionsByCustomerId_whenNone_returnsEmptyArray() {
        Mockito.when(repo.transactionsForCustomer("does-not-exist")).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/transactions/does-not-exist")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }
}