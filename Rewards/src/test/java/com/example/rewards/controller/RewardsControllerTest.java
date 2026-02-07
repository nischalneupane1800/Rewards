package com.example.rewards.controller;

import com.example.rewards.model.Customer;
import com.example.rewards.model.Transaction;
import com.example.rewards.repository.DataRepository;
import com.example.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@WebFluxTest(controllers = RewardsController.class)
@Import(RewardsService.class)
class RewardsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DataRepository repo;

    @Test
    void getRewardsForCustomer_returnsSummaryShape() {
        // Use dates inside the last 3 months relative to "now" in the controller.
        Mockito.when(repo.transactionsForCustomer("c1")).thenReturn(Flux.just(
                new Transaction("t1", "c1", 120.0, LocalDate.now().withDayOfMonth(1)),
                new Transaction("t2", "c1", 100.0, LocalDate.now().minusMonths(1).withDayOfMonth(1))
        ));

        webTestClient.get()
                .uri("/rewards/c1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("application/json")
                .expectBody()
                .jsonPath("$.monthlyRewards").isArray()
                .jsonPath("$.totalPoints").isNumber();
    }

    @Test
    void getRewardsForAllCustomers_defaultSort_customerIdAsc() {
        Mockito.when(repo.customers()).thenReturn(Flux.just(
                new Customer("c2", "Alex"),
                new Customer("c1", "David")
        ));

        // c1 has higher total, but default sort is customerId asc -> c1 then c2
        Mockito.when(repo.transactionsForCustomer("c1")).thenReturn(Flux.just(
                new Transaction("t1", "c1", 200.0, LocalDate.now().withDayOfMonth(2)) // 250 pts
        ));
        Mockito.when(repo.transactionsForCustomer("c2")).thenReturn(Flux.just(
                new Transaction("t2", "c2", 75.0, LocalDate.now().withDayOfMonth(2))  // 25 pts
        ));

        webTestClient.get()
                .uri("/rewards")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo("c1")
                .jsonPath("$[1].customerId").isEqualTo("c2");
    }

    @Test
    void getRewardsForAllCustomers_sortByTotalPointsDesc() {
        Mockito.when(repo.customers()).thenReturn(Flux.just(
                new Customer("c1", "David"),
                new Customer("c2", "Alex")
        ));

        Mockito.when(repo.transactionsForCustomer("c1")).thenReturn(Flux.just(
                new Transaction("t1", "c1", 75.0, LocalDate.now().withDayOfMonth(2)) // 25 pts
        ));
        Mockito.when(repo.transactionsForCustomer("c2")).thenReturn(Flux.just(
                new Transaction("t2", "c2", 200.0, LocalDate.now().withDayOfMonth(2)) // 250 pts
        ));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rewards")
                        .queryParam("sort", "totalPoints,desc")
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo("c2")
                .jsonPath("$[0].rewards.totalPoints").isNumber()
                .jsonPath("$[1].customerId").isEqualTo("c1");
    }
}