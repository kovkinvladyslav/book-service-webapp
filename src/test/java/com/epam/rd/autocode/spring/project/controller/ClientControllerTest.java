package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ClientControllerTest {

    private PaymentService paymentService;
    private ClientService clientService;
    private ClientController controller;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        clientService = mock(ClientService.class);
        controller = new ClientController(paymentService, clientService);
    }

    private TestingAuthenticationToken auth(String email) {
        return new TestingAuthenticationToken(
                email, "x", List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    @Test
    void viewProfile_populatesClientUpdateDTO_andBalance() {
        ClientDTO client = new ClientDTO();
        client.setEmail("c@ex.com");
        client.setName("Client");
        client.setBalance(new BigDecimal("12.34"));
        when(clientService.getClientByEmail("c@ex.com")).thenReturn(client);

        Model model = new ExtendedModelMap();
        String view = controller.viewProfile(model, auth("c@ex.com"));

        assertThat(view).isEqualTo("client/profile");
        Object dtoObj = model.getAttribute("client");
        assertThat(dtoObj).isInstanceOf(ClientUpdateDTO.class);
        ClientUpdateDTO dto = (ClientUpdateDTO) dtoObj;
        assertThat(dto.getEmail()).isEqualTo("c@ex.com");
        assertThat(dto.getName()).isEqualTo("Client");
        assertThat(model.getAttribute("balance")).isEqualTo(new BigDecimal("12.34"));
    }

    @Test
    void updateProfile_whenBindingErrors_reloadsBalance_andReturnsView() {
        ClientDTO current = new ClientDTO();
        current.setBalance(new BigDecimal("99.99"));
        when(clientService.getClientByEmail("c@ex.com")).thenReturn(current);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        Model model = new ExtendedModelMap();
        String view = controller.updateProfile(new ClientUpdateDTO(), br, auth("c@ex.com"), model);

        assertThat(view).isEqualTo("client/profile");
        assertThat(model.getAttribute("balance")).isEqualTo(new BigDecimal("99.99"));
        verify(clientService, never()).updateClientByEmail(anyString(), any());
    }

    @Test
    void updateProfile_success_updates_fetches_again_setsSuccessMessage() {
        ClientUpdateDTO incoming = new ClientUpdateDTO();
        incoming.setName("New Name");
        incoming.setEmail("ignored@ex.com"); // controller uses auth name as key

        ClientDTO after = new ClientDTO();
        after.setName("New Name");
        after.setEmail("c@ex.com");
        after.setBalance(new BigDecimal("55.00"));

        when(clientService.getClientByEmail("c@ex.com")).thenReturn(after);

        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.updateProfile(incoming, br, auth("c@ex.com"), model);

        assertThat(view).isEqualTo("client/profile");
        verify(clientService).updateClientByEmail("c@ex.com", incoming);
        verify(clientService, atLeastOnce()).getClientByEmail("c@ex.com");

        ClientUpdateDTO returned = (ClientUpdateDTO) model.getAttribute("client");
        assertThat(returned.getName()).isEqualTo("New Name");
        assertThat(returned.getEmail()).isEqualTo("c@ex.com");
        assertThat(model.getAttribute("balance")).isEqualTo(new BigDecimal("55.00"));
        assertThat(model.getAttribute("successMessage")).isEqualTo("Profile updated successfully!");
    }

    @Test
    void showBalance_addsClientAndReturnsView() {
        ClientDTO client = new ClientDTO();
        when(clientService.getClientByEmail("c@ex.com")).thenReturn(client);

        Model model = new ExtendedModelMap();
        String view = controller.showBalance(auth("c@ex.com"), model);

        assertThat(view).isEqualTo("client/balance");
        assertThat(model.getAttribute("client")).isEqualTo(client);
    }

    @Test
    void depositFunds_callsPaymentService_andRedirects() {
        String view = controller.depositFunds(new BigDecimal("10.00"), auth("c@ex.com"));
        assertThat(view).isEqualTo("redirect:/client/balance");
        verify(paymentService).deposit("c@ex.com", new BigDecimal("10.00"));
    }
}
