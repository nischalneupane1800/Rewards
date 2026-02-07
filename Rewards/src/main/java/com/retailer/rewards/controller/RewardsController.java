package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewards;
import com.retailer.rewards.dto.RewardSummary;
import com.retailer.rewards.service.RewardsService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/rewards")
@Validated
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }


    @GetMapping("/{customerId}")
    public RewardSummary rewardsForCustomer(@PathVariable String customerId) {

        if (customerId == null || customerId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "customerId must be provided");
        }

        try {
            return rewardsService.getRewardsForCustomer(customerId);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Customer not found");
        }
    }

    @GetMapping
    public List<CustomerRewards> rewardsForAllCustomers(
            @RequestParam(name = "sort", defaultValue = "customerId,asc") String sort
    ) {
        return rewardsService.getRewardsForAllCustomers(sort);
    }
}
