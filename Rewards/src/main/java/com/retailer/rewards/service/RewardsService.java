package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewards;
import com.retailer.rewards.dto.MonthlyReward;
import com.retailer.rewards.dto.RewardSummary;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    private final CustomerRepository customerRepo;
    private final TransactionRepository transactionRepo;
    private final Clock clock;

    public RewardsService(CustomerRepository customerRepo,
                          TransactionRepository transactionRepo,
                          Clock clock) {
        this.customerRepo = customerRepo;
        this.transactionRepo = transactionRepo;
        this.clock = clock;
    }

    public RewardSummary getRewardsForCustomer(String customerId) {
        List<Transaction> txns =
                transactionRepo.findByCustomerId(customerId);

        if (txns.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        return summarizeLast3Months(txns, currentYearMonth());
    }

    public List<CustomerRewards> getRewardsForAllCustomers(String sort) {
        return customerRepo.findAll().stream()
                .map(c -> new CustomerRewards(
                        c.getId(),
                        summarizeLast3Months(
                                transactionRepo.findByCustomerId(c.getId()),
                                currentYearMonth()
                        )
                ))
                .sorted(buildComparator(sort))
                .toList();
    }

    int calculatePoints(Double amount) {
        if (amount == null || amount < 50) {
            return 0;
        }

        int dollars = amount.intValue();

        int over100 = Math.max(0, dollars - 100);
        int between50And100 = Math.max(0, Math.min(dollars, 100) - 50);

        return (over100 * 2) + between50And100;
    }

    RewardSummary summarizeLast3Months(List<Transaction> txns, YearMonth now) {
        YearMonth start = now.minusMonths(2);

        Map<YearMonth, Integer> pointsByMonth =
                txns.stream()
                        .filter(t -> t.getDate() != null)
                        .filter(t -> {
                            YearMonth ym = YearMonth.from(t.getDate());
                            return !ym.isBefore(start) && !ym.isAfter(now);
                        })
                        .collect(Collectors.groupingBy(
                                t -> YearMonth.from(t.getDate()),
                                Collectors.summingInt(
                                        t -> calculatePoints(t.getAmount())
                                )
                        ));

        List<MonthlyReward> monthly =
                pointsByMonth.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> new MonthlyReward(
                                e.getKey().toString(),
                                e.getValue()
                        ))
                        .toList();

        int total = monthly.stream()
                .mapToInt(MonthlyReward::points)
                .sum();

        return new RewardSummary(monthly, total);
    }

    private YearMonth currentYearMonth() {
        return YearMonth.now(clock);
    }

    private Comparator<CustomerRewards> buildComparator(String sort) {
        if (sort == null || sort.isBlank()) {
            return Comparator.comparing(CustomerRewards::customerId);
        }

        String[] parts = sort.split(",", 2);
        String field = parts[0].toLowerCase();
        boolean desc = parts.length == 2 && "desc".equalsIgnoreCase(parts[1]);

        Comparator<CustomerRewards> cmp = switch (field) {
            case "totalpoints" -> Comparator.comparingInt(cr -> cr.rewards().totalPoints());
            case "customerid", "customer" -> Comparator.comparing(CustomerRewards::customerId);
            default -> Comparator.comparing(CustomerRewards::customerId);
        };

        return desc ? cmp.reversed() : cmp;
    }
}

