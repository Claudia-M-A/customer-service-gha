package com.example.customerservice.controller;

import com.example.customerservice.config.SecurityConfiguration;
import com.example.customerservice.dto.CustomerResponse;
import com.example.customerservice.exception.CustomerNotFoundException;
import com.example.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {
        "spring.security.user.name=test-user",
        "spring.security.user.password=test-password"
})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void customerEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/customers/7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postCreatesCustomerAndReturnsCreatedLocation() throws Exception {
        when(customerService.create(any())).thenReturn(
                new CustomerResponse(7L, "Ada", "Lovelace", "ada@example.com"));

        mockMvc.perform(post("/api/v1/customers")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/customers/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void getByIdReturnsCustomer() throws Exception {
        when(customerService.findById(7L)).thenReturn(
                new CustomerResponse(7L, "Ada", "Lovelace", "ada@example.com"));

        mockMvc.perform(get("/api/v1/customers/{id}", 7L)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void postWithInvalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Lovelace","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void getByIdWhenCustomerDoesNotExistReturnsNotFound() throws Exception {
        when(customerService.findById(eq(404L))).thenThrow(new CustomerNotFoundException(404L));

        mockMvc.perform(get("/api/v1/customers/{id}", 404L)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Customer with id 404 was not found"));
    }

    private static String basicAuth() {
        String credentials = "test-user:test-password";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
