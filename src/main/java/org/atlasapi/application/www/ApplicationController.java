package org.atlasapi.application.www;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ApplicationController {
    
    @RequestMapping("/applications")
    public String applications(Map<String, Object> model) {
        
        return "applications/index";
    }
}
