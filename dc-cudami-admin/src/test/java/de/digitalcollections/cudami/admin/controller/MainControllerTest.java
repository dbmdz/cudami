package de.digitalcollections.cudami.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import de.digitalcollections.cudami.admin.propertyeditor.LocalizedStructuredContentEditor;
import de.digitalcollections.cudami.admin.propertyeditor.LocalizedTextEditor;
import de.digitalcollections.cudami.admin.propertyeditor.RoleEditor;
import de.digitalcollections.cudami.admin.propertyeditor.StructuredContentEditor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MainController.class)
public class MainControllerTest {

  @MockBean private LocalizedStructuredContentEditor localizedStructuredContentEditor;
  @MockBean private LocalizedTextEditor localizedTextEditor;
  @Autowired private MockMvc mockMvc;
  @MockBean private RoleEditor roleEditor;
  @MockBean private StructuredContentEditor structuredContentEditor;

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
