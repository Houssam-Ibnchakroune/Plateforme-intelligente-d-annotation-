package com.ensah.annonateur.repositories;

import com.ensah.annonateur.models.Dataset;
import com.ensah.annonateur.models.TextPair;
import com.ensah.annonateur.models.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TextPairRepository extends JpaRepository<TextPair, Long> {
    List<TextPair> findByTaskType(TaskType taskType);
    long countByDataset(Dataset ds);

    /* --- NOUVELLE MÉTHODE --- */
    List<TextPair> findByDataset(Dataset dataset);


    /* compter les paires qui ont au moins UNE annotation label != '' */
    @Query("SELECT COUNT(DISTINCT a.textPair) FROM Annotation a " +
            "WHERE a.textPair.dataset = :ds AND a.label <> ''")
    long countAnnotatedPairs(@Param("ds") Dataset ds);

}