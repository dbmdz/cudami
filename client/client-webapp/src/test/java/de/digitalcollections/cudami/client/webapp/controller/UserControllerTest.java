package de.digitalcollections.cudami.client.webapp.controller;

import de.digitalcollections.cudami.config.SpringConfigBackendForTest;
import de.digitalcollections.cudami.config.SpringConfigBusinessForTest;
import de.digitalcollections.cudami.config.SpringConfigSecurity;
import de.digitalcollections.cudami.config.SpringConfigWeb;
import javax.annotation.Resource;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * unit testing PageController
 *
 * see also: http://zjhzxhz.com/2014/05/unit-testing-of-spring-mvc-controllers/,
 * http://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-security/
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringConfigWeb.class, SpringConfigSecurity.class, SpringConfigBusinessForTest.class,
  SpringConfigBackendForTest.class})
public class UserControllerTest {

  private MockMvc mockMvc;

  @Resource
  private FilterChainProxy springSecurityFilterChain;
  @Autowired
  private WebApplicationContext wac;

  @Test
  public void resourceRequest() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get("/css/main.css"))
            .andDo(MockMvcResultHandlers.print()) // print the request/response in the console
            //                .andExpect(MockMvcResultMatchers.content().contentType("text/css"))
            .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("background")));
  }

  @Before
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
            .addFilter(springSecurityFilterChain)
            .build();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testApiCall() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"));
//                .andExpect(redirectedUrl("/index.htm"));
  }
}
