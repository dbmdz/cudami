package io.github.dbmdz.cudami.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
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
