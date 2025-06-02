package com.ensah.annonateur.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
@Entity
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "textpair_id")
    private TextPair textPair;

    @ManyToOne
    @JoinColumn(name = "annotator_id")
    private Annotator annotator;

    private String label; // "similar", "entails", etc

    private LocalDateTime timestamp = LocalDateTime.now();
    /* ------------ nouveau champ ------------ */
    @ManyToOne
    @JoinColumn(name = "dataset_id",
            nullable = false)           // colonne non nulle
    // Getters/Setters
    private Dataset dataset;
    public void setId(Long id) {
        this.id = id;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setTextPair(TextPair textPair) {
        this.textPair = textPair;
    }

    public void setAnnotator(Annotator annotator) {
        this.annotator = annotator;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public TextPair getTextPair() {
        return textPair;
    }

    public Annotator getAnnotator() {
        return annotator;
    }

    public String getLabel() {
        return label;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}