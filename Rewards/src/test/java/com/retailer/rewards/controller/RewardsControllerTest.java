package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewards;
import com.retailer.rewards.dto.MonthlyReward;
import com.retailer.rewards.dto.RewardSummary;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsService rewardsService;

    @Test
    void getRewardsForCustomer_returnsSummaryShape() throws Exception {
        RewardSummary summary = new RewardSummary(
                List.of(new MonthlyReward("2026-02", 90)),
                90
        );

        Mockito.when(rewardsService.getRewardsForCustomer("c1"))
                .thenReturn(summary);

        mockMvc.perform(get("/rewards/c1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthlyRewards").isArray())
                .andExpect(jsonPath("$.totalPoints").value(90));
    }

    @Test
    void getRewardsForAllCustomers_defaultSort() throws Exception {
        Mockito.when(rewardsService.getRewardsForAllCustomers("customerId,asc"))
                .thenReturn(List.of(
                        new CustomerRewards("c1",
                                new RewardSummary(List.of(), 10)),
                        new CustomerRewards("c2",
                                new RewardSummary(List.of(), 5))
                ));

        mockMvc.perform(get("/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("c1"))
                .andExpect(jsonPath("$[1].customerId").value("c2"));
    }

    @Test
    void getRewardsForAllCustomers_sortByTotalPointsDesc() throws Exception {
        Mockito.when(rewardsService.getRewardsForAllCustomers("totalPoints,desc"))
                .thenReturn(List.of(
                        new CustomerRewards("c2",
                                new RewardSummary(List.of(), 250)),
                        new CustomerRewards("c1",
                                new RewardSummary(List.of(), 25))
                ));

        mockMvc.perform(get("/rewards?sort=totalPoints,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("c2"))
                .andExpect(jsonPath("$[0].rewards.totalPoints").value(250));
    }
}
