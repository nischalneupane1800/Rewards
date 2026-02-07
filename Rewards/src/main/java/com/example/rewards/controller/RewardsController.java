package com.example.rewards.controller;

import com.example.rewards.model.CustomerRewards;
import com.example.rewards.repository.DataRepository;
import com.example.rewards.service.RewardsService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.Locale;

@RestController
@RequestMapping("/rewards")
public class RewardsController {

    private final DataRepository repo;
    private final RewardsService service;

    public RewardsController(DataRepository repo, RewardsService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping("/{customerId}")
    public reactor.core.publisher.Mono<com.example.rewards.model.RewardSummary> rewardsForCustomer(
            @PathVariable String customerId
    ) {
        YearMonth now = YearMonth.now();
        return service.summarizeLast3Months(repo.transactionsForCustomer(customerId), now);
    }

    @GetMapping
    public Flux<CustomerRewards> rewardsForAllCustomers(
            @RequestParam(name = "sort", required = false, defaultValue = "customerId,asc") String sort
    ) {
        YearMonth now = YearMonth.now();

        return repo.customers()
                .flatMap(c ->
                        service.summarizeLast3Months(repo.transactionsForCustomer(c.id()), now)
                                .map(summary -> new CustomerRewards(c.id(), summary))
                )
                .sort(buildCustomerRewardsComparator(sort));
    }

    private Comparator<CustomerRewards> buildCustomerRewardsComparator(String sort) {
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim().toLowerCase(Locale.ROOT);
        String dir = (parts.length == 2 ? parts[1].trim().toLowerCase(Locale.ROOT) : "asc");

        Comparator<CustomerRewards> cmp = switch (field) {
            case "customerid", "customer" -> Comparator.comparing(CustomerRewards::customerId);
            case "totalpoints" -> Comparator.comparingInt(cr -> cr.rewards().totalPoints());
            default -> throw new IllegalArgumentException(
                    "Unsupported sort field='" + field + "'. Use customerId|totalPoints."
            );
        };

        return "desc".equals(dir) ? cmp.reversed() : cmp;
    }
}