// AnnotationRepository.java
package com.ensah.annonateur.repositories;

import com.ensah.annonateur.models.Annotator;
import com.ensah.annonateur.models.Annotation;
import com.ensah.annonateur.models.Dataset;
import com.ensah.annonateur.models.TextPair;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByAnnotator(Annotator annotator);
    boolean existsByTextPairAndAnnotator(TextPair textPair, Annotator annotator);
    @Query("""
   select count(distinct a.textPair.id)
   from Annotation a
   where a.annotator = :ann
     and a.dataset = :ds
""")
    long countDone(@Param("ds")  Dataset ds,
                   @Param("ann") Annotator ann);
    /* -------  AJOUTS  ------- */

    /* 1)  Toutes les annotations d’une paire */
    List<Annotation> findByTextPair(TextPair pair);

    /* 2)  Nombre d’annotations pour une paire (id) */
    @Query("select count(a) from Annotation a where a.textPair.id = :pid")
    int countByTextPair(@Param("pid") Long pairId);

    /* 3)  Annotateurs ayant annoté un dataset */
    @Query("""
        select distinct a.annotator
        from Annotation a
        where a.dataset.id = :dsId
    """)
    List<Annotator> annotatorsOnDataset(@Param("dsId") Long datasetId);

    /* 4)  Comptage des votes majoritaires (hits) d’un annotateur
           – version simplifiée : on considère la majorité comme
             la valeur la plus fréquente pour CHAQUE paire              */
    @Query("""
       select count(*)
       from Annotation a
       where a.dataset    = :ds
         and a.annotator  = :ann
         and a.label = (
              select b.label
              from   Annotation b
              where  b.textPair = a.textPair
              group  by b.label
              order  by count(*) desc
              limit 1
         )
    """)
    int majorityHits(@Param("ds") Dataset ds,
                     @Param("ann") Annotator ann);


    @Query("""
   select a from Annotation a
   where a.textPair.dataset = :ds
""")
    List<Annotation> findByTextPair_Dataset(@Param("ds") Dataset ds);
}