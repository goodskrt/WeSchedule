package com.iusjc.weschedule.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import com.iusjc.weschedule.repositories.ReservationRepository;
import java.io.StringWriter;
import java.io.PrintWriter;

@Controller
public class TestController {

    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/test-render")
    @ResponseBody
    public String testRender() {
        try {
            Context context = new Context();
            context.setVariable("reservations", reservationRepository.findAllByOrderByDateCreationDesc());
            return templateEngine.process("admin/reservations-demandes", context);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }
}
