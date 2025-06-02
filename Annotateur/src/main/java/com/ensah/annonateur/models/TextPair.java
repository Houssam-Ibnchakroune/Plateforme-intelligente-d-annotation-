package com.ensah.annonateur.models;

import jakarta.persistence.*;

import java.util.List;


@Entity
public class TextPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String text1;

    @Column(columnDefinition = "TEXT")
    private String text2;

    @Enumerated(EnumType.STRING)
    private TaskType taskType; // SIMILARITY, NLI, etc

    @OneToMany(mappedBy = "textPair")
    private List<Annotation> annotations;
    @ManyToOne
    private Dataset dataset;
    @Column(length = 32)
    private String goldLabel;   // la « classe » fournie dans le fichier

    /* ---------------- getters/setters ---------------- */

    public String getGoldLabel()            { return goldLabel; }
    public void setGoldLabel(String label)  { this.goldLabel = label; }
    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
// Constructeurs/Getters/Setters

    public Long getId() {
        return id;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }
}
