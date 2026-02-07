package com.example.rewards.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.rewards.model.RewardSummary;
import com.example.rewards.model.Transaction;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

class RewardsServiceTest {

    private final RewardsService service = new RewardsService();

    @Test
    void calculatePoints_boundariesAndExamples() {
        // Below 50
        assertEquals(0, service.calculatePoints(0));
        assertEquals(0, service.calculatePoints(49.99));

        // Boundary at 50 (over $50 earns points, so 50 itself earns 0)
        assertEquals(0, service.calculatePoints(50.00));
        assertEquals(1, service.calculatePoints(51.00));

        // Boundary at 100 (100 earns 50 points from the 50-100 band)
        assertEquals(50, service.calculatePoints(100.00));
        assertEquals(52, service.calculatePoints(101.00)); // 50 + (2*1)

        // Example in spec
        assertEquals(90, service.calculatePoints(120.00));

        // Larger amount
        assertEquals(250, service.calculatePoints(200.00)); // 50 + 2*100
    }

    @Test
    void summarizeCustomerRewards_groupsByMonth_sumsAndSorts() {
        // Points:
        // 2026-01: $120 => 90, $75 => 25  => 115
        // 2026-02: $100 => 50            => 50
        List<Transaction> txns = List.of(
                new Transaction("t1", "c1", 120.0, LocalDate.of(2026, 1, 10)),
                new Transaction("t2", "c1", 75.0,  LocalDate.of(2026, 1, 20)),
                new Transaction("t3", "c1", 100.0, LocalDate.of(2026, 2, 5))
        );

        RewardSummary summary = service.summarizeCustomerRewards(txns);

        assertNotNull(summary);
        assertEquals(2, summary.monthlyRewards().size(), "Should have 2 distinct months");
        assertEquals(165, summary.totalPoints(), "Total should equal sum of monthly points");

        // Sorted by YearMonth: 2026-01 then 2026-02
        assertEquals("2026-01", summary.monthlyRewards().get(0).month());
        assertEquals(115, summary.monthlyRewards().get(0).points());

        assertEquals("2026-02", summary.monthlyRewards().get(1).month());
        assertEquals(50, summary.monthlyRewards().get(1).points());
    }

    @Test
    void summarizeLast3Months_filtersCorrectly() {
        YearMonth now = YearMonth.of(2026, 2);

        List<Transaction> txns = List.of(
                new Transaction("t1", "c1", 120, LocalDate.of(2026, 2, 1)),
                new Transaction("t2", "c1", 100, LocalDate.of(2026, 1, 1)),
                new Transaction("t3", "c1", 75,  LocalDate.of(2025, 12, 1)),
                new Transaction("t4", "c1", 200, LocalDate.of(2025, 11, 30)) // excluded
        );

        StepVerifier.create(service.summarizeLast3Months(Flux.fromIterable(txns), now))
                .assertNext(summary -> {
                    // months included: 2025-12, 2026-01, 2026-02
                    org.junit.jupiter.api.Assertions.assertEquals(3, summary.monthlyRewards().size());
                    org.junit.jupiter.api.Assertions.assertTrue(summary.totalPoints() > 0);
                })
                .verifyComplete();
    }
}