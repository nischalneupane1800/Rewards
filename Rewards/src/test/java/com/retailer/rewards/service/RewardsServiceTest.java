package com.retailer.rewards.service;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.dto.CustomerRewards;
import com.retailer.rewards.dto.RewardSummary;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private final Clock clock =
            Clock.fixed(Instant.parse("2026-02-15T00:00:00Z"), ZoneId.of("UTC"));


    @InjectMocks
    private RewardsService rewardsService;

    @BeforeEach
    void setup() {
        rewardsService =
                new RewardsService(customerRepository, transactionRepository, clock);
    }

    @Nested
    @DisplayName("calculatePoints — core reward logic")
    class CalculatePointsTests {

        @Test
        void zeroAmount_returnsZero() {
            assertEquals(0, rewardsService.calculatePoints(0.0));
        }

        @Test
        void negativeAmount_returnsZero() {
            assertEquals(0, rewardsService.calculatePoints(-10.0));
        }

        @Test
        void underFifty_returnsZero() {
            assertEquals(0, rewardsService.calculatePoints(49.99));
        }

        @Test
        void exactlyFifty_returnsZero() {
            assertEquals(0, rewardsService.calculatePoints(50.0));
        }

        @Test
        void fiftyOne_returnsOne() {
            assertEquals(1, rewardsService.calculatePoints(51.0));
        }

        @Test
        void seventyFive_returns25() {
            assertEquals(25, rewardsService.calculatePoints(75.0));
        }

        @Test
        void ninetyNinePointNineNine_returns49() {
            assertEquals(49, rewardsService.calculatePoints(99.99));
        }

        @Test
        void exactlyOneHundred_returns50() {
            // Critical boundary — many implementations fail here
            assertEquals(50, rewardsService.calculatePoints(100.0));
        }

        @Test
        void oneHundredOne_returns52() {
            assertEquals(52, rewardsService.calculatePoints(101.0));
        }

        @Test
        void oneHundredTwenty_returns90() {
            // Example from problem statement
            assertEquals(90, rewardsService.calculatePoints(120.0));
        }

        @Test
        void twoHundred_returns250() {
            assertEquals(250, rewardsService.calculatePoints(200.0));
        }

        @Test
        void centsAreFloored_noHalfPointsBug() {
            assertEquals(90, rewardsService.calculatePoints(120.99));
        }
    }

    // =========================================================
    // Single customer reward tests
    // =========================================================

    @Nested
    @DisplayName("getRewardsForCustomer — monthly aggregation")
    class SingleCustomerRewardTests {

        @Test
        void noTransactions_returnsEmptyRewardSummary() {
            when(transactionRepository.findByCustomerId("c1"))
                    .thenReturn(List.of());

            RewardSummary summary =
                    rewardsService.getRewardsForCustomer("c1");

            assertNotNull(summary);
            assertTrue(summary.monthlyRewards().isEmpty());
            assertEquals(0, summary.totalPoints());
        }

        @Test
        void singleMonthTransactions_calculatesCorrectly() {
            List<Transaction> transactions = List.of(
                    new Transaction("t1", "c1", 120.0,
                            LocalDate.of(2026, 2, 1)), // 90
                    new Transaction("t2", "c1", 75.0,
                            LocalDate.of(2026, 2, 10)) // 25
            );

            when(transactionRepository.findByCustomerId("c1"))
                    .thenReturn(transactions);

            RewardSummary summary =
                    rewardsService.getRewardsForCustomer("c1");

            assertEquals(1, summary.monthlyRewards().size());
            assertEquals(115, summary.totalPoints());
        }

        @Test
        void multipleMonthTransactions_groupedCorrectly() {
            List<Transaction> transactions = List.of(
                    new Transaction("t1", "c1", 120.0,
                            LocalDate.of(2026, 1, 5)),  // 90
                    new Transaction("t2", "c1", 200.0,
                            LocalDate.of(2026, 2, 5))   // 250
            );

            when(transactionRepository.findByCustomerId("c1"))
                    .thenReturn(transactions);

            RewardSummary summary =
                    rewardsService.getRewardsForCustomer("c1");

            assertEquals(2, summary.monthlyRewards().size());
            assertEquals(340, summary.totalPoints());
        }
    }


    @Nested
    @DisplayName("getRewardsForAllCustomers — multi-customer")
    class AllCustomersRewardTests {

        @Test
        void noCustomers_returnsEmptyList() {
            when(customerRepository.findAll()).thenReturn(List.of());

            List<CustomerRewards> rewards =
                    rewardsService.getRewardsForAllCustomers("customerId,asc");

            assertTrue(rewards.isEmpty());
        }

        @Test
        void multipleCustomers_returnSeparateRewards() {
            when(customerRepository.findAll()).thenReturn(List.of(
                    new Customer("c1", "David"),
                    new Customer("c2", "Alex")
            ));

            when(transactionRepository.findByCustomerId("c1"))
                    .thenReturn(List.of(
                            new Transaction("t1", "c1", 120.0,
                                    LocalDate.of(2026, 2, 1)) // 90
                    ));

            when(transactionRepository.findByCustomerId("c2"))
                    .thenReturn(List.of(
                            new Transaction("t2", "c2", 200.0,
                                    LocalDate.of(2026, 2, 1)) // 250
                    ));

            List<CustomerRewards> rewards =
                    rewardsService.getRewardsForAllCustomers("totalPoints,desc");

            assertEquals(2, rewards.size());
            assertEquals("c2", rewards.get(0).customerId()); // highest points first
            assertEquals(250, rewards.get(0).rewards().totalPoints());
        }
    }
}
