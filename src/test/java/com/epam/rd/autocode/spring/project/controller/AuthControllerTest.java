package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private ClientService clientService;
    private PasswordEncoder passwordEncoder;
    private EmployeeService employeeService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        clientService = mock(ClientService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        employeeService = mock(EmployeeService.class);
        controller = new AuthController(clientService, passwordEncoder, employeeService);
    }

    @Test
    void loginPage_setsAllMessages_whenParamsPresent() {
        Model model = new ExtendedModelMap();

        String view = controller.loginPage("1", "1", "1", model);

        assertThat(view).isEqualTo("login");
        assertThat(model.getAttribute("error")).isEqualTo("Invalid email or password");
        assertThat(model.getAttribute("success")).isEqualTo("Registration successful! Please login.");
        assertThat(model.getAttribute("info")).isEqualTo("You have been logged out successfully.");
    }

    @Test
    void loginPage_noParams_returnsLogin() {
        Model model = new ExtendedModelMap();
        String view = controller.loginPage(null, null, null, model);
        assertThat(view).isEqualTo("login");
        assertThat(model.getAttribute("error")).isNull();
        assertThat(model.getAttribute("success")).isNull();
        assertThat(model.getAttribute("info")).isNull();
    }

    @Test
    void showRegisterForm_initializesDto_andSetsExistsErrorWhenRequested() {
        Model model = new ExtendedModelMap();

        String viewExists = controller.showRegisterForm("exists", model);
        assertThat(viewExists).isEqualTo("register");
        assertThat(model.getAttribute("clientDTO")).isInstanceOf(ClientDTO.class);
        assertThat(model.getAttribute("error")).isEqualTo("This email is already registered. Please login or use another email.");

        Model model2 = new ExtendedModelMap();
        String viewNoErr = controller.showRegisterForm(null, model2);
        assertThat(viewNoErr).isEqualTo("register");
        assertThat(model2.getAttribute("error")).isNull();
    }

    @Test
    void register_whenValidationErrors_returnsRegister() {
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);

        String view = controller.register(new ClientDTO(), br);

        assertThat(view).isEqualTo("register");
        verifyNoInteractions(clientService);
    }

    @Test
    void register_success_nullBalance_isZero_andPasswordEncoded_andRedirects() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("a@b.c");
        dto.setPassword("raw");
        dto.setBalance(null);

        when(passwordEncoder.encode("raw")).thenReturn("ENC");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        String view = controller.register(dto, br);

        assertThat(view).isEqualTo("redirect:/login?registered");
        assertThat(dto.getPassword()).isEqualTo("ENC");
        assertThat(dto.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(clientService).addClient(dto);
    }

    @Test
    void register_success_existingBalance_preserved_andRedirects() {
        ClientDTO dto = new ClientDTO();
        dto.setPassword("pwd");
        dto.setBalance(new BigDecimal("123.45"));

        when(passwordEncoder.encode("pwd")).thenReturn("ENC2");
        BindingResult br = mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);

        String view = controller.register(dto, br);

        assertThat(view).isEqualTo("redirect:/login?registered");
        assertThat(dto.getPassword()).isEqualTo("ENC2");
        assertThat(dto.getBalance()).isEqualByComparingTo(new BigDecimal("123.45"));
        verify(clientService).addClient(dto);
    }

    @Test
    void index_unauthenticated_returnsIndexWithoutDisplayName() {
        Model model = new ExtendedModelMap();
        String view = controller.index(null, model);
        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("displayName")).isNull();
    }

    @Test
    void index_roleClient_setsClientName() {
        String email = "client@ex.com";
        ClientDTO client = new ClientDTO();
        client.setName("Client Name");
        when(clientService.getClientByEmail(email)).thenReturn(client);

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(email, "x",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        Model model = new ExtendedModelMap();
        String view = controller.index(auth, model);

        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("displayName")).isEqualTo("Client Name");
    }

    @Test
    void index_roleEmployee_setsEmployeeName() {
        String email = "emp@ex.com";
        var empDto = new com.epam.rd.autocode.spring.project.dto.EmployeeDTO();
        empDto.setName("Emp Name");
        when(employeeService.getEmployeeByEmail(email)).thenReturn(empDto);

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(email, "x",
                        List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));

        Model model = new ExtendedModelMap();
        String view = controller.index(auth, model);

        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("displayName")).isEqualTo("Emp Name");
    }
}
