package com.ensah.annonateur.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        columnNames = {"dataset_id","annotator_id"}))
public class Assignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private Dataset dataset;
    @ManyToOne private Annotator annotator;

    private LocalDateTime assignedAt = LocalDateTime.now();
    public Assignment(Dataset dataset, Annotator annotator) {
        this.dataset   = dataset;
        this.annotator = annotator;
        this.assignedAt = LocalDateTime.now();
    }
    public Annotator getAnnotator() { return annotator; }
    public Dataset    getDataset()  { return dataset;  }

    public Assignment() {}
    /* getters / setters */

    public Long getId() {
        return id;
    }



    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setAnnotator(Annotator annotator) {
        this.annotator = annotator;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
