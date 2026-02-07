package com.example.rewards.service;

import com.example.rewards.model.MonthlyReward;
import com.example.rewards.model.RewardSummary;
import com.example.rewards.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    public int calculatePoints(double amount) {
        int dollars = (int) Math.floor(amount);

        int over100 = Math.max(0, dollars - 100);
        int between50And100 = Math.max(0, Math.min(dollars, 100) - 50);

        return (over100 * 2) + between50And100;
    }

    public RewardSummary summarizeCustomerRewards(List<Transaction> txns) {
        Map<YearMonth, Integer> pointsByMonth = txns.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.date()),
                        Collectors.summingInt(t -> calculatePoints(t.amount()))
                ));

        List<MonthlyReward> monthly = pointsByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyReward(e.getKey().toString(), e.getValue()))
                .toList();

        int total = monthly.stream().mapToInt(MonthlyReward::points).sum();
        return new RewardSummary(monthly, total);
    }

    public Mono<RewardSummary> summarizeLast3Months(Flux<Transaction> txns, YearMonth now) {
        YearMonth start = now.minusMonths(2);

        return txns.filter(t -> {
                    YearMonth ym = YearMonth.from(t.date());
                    return !ym.isBefore(start) && !ym.isAfter(now);
                })
                .collectList()
                .map(this::summarizeCustomerRewards);
    }
}