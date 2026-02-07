package com.retailer.rewards.controller;

import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionsController.class)
class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void getAllTransactions_returnsList() throws Exception {
        Mockito.when(transactionService.getAllTransactions()).thenReturn(List.of(
                new Transaction("t1", "c1", 120.0, LocalDate.of(2026, 2, 1)),
                new Transaction("t2", "c2", 75.0,  LocalDate.of(2026, 1, 10))
        ));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("t1"))
                .andExpect(jsonPath("$[0].customerId").value("c1"))
                .andExpect(jsonPath("$[0].amount").value(120.0))
                .andExpect(jsonPath("$[0].date").value("2026-02-01"))
                .andExpect(jsonPath("$[1].id").value("t2"))
                .andExpect(jsonPath("$[1].customerId").value("c2"));
    }

    @Test
    void getTransactionsByCustomerId_returnsOnlyThatCustomer() throws Exception {
        Mockito.when(transactionService.getTransactionsForCustomer("c1")).thenReturn(List.of(
                new Transaction("t1", "c1", 120.0, LocalDate.of(2026, 2, 1)),
                new Transaction("t3", "c1", 100.0, LocalDate.of(2026, 1, 1))
        ));

        mockMvc.perform(get("/transactions/c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("c1"))
                .andExpect(jsonPath("$[1].customerId").value("c1"));
    }

    @Test
    void getTransactionsByCustomerId_whenNone_returnsEmptyArray() throws Exception {
        Mockito.when(transactionService.getTransactionsForCustomer("none"))
                .thenReturn(List.of());

        mockMvc.perform(get("/transactions/none"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
