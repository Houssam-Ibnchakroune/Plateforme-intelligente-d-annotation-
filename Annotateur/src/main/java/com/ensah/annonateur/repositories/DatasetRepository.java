package com.ensah.annonateur.repositories;

import com.ensah.annonateur.models.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset,Long> {}
