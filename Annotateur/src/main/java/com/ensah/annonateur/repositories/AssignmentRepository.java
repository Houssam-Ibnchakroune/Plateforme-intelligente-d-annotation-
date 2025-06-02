package com.ensah.annonateur.repositories;

import com.ensah.annonateur.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {

    boolean existsByDatasetAndAnnotator(Dataset d, Annotator a);

    List<Assignment> findByAnnotator(Annotator a);

    List<Assignment> findByDataset(Dataset ds);
    void deleteByDataset_IdAndAnnotator_Id(Long datasetId, Long annotatorId);
}
