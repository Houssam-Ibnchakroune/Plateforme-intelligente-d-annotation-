package com.ensah.annonateur.services;

import com.ensah.annonateur.models.*;
import com.ensah.annonateur.repositories.AnnotationRepository;
import com.ensah.annonateur.repositories.TextPairRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Calculs de qualité d’annotation :
 *   – Fleiss κ global pour un dataset
 *   – Heuristique de détection des « spammeurs »
 */
@Service
public class MetricsService {

    private final AnnotationRepository annRepo;
    private final TextPairRepository   pairRepo;

    public MetricsService(AnnotationRepository annRepo,
                          TextPairRepository pairRepo) {
        this.annRepo  = annRepo;
        this.pairRepo = pairRepo;
    }

    /* ===============================================================
       1) Fleiss κ (version compactée, toutes paires même nb de juges)
       =============================================================== */
    public double fleissKappa(Dataset ds) {

        List<TextPair> pairs = pairRepo.findByDataset(ds);
        if (pairs.isEmpty()) return 0;

        /* 1-A  : liste des catégories rencontrées */
        List<String> categories = new ArrayList<>();
        pairs.forEach(p -> annRepo.findByTextPair(p).forEach(a -> {
            if (!categories.contains(a.getLabel()))
                categories.add(a.getLabel());
        }));
        int m = categories.size();

        /* 1-B  : tableau n_ij  (pair → distribution catégorie) */
        Map<Long,int[]> rows = new HashMap<>();
        int n = 0;                                        // nb de juges/pair

        for (TextPair p : pairs) {
            int[] counts = new int[m];                    // catégories
            List<Annotation> anns = annRepo.findByTextPair(p);
            n = Math.max(n, anns.size());
            anns.forEach(a -> {
                int j = categories.indexOf(a.getLabel());
                counts[j]++;
            });
            rows.put(p.getId(), counts);
        }

        /* 1-C  : calcul P̄ et P_e */
        double sumPi = 0;
        double[] pj = new double[m];                      // proportion par cat.

        for (int[] r : rows.values()) {
            double Pi = 0;
            for (int j = 0; j < m; j++) {
                pj[j] += r[j];
                Pi += r[j] * (r[j] - 1);
            }
            Pi /= (double) n * (n - 1);
            sumPi += Pi;
        }
        double Pbar = sumPi / rows.size();

        for (int j = 0; j < m; j++) pj[j] /= (double) rows.size() * n;
        double Pe = 0;
        for (double p : pj) Pe += p * p;

        return (Pe == 1) ? 0 : (Pbar - Pe) / (1 - Pe);
    }

    /* ===============================================================
       2) Détection simple de spammeurs
       Heuristique : exactitude individuelle < ‘threshold’
       =============================================================== */
    public List<Annotator> spammers(Dataset ds) {

        List<Annotator> annotators = annRepo.annotatorsOnDataset(ds.getId());
        if (annotators.isEmpty()) return List.of();

        /* seuil : 50 % si κ ≥ 0.2, sinon 30 % */
        double threshold = fleissKappa(ds) >= 0.2 ? 0.5 : 0.3;

        return annotators.stream()
                .filter(a -> individualAccuracy(ds, a) < threshold)
                .toList();
    }

    /* ----- exactitude par annotateur (majorité VS vote) ------------ */
    private double individualAccuracy(Dataset ds, Annotator ann) {

        int total   = (int) annRepo.countDone(ds, ann);     // nb de paires traitées
        if (total == 0) return 1;                           // pas d’info → OK

        int correct = annRepo.majorityHits(ds, ann);        // votes = majorité
        return (double) correct / total;
    }
}
