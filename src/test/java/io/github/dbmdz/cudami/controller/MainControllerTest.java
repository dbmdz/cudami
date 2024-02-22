package io.github.dbmdz.cudami.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.dbmdz.cudami.business.api.service.security.UserService;
import io.github.dbmdz.cudami.propertyeditor.RoleEditor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MainController.class)
public class MainControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private RoleEditor roleEditor;
  @MockBean private UserDetailsService userDetailsService;
  @MockBean private UserService userService;

  @Test
  public void testNoAdminUserExists() throws Exception {
    this.mockMvc
        .perform(get("/"))
        .andDo(print())
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost/login"));
  }
}
