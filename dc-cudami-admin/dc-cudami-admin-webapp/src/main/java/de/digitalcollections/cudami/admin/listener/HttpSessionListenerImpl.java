package de.digitalcollections.cudami.admin.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Session configuration. */
@WebListener
public class HttpSessionListenerImpl implements HttpSessionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpSessionListenerImpl.class);

  private void setSessionTimeout(HttpSession session) {
    session.setMaxInactiveInterval(10 * 60 * 60); // seconds: 36000s = 10h
    // replaces web.xml config: <session-config> <session-timeout>600</session-timeout> <!--
    // minutes: 60 * 10 h --> </session-config>
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    LOGGER.info("==== Session " + session.getId() + " is created ====");
    setSessionTimeout(session);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    LOGGER.info("==== Session " + session.getId() + " is destroyed ====");
  }
}
