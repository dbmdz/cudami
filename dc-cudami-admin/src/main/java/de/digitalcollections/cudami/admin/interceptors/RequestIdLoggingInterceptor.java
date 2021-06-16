package de.digitalcollections.cudami.admin.interceptors;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestIdLoggingInterceptor implements HandlerInterceptor {
  @Override
  @SuppressFBWarnings(value = {"HRS_REQUEST_PARAMETER_TO_HTTP_HEADER"})
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler)
      throws Exception {
    String requestId = request.getHeader("X-Request-Id");
    if (!Strings.isNullOrEmpty(requestId)) {
      MDC.put("request_id", requestId);
      response.setHeader("X-Request-Id", requestId);
    }
    return true;
  }
}
