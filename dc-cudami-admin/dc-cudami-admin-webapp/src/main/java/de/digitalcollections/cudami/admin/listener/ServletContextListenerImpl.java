package de.digitalcollections.cudami.admin.listener;

import java.util.EnumSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

/**
 * An implementation of {@link ServletContextListener} servlet context lifecycle listener.
 */
@WebListener
public class ServletContextListenerImpl implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final ServletContext servletContext = sce.getServletContext();

    // Set the session tracking globally for this servlet context to Cookie. This will override web.xml session tracking.
    Set<SessionTrackingMode> modes = EnumSet.noneOf(SessionTrackingMode.class);
    modes.add(SessionTrackingMode.COOKIE);
    servletContext.setSessionTrackingModes(modes);

    SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
    sessionCookieConfig.setHttpOnly(true);

    // if in production (HTTPS) make session cookie secure
    String activeProfile = System.getProperty("spring.profiles.active", "PROD");
    if (activeProfile != null && "PROD".equals(activeProfile)) {
      sessionCookieConfig.setSecure(true);
    }
  }

}
