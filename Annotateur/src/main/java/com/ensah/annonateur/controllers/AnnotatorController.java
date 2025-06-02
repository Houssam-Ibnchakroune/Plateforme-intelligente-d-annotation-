package com.ensah.annonateur.controllers;

import com.ensah.annonateur.models.Annotator;
import com.ensah.annonateur.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.SecureRandom;


@Controller
@RequestMapping("/admin/annotators")
public class AnnotatorController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AnnotatorController(UserRepository r, PasswordEncoder e){
        this.repo=r; this.encoder=e;
    }

    @GetMapping
    public String list(Model m){
        m.addAttribute("annotators",
                repo.findAll().stream()
                        .filter(u -> u instanceof Annotator)
                        .toList());
        return "annotators/list";
    }

    @GetMapping("/new")   /* formulaire */
    public String form(){ return "annotators/new"; }

    @PostMapping("/new")
    public String create(@RequestParam String username,
                         @RequestParam String firstName,
                         @RequestParam String lastName,
                         RedirectAttributes ra) {          // ← flash-scope

        /* 1) Vérifier l’unicité du login */
        if (repo.findByUsername(username).isPresent()) {
            ra.addFlashAttribute("error", "Ce login existe déjà !");
            return "redirect:/admin/annotators/new";
        }

        /* 2) Génération d’un mot de passe aléatoire — 8 caractères */
        String rawPwd = generatePassword(8);              // ex. "x7D3pQ9a"

        /* 3) Création de l’annotateur */
        Annotator a = new Annotator();
        a.setUsername(username);
        a.setPassword(encoder.encode(rawPwd));            // hashé en base
        a.setFirstName(firstName);
        a.setLastName(lastName);
        repo.save(a);

        /* 4) Message flash contenant les identifiants */
        ra.addFlashAttribute("success",
                "Identifiants créés : " + username + " / " + rawPwd);

        return "redirect:/admin/annotators";
    }

    /* utilitaire local (ou dans un service) */
    private String generatePassword(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Long id){
        repo.findById(id).ifPresent(u -> { u.setEnabled(false); repo.save(u);});
        return "redirect:/admin/annotators";
    }
    /* ----------- ouvrir le formulaire ----------- */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model m) {
        Annotator a = (Annotator) repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id"));
        m.addAttribute("annotator", a);
        m.addAttribute("error", null);          // pas d’erreur au 1er affichage
        return "annotators/edit";
    }

    /* ----------- sauvegarder ----------- */
    @PostMapping("/{id}/edit")
    public String editSave(@PathVariable Long id,
                           @RequestParam String username,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           Model m) {

        /* unicité du login hors annotateur courant */
        var existing = repo.findByUsername(username);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            Annotator a = (Annotator) repo.findById(id).get();
            m.addAttribute("annotator", a);
            m.addAttribute("error", "Ce login existe déjà !");
            return "annotators/edit";
        }

        Annotator a = (Annotator) repo.findById(id).orElseThrow();
        a.setUsername(username);
        a.setFirstName(firstName);
        a.setLastName(lastName);
        repo.save(a);
        return "redirect:/admin/annotators";
    }
}

