package de.digitalcollections.cudami.server.interceptor;

import java.lang.reflect.Field;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsondoc.spring.boot.starter.JSONDocProperties;
import org.jsondoc.springmvc.controller.JSONDocController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JsondocInterceptor implements HandlerInterceptor {

  @Autowired private JSONDocProperties jsonDocProperties;

  @Autowired private JSONDocController jsonDocController;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    final URL baseURLFromRequest =
        new URL(
            request.getScheme(),
            request.getServerName(),
            request.getServerPort(),
            request.getContextPath());
    jsonDocProperties.setBasePath(baseURLFromRequest.toExternalForm());
    final Field basePath = jsonDocController.getClass().getDeclaredField("basePath");
    basePath.setAccessible(true);
    basePath.set(jsonDocController, jsonDocProperties.getBasePath());
    return true;
  }
}
