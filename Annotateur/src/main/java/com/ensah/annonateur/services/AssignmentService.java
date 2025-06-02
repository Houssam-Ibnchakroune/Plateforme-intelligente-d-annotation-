package com.ensah.annonateur.services;

import com.ensah.annonateur.models.*;
import com.ensah.annonateur.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository repo;
    private final TextPairRepository   pairRepo;       // ← ajout
    private final AnnotationRepository annotationRepo; // ← ajout

    public AssignmentService(AssignmentRepository repo,
                             TextPairRepository pairRepo,
                             AnnotationRepository annotationRepo) {
        this.repo = repo;
        this.pairRepo = pairRepo;
        this.annotationRepo = annotationRepo;
    }

    /* DTO */
    public record AnnotatorStat(Annotator annotator, long done, long total) { }

    /* Stats par annotateur */
    public List<AnnotatorStat> statsForDataset(Dataset ds) {

        long totalPairs = pairRepo.countByDataset(ds);

        return repo.findByDataset(ds).stream()
                .map(as -> {
                    long done = annotationRepo.countDone(ds, as.getAnnotator());
                    return new AnnotatorStat(as.getAnnotator(), done, totalPairs);
                })
                .toList();
    }

    /* -------- ASSIGNATION ---------- */
    @Transactional
    public void assignDataset(Dataset d, List<Annotator> annots){
        annots.forEach(a -> {
            if (!repo.existsByDatasetAndAnnotator(d, a)) {
                repo.save(new Assignment(d, a));
            }
        });
    }

    public List<Assignment> tasksFor(Annotator a){
        return repo.findByAnnotator(a);
    }

    /* -------- DESAFFECTATION ---------- */
    @Transactional
    public void removeAssignment(Long datasetId, Long annotatorId){
        repo.deleteByDataset_IdAndAnnotator_Id(datasetId, annotatorId);
    }
    public record TaskRow(Dataset ds, long done, long total) {}

    public List<TaskRow> taskRowsFor(Annotator a) {

        return repo.findByAnnotator(a).stream()
                .map(as -> {
                    Dataset ds = as.getDataset();
                    long total = pairRepo.countByDataset(ds);
                    long done = annotationRepo.countDone(ds, a);   // nouvelle méthode
                    return new TaskRow(ds, done, total);
                })
                .toList();
    }
}
