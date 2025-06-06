package io.github.dbmdz.cudami.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
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
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    String requestId = request.getHeader("X-Request-Id");
    if (StringUtils.hasText(requestId)) {
      MDC.put("request_id", requestId);
      response.setHeader("X-Request-Id", requestId);
    }
    return true;
  }
}
