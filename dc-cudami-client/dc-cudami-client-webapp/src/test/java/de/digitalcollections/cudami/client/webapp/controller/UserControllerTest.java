package de.digitalcollections.cudami.client.webapp.controller;

import de.digitalcollections.cudami.client.Application;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
 */
// Don’t forget to also add @RunWith(SpringRunner.class) to your test, otherwise the annotations will be ignored:
@RunWith(SpringRunner.class)
// annotation which can be used as an alternative to the standard spring-test @ContextConfiguration annotation when you need Spring Boot features:
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// RANDOM_PORT — Loads an EmbeddedWebApplicationContext and provides a real servlet environment. Embedded servlet containers are started and listening on a random port
// SpringBootTest registers a TestRestTemplate bean for use in web tests that are using a fully running container.

//@ComponentScan(excludeFilters = {
//  @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SpringConfigBackend.class, SpringConfigBusiness.class})
//        ,
//  @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.digitalcollections.cudami.client.business.*.*.*ServiceImpl")
//})
// Fixed: adding @Primary to mock components !

@TestPropertySource(properties = {"management.port=0"})
// using @ContextConfiguration(classes=…​) in order to specify which Spring @Configuration to load
//@ContextConfiguration(classes = {SpringConfigWeb.class, SpringConfigSecurity.class, SpringConfigBusinessForTest.class, SpringConfigBackendForTest.class})
public class UserControllerTest {

  // The @LocalServerPort annotation can be used to inject the actual port used into your test.
  // For convenience, tests that need to make REST calls to the started server can
  // additionally @Autowire a TestRestTemplate which will resolve relative links to the running server.
  @LocalServerPort
  private int port;

  @Value("${local.management.port}")
  private int mgt;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Test
  public void resourceRequest() throws Exception {
    String body = this.testRestTemplate.getForObject("/css/main.css", String.class);
    Assert.assertTrue(body.contains("background"));
  }

//  @Before
//  public void setUp() {
//    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
//            .addFilter(springSecurityFilterChain)
//            .build();
//  }
  @After
  public void tearDown() {
  }

//  @Test
//  public void testApiCall() throws Exception {
//    mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
//            .andExpect(status().isOk())
//            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"));
////                .andExpect(redirectedUrl("/index.htm"));
//  }
}
