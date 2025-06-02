package com.ensah.annonateur.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Dataset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 500)
    private String description;

    /** classes séparées par ; ex : "similar;not_similar" */
    private String classes;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    private LocalDateTime createdAt;

    /* ---------------- getters/setters ---------------- */

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    /* Getters / setters */

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String n){ this.name=n; }
    public String getDescription(){ return description; }
    public void setDescription(String d){ this.description=d; }
    public String getClasses(){ return classes; }
    public void setClasses(String c){ this.classes=c; }
    public TaskType getTaskType(){ return taskType; }
    public void setTaskType(TaskType t){ this.taskType=t; }

}
