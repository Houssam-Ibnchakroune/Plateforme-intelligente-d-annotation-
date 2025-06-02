package com.ensah.annonateur.services;

import com.ensah.annonateur.models.*;
import com.ensah.annonateur.repositories.AnnotationRepository;
import com.ensah.annonateur.repositories.TextPairRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnotationService {

    private final TextPairRepository   pairRepo;
    private final AnnotationRepository annotationRepo;

    public AnnotationService(TextPairRepository pairRepo,
                             AnnotationRepository annotationRepo) {
        this.pairRepo      = pairRepo;
        this.annotationRepo = annotationRepo;
    }

    /* --------------------------------------------------------------
       Paires restantes pour ce dataset et cet annotateur
       -------------------------------------------------------------- */
    public List<TextPair> unannotatedPairs(Dataset ds, Annotator ann) {
        return pairRepo.findByDataset(ds).stream()
                .filter(p -> !annotationRepo.existsByTextPairAndAnnotator(p, ann))
                .toList();
    }

    /* --------------------------------------------------------------
       Sauvegarde d’une annotation (avec dataset_id)
       -------------------------------------------------------------- */
    @Transactional
    public void save(Dataset ds, Long pairId, Annotator ann, String label) {

        TextPair pair = pairRepo.findById(pairId)
                .orElseThrow();              // paire inconnue

        /* cohérence : la paire doit appartenir à ce dataset */
        if (!pair.getDataset().equals(ds))
            throw new IllegalArgumentException("pair ≠ dataset");

        /* si déjà annotée par cet utilisateur → rien à faire */
        if (annotationRepo.existsByTextPairAndAnnotator(pair, ann))
            return;

        /* création de l’annotation */
        Annotation a = new Annotation();
        a.setDataset(ds);          // ← nouveau champ
        a.setTextPair(pair);
        a.setAnnotator(ann);
        a.setLabel(label);

        annotationRepo.save(a);
    }
}
