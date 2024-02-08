package de.digitalcollections.cudami.admin.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.MDC;

/**
 * Add session id of request to logging MDC (Mapped Diagnostic Context). Example:
 *
 * <pre>{@code
 * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 * <layout>
 *   <Pattern>%X{sessionID} - %m%n</Pattern>
 * </layout>
 * </appender>
 * }</pre>
 */
public class LogSessionIdFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpSession session = httpRequest.getSession();
    if (session != null) {
      MDC.put("sessionID", session.getId());
    }

    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void destroy() {}
}
