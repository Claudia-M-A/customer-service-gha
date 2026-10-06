package com.example.customerservice.service;

import com.example.customerservice.dto.CustomerRequest;
import com.example.customerservice.dto.CustomerResponse;
import com.example.customerservice.entity.Customer;
import com.example.customerservice.exception.CustomerNotFoundException;
import com.example.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest request;

    @BeforeEach
    void setUp() {
        request = new CustomerRequest("Ada", "Lovelace", "ada@example.com");
    }

    @Test
    void createSavesCustomerAndReturnsResponseDto() {
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.create(request);

        assertEquals("Ada", response.firstName());
        assertEquals("Lovelace", response.lastName());
        assertEquals("ada@example.com", response.email());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("Ada", customerCaptor.getValue().getFirstName());
        assertEquals("Lovelace", customerCaptor.getValue().getLastName());
        assertEquals("ada@example.com", customerCaptor.getValue().getEmail());
    }

    @Test
    void findByIdReturnsResponseWhenCustomerExists() {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com");
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.findById(42L);

        assertEquals("Ada", response.firstName());
        assertEquals("Lovelace", response.lastName());
        assertEquals("ada@example.com", response.email());
        verify(customerRepository).findById(42L);
    }

    @Test
    void findByIdThrowsCustomerNotFoundWhenCustomerDoesNotExist() {
        when(customerRepository.findById(42L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.findById(42L));

        assertEquals("Customer with id 42 was not found", exception.getMessage());
    }

    @Test
    void updateChangesCustomerAndReturnsUpdatedResponse() {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com");
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CustomerRequest updatedRequest = new CustomerRequest("Augusta", "King", "augusta@example.com");

        CustomerResponse response = customerService.update(42L, updatedRequest);

        assertEquals("Augusta", response.firstName());
        assertEquals("King", response.lastName());
        assertEquals("augusta@example.com", response.email());
        verify(customerRepository).save(customer);
    }

    @Test
    void deleteRemovesCustomerWhenCustomerExists() {
        Customer customer = new Customer("Ada", "Lovelace", "ada@example.com");
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));

        customerService.delete(42L);

        verify(customerRepository).delete(customer);
    }
}
