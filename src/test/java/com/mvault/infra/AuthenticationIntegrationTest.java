package com.mvault.infra;

import com.mvault.support.IsolatedDatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IsolatedDatabaseTest
class AuthenticationIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy springSecurityFilterChain;
    private MockMvc mvc;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @Test void loginInexistenteRetorna401() throws Exception {
        mvc.perform(post("/login").contentType("application/json")
                .content("{\"login\":\"usuario-ausente\",\"senha\":\"senha-de-teste\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void tokenMalformadoRetorna401() throws Exception {
        mvc.perform(get("/produtos").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }
}
