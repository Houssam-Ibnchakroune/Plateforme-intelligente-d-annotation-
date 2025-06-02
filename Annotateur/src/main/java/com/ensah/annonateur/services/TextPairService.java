package com.ensah.annonateur.services;

import com.ensah.annonateur.models.Annotation;
import com.ensah.annonateur.models.Annotator;
import com.ensah.annonateur.models.TaskType;
import com.ensah.annonateur.models.TextPair;
import com.ensah.annonateur.repositories.AnnotationRepository;
import com.ensah.annonateur.repositories.TextPairRepository;
import com.ensah.annonateur.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class TextPairService {

    private final TextPairRepository repo;
    private final UserRepository userRepo;
    private final AnnotationRepository annotationRepo;

    public TextPairService(TextPairRepository repo,
                           UserRepository userRepo,
                           AnnotationRepository annotationRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.annotationRepo = annotationRepo;
    }

    /* -------- IMPORT CSV ou JSON -------- */
    @Transactional
    public void importFile(MultipartFile file, TaskType taskType) throws IOException {
        if (file.getOriginalFilename().endsWith(".csv")) importCsv(file, taskType);
        else importJson(file, taskType);
    }

    private void importCsv(MultipartFile file, TaskType taskType) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            reader.lines().skip(1).forEach(l -> {               // sauté l'en-tête
                String[] t = l.split(";", -1);                  // id;text1;text2
                TextPair p = new TextPair();
                p.setText1(t[1]); p.setText2(t[2]); p.setTaskType(taskType);
                repo.save(p);
            });
        }
    }
    private void importJson(MultipartFile file, TaskType taskType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(file.getInputStream());
        for (JsonNode node : root) {
            TextPair p = new TextPair();
            p.setText1(node.get("text1").asText());
            p.setText2(node.get("text2").asText());
            p.setTaskType(taskType);
            repo.save(p);
        }
    }

    /* -------- ATTRIBUTION aléatoire -------- */
    @Transactional
    public void assignRandom(int copies) {
        List<Annotator> annots = userRepo.findAll()
                .stream()
                .filter(a -> a instanceof Annotator)
                .map(a -> (Annotator)a)
                .toList();

        List<TextPair> pairs = repo.findAll();
        Random rnd = new Random();
        for (TextPair p : pairs) {
            // on veut par ex. 3 copies
            Collections.shuffle(annots, rnd);
            for (int i = 0; i < copies; i++) {
                Annotator at = annots.get(i % annots.size());
                if (!annotationRepo.existsByTextPairAndAnnotator(p, at)) {
                    Annotation a = new Annotation();
                    a.setTextPair(p); a.setAnnotator(at); a.setLabel("UNLABELED");
                    annotationRepo.save(a);
                }
            }
        }
    }

    /* -------- EXPORT CSV -------- */
    public byte[] exportCsv() {
        StringBuilder sb = new StringBuilder("id;text1;text2;label;annotator;timestamp\n");
        annotationRepo.findAll().forEach(a -> sb.append(
                "%d;%s;%s;%s;%s;%s\n".formatted(
                        a.getTextPair().getId(),
                        a.getTextPair().getText1().replaceAll(";", ","),
                        a.getTextPair().getText2().replaceAll(";", ","),
                        a.getLabel(),
                        a.getAnnotator().getUsername(),
                        a.getTimestamp()
                )));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

}
