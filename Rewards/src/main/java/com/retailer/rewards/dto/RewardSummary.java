package com.retailer.rewards.dto;

import java.util.List;

public record RewardSummary(List<MonthlyReward> monthlyRewards, int totalPoints) {}