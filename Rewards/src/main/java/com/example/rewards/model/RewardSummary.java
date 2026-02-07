package com.example.rewards.model;

import java.util.List;

public record RewardSummary(List<MonthlyReward> monthlyRewards, int totalPoints) {}