package com.ensah.annonateur.controllers;

import com.ensah.annonateur.models.*;
import com.ensah.annonateur.repositories.UserRepository;
import com.ensah.annonateur.services.AnnotationService;
import com.ensah.annonateur.services.DatasetService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/annotate")
public class AnnotationController {

    private final AnnotationService annotationService;
    private final DatasetService    datasetService;
    private final UserRepository    userRepo;

    public AnnotationController(AnnotationService annotationService,
                                DatasetService datasetService,
                                UserRepository userRepo) {
        this.annotationService = annotationService;
        this.datasetService    = datasetService;
        this.userRepo          = userRepo;
    }

    /* ------------------------------------------------------------------
       GET  /annotate?dataset=ID  -> interface d’annotation
       ------------------------------------------------------------------ */
    @GetMapping
    public String form(@RequestParam Long dataset,
                       @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                       Model m) {

        Dataset   ds  = datasetService.get(dataset);
        Annotator ann = (Annotator) userRepo.findByUsername(principal.getUsername())
                .orElseThrow();

        m.addAttribute("pairs",  annotationService.unannotatedPairs(ds, ann));
        m.addAttribute("labels", List.of(ds.getClasses().split(";")));
        m.addAttribute("datasetId", ds.getId());           // champ caché

        return "annotation/interface";
    }

    /* ------------------------------------------------------------------
       POST /annotate  -> enregistre l’annotation et passe à la suivante
       ------------------------------------------------------------------ */
    @PostMapping
    public String save(@RequestParam Long datasetId,
                       @RequestParam Long pairId,
                       @RequestParam String label,
                       @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        Annotator ann = (Annotator) userRepo.findByUsername(principal.getUsername())
                .orElseThrow();
        Dataset ds = datasetService.get(datasetId);

        annotationService.save(ds, pairId, ann, label);

        return "redirect:/annotate?dataset=" + datasetId;
    }
}
