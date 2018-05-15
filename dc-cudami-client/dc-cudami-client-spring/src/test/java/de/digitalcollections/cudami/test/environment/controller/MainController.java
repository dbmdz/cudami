package de.digitalcollections.cudami.test.environment.controller;

import de.digitalcollections.prosemirror.model.api.contentblocks.IFrame;
import de.digitalcollections.prosemirror.model.impl.contentblocks.IFrameImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

  @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
  public String printWelcome(Model model) {
    IFrame iframe = new IFrameImpl("http://www.test.de", "98%", "auto");
    model.addAttribute("block", iframe);
    return "test";
  }
}
