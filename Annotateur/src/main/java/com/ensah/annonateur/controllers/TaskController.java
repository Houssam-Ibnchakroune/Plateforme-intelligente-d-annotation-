/* TaskController.java */
package com.ensah.annonateur.controllers;

import com.ensah.annonateur.models.Annotator;
import com.ensah.annonateur.repositories.UserRepository;
import com.ensah.annonateur.services.AssignmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskController {

    private final AssignmentService assignmentService;
    private final UserRepository userRepo;
    public TaskController(AssignmentService as, UserRepository ur){
        this.assignmentService = as;
        this.userRepo = ur;
    }


    @GetMapping("/tasks")
    public String myTasks(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                          Model m) {

        Annotator me = (Annotator) userRepo.findByUsername(principal.getUsername())
                .orElseThrow();                     // jamais null

        m.addAttribute("rows", assignmentService.taskRowsFor(me));
        return "tasks/list";
    }


}
