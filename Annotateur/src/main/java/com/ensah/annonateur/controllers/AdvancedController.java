package com.ensah.annonateur.controllers;

import com.ensah.annonateur.services.DatasetService;
import com.ensah.annonateur.services.PythonService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import java.util.Set;


@Controller
@RequestMapping("/admin/advanced")
@RequiredArgsConstructor
public class AdvancedController {

    private final DatasetService datasetService;
    private final PythonService  python;
    @GetMapping                   /*  ➜  /admin/advanced */
    public String list(Model model) {

        model.addAttribute("datasets", datasetService.listAll());
        return "advanced/list";          // templates/advanced/list.html
    }
    /* ---------------- MÉTRIQUES ------------------------------- */
    @GetMapping("/{id}/metrics")
    public String metrics(@PathVariable Long id, Model model) throws IOException {

        Path tmpCsv = writeTempCsv(id);
        JsonNode out = python.run("metrics.py", tmpCsv);

        /* -------- bloc Fleiss -------- */
        model.addAttribute("dataset", datasetService.get(id));
        model.addAttribute("fleiss",  out.path("fleiss").asDouble());

        /* -------- bloc par-annotateur -------- */
        // on transforme le tableau JSON -> List<Map.Entry<String,Double>>
        List<Map.Entry<String,Double>> perAnnot = new ArrayList<>();

        JsonNode arr = out.path("per_annotator");          // renvoie [] si absent
        if (arr.isArray()) {
            arr.forEach(n -> {
                String name = n.path("annotateur").asText(null);
                double k    = n.path("kappa").asDouble(Double.NaN);
                if (name != null && !Double.isNaN(k))
                    perAnnot.add(Map.entry(name, k));
            });
        }
        // tri décroissant (optionnel)
        perAnnot.sort(Map.Entry.<String,Double>comparingByValue().reversed());

        model.addAttribute("perAnnot", perAnnot);
        return "advanced/metrics";
    }



    /* ---------------- SPAMMEURS ------------------------------- */
    // --- DTO interne ---
            record SpamRow(String annotateur,double kappa,int n){}

    @GetMapping("/{id}/spammers")
    public String spammers(@PathVariable Long id, Model model) throws IOException {

        /* 1) génère un CSV temporaire des annotations -------------------- */
        Path tmpCsv = writeTempCsv(id);

        /* 2) exécute le script Python  ----------------------------------- */
        JsonNode out = python.run("spam.py", tmpCsv);

        /* 3) ←  spammers  =  ["user1", "user2", …] ----------------------- */
        List<String> spamNames = new ArrayList<>();
        out.withArray("spammers").forEach(n -> spamNames.add(n.asText()));

        /* 4) ←  details  = [{annotateur,kappa,n}, …]  -------------------- */
        List<SpamRow> details = new ArrayList<>();
        out.withArray("details").forEach(d -> details.add(
                new SpamRow(
                        d.get("annotateur").asText(),
                        d.get("kappa").asDouble(),
                        d.get("n").asInt()
                )));

        /* 5) données envoyées à la vue  ---------------------------------- */
        model.addAttribute("dataset",  datasetService.get(id));
        model.addAttribute("details",  details);     // tous les annotateurs
        model.addAttribute("spammers", spamNames);   // seuls les spammeurs

        return "advanced/spammers";
    }




    /* utilitaire commun */
    private Path writeTempCsv(Long id) throws IOException {
        ByteArrayResource res = datasetService.exportAnnotationsCsv(id);
        Path tmp = Files.createTempFile("ds_" + id, ".csv");
        Files.write(tmp, res.getByteArray());
        return tmp;
    }
}
