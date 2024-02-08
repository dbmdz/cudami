package de.digitalcollections.cudami.admin.interceptors;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.admin.business.api.service.security.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

/** Check if admin user exists before login (call to "/login") dialog. */
public class CreateAdminUserInterceptor implements HandlerInterceptor, MessageSourceAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateAdminUserInterceptor.class);

  private MessageSource messageSource;

  @Autowired private UserService service;

  @Override
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws ServiceException {
    LOGGER.info("checking if admin user exists...");
    boolean activeAdminUserExists = service.doesActiveAdminUserExist();
    if (!activeAdminUserExists) {
      request.setAttribute("createAdminUser", true);
    }
    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws IllegalStateException {
    final Object doCreateAdminUser = request.getAttribute("createAdminUser");
    if (doCreateAdminUser != null) {
      boolean createAdminUser = (boolean) doCreateAdminUser;
      if (createAdminUser) {
        modelAndView.setView(new RedirectView("/setup/adminUser", true));
        String message =
            messageSource.getMessage(
                "msg.create_a_new_admin_user", null, LocaleContextHolder.getLocale());

        FlashMap flashMap = new FlashMap();
        flashMap.put("info_message", message);
        FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
        if (flashMapManager == null) {
          throw new IllegalStateException(
              "FlashMapManager not found despite output FlashMap having been set");
        }
        flashMapManager.saveOutputFlashMap(flashMap, request, response);

        //        modelAndView.addObject("info_message", message);
        LOGGER.info("Admin user does not exist. Create a new administrator user.");
      }
    }
  }
}
