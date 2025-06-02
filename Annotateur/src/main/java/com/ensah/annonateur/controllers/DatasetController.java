package com.ensah.annonateur.controllers;

import com.ensah.annonateur.models.Annotator;
import com.ensah.annonateur.models.Dataset;
import com.ensah.annonateur.models.TaskType;
import com.ensah.annonateur.repositories.TextPairRepository;
import com.ensah.annonateur.repositories.UserRepository;
import com.ensah.annonateur.services.AssignmentService;
import com.ensah.annonateur.services.DatasetService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/datasets")
public class DatasetController {

    private final DatasetService    datasetService;
    private final TextPairRepository textPairRepo;
    private final AssignmentService  assignmentService;
    private final UserRepository userRepo;

    /* -------- CONSTRUCTEUR -------- */
    public DatasetController(DatasetService ds, TextPairRepository tp,
                             AssignmentService as, UserRepository ur){
        this.datasetService = ds;
        this.textPairRepo   = tp;
        this.assignmentService = as;
        this.userRepo      = ur;
    }
    /* LISTE */
    @GetMapping
    public String list(Model m){
        m.addAttribute("datasets", datasetService.listAll());
        m.addAttribute("calc", datasetService);   // pour appeler completionPercent dans Thymeleaf
        return "datasets/list";
    }

    /* FORMULAIRE */
    @GetMapping("/new")
    public String form(Model m){
        m.addAttribute("taskTypes", TaskType.values());
        return "datasets/new";
    }

    /* CRÉATION */
    @PostMapping("/new")
    public String create(@RequestParam String name,
                         @RequestParam String description,
                         @RequestParam String classes,
                         @RequestParam TaskType taskType,
                         @RequestParam MultipartFile file) throws Exception {
        datasetService.create(name, description, classes, taskType, file);
        return "redirect:/admin/datasets";
    }
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model m) {

        Dataset ds = datasetService.get(id);
        m.addAttribute("dataset", ds);

        m.addAttribute("percent", datasetService.completionPercent(ds));

        /* liste des couples (5 premiers caractères) */
        m.addAttribute("pairs",
                textPairRepo.findByDataset(ds));

        /* stats annotateurs */
        m.addAttribute("stats",
                assignmentService.statsForDataset(ds));

        return "datasets/details";
    }

    /* retirer un annotateur de ce dataset */
    @PostMapping("/{datasetId}/unassign/{annotatorId}")
    public String unassign(@PathVariable Long datasetId,
                           @PathVariable Long annotatorId) {

        assignmentService.removeAssignment(datasetId, annotatorId);
        return "redirect:/admin/datasets/" + datasetId;
    }
    /* ----------- page de sélection ----------- */
    @GetMapping("/{id}/assign")
    public String assignForm(@PathVariable Long id, Model m) {
        Dataset ds = datasetService.get(id);
        m.addAttribute("dataset", ds);

        /* tous les annotateurs existants */
        m.addAttribute("annotators",
                userRepo.findAll().stream()
                        .filter(u -> u instanceof Annotator)
                        .map(u -> (Annotator) u)
                        .toList());
        return "datasets/assign";
    }

    /* ----------- validation ----------- */
    @PostMapping("/{id}/assign")
    public String assignSave(@PathVariable Long id,
                             @RequestParam(name="annotatorIds",required=false)
                             List<Long> annotatorIds) {

        if (annotatorIds != null && !annotatorIds.isEmpty()) {
            Dataset ds = datasetService.get(id);
            List<Annotator> list = userRepo.findAllById(annotatorIds)
                    .stream()
                    .map(u -> (Annotator)u).toList();
            assignmentService.assignDataset(ds, list);
        }
        return "redirect:/admin/datasets/" + id;      // retour page détails
    }
    /* --------------------------------------------------------------
   GET /admin/datasets/{id}/export      -> téléchargement CSV
   -------------------------------------------------------------- */
    /* DatasetController.java */
    @GetMapping("/{id}/export-full")
    public ResponseEntity<Resource> exportFull(@PathVariable Long id) throws IOException {

        ByteArrayResource csv = datasetService.exportAnnotationsCsv(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"dataset_"+id+"_annotations.csv\"")
                .contentLength(csv.contentLength())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }


}
