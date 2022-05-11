package de.digitalcollections.cudami.server.interceptors;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestIdLoggingInterceptor implements HandlerInterceptor {
  /** Clear MDC to avoid data leaking between two requests handled by the same thread. */
  @Override
  public void postHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler,
      ModelAndView modelAndView) {
    MDC.clear();
  }

  /**
   * Register the request identifier (if received from client/frontend server) in the logging
   * context.
   */
  @Override
  @SuppressFBWarnings(value = {"HRS_REQUEST_PARAMETER_TO_HTTP_HEADER"})
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    String requestId = request.getHeader("X-Request-Id");
    if (!Strings.isNullOrEmpty(requestId)) {
      MDC.put("request_id", requestId);
      response.setHeader("X-Request-Id", requestId);
    }
    return true;
  }
}
