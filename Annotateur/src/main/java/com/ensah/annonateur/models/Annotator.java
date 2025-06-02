package com.ensah.annonateur.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ANNOTATOR")
public class Annotator extends User {


}