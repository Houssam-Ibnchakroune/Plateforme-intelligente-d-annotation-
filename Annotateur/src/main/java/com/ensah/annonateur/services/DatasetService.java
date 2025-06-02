package com.ensah.annonateur.services;

import com.ensah.annonateur.models.*;
import com.ensah.annonateur.repositories.*;          // + AnnotationRepository
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;                // <<—  nouvel import

@Service
@Transactional
public class DatasetService {

    private final DatasetRepository    datasetRepo;
    private final TextPairRepository   pairRepo;
    private final AnnotationRepository annotationRepo;   // <<—  injecté

    public DatasetService(DatasetRepository dRepo,
                          TextPairRepository pRepo,
                          AnnotationRepository aRepo) {  // <<—  ctor élargi
        this.datasetRepo  = dRepo;
        this.pairRepo     = pRepo;
        this.annotationRepo = aRepo;
    }

    /* ---------- CRUD basique ---------- */
    public List<Dataset> listAll() { return datasetRepo.findAll(); }

    public Dataset get(Long id) {
        return datasetRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dataset "+id+" introuvable"));
    }

    /* ---------- Création + import ---------- */
    public Dataset create(String name,
                          String desc,
                          String classes,
                          TaskType type,
                          MultipartFile file) throws IOException {

        Dataset ds = new Dataset();
        ds.setName(name);
        ds.setDescription(desc);
        ds.setClasses(classes);
        ds.setTaskType(type);
        ds.setCreatedAt(LocalDateTime.now());
        datasetRepo.save(ds);

        importFile(ds, type, file);
        return ds;
    }

    /* ---------- % d’avancement ---------- */
    public double completionPercent(Dataset ds) {
        long total = pairRepo.countByDataset(ds);
        long done  = pairRepo.countAnnotatedPairs(ds);
        return total == 0 ? 0 : 100.0 * done / total;
    }

    /* ======================================================================
                              IMPORTS
   ======================================================================*/
    private void importFile(Dataset ds, TaskType type, MultipartFile file) throws IOException {

        String fn = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase(Locale.ROOT);

        if      (fn.endsWith(".csv"))  parseCsv (file.getInputStream(), ds, type);
        else if (fn.endsWith(".json")) parseJson(file.getInputStream(), ds, type);
        else throw new IllegalArgumentException("Seuls CSV et JSON sont acceptés.");
    }

    /* ---------------- CSV ----------------
       id,text1,text2  (header facultatif)
       --------------------------------------------------------------------*/
    private void parseCsv(InputStream in, Dataset ds, TaskType type) throws IOException {

        CSVFormat fmt = CSVFormat.DEFAULT
                .withDelimiter(',')            // séparateur virgule
                .withIgnoreEmptyLines()
                .withFirstRecordAsHeader();     // saute la ligne d’en-tête si présente

        try (CSVParser parser = new CSVParser(
                new InputStreamReader(in, StandardCharsets.UTF_8), fmt)) {

            for (CSVRecord rec : parser) {

                if (rec.size() < 3) continue;   // ligne incomplète → on l’ignore

                savePair(ds, type,
                        rec.get(1).trim(),     // text1
                        rec.get(2).trim(),     // text2
                        "");                   // pas de goldLabel
            }
        }
    }

    /* ---------------- JSON ----------------
       [
         {"id":123,"text1":"…","text2":"…"},
         …
       ]
       --------------------------------------------------------------------*/
    private record JsonInput(long id, String text1, String text2) {}

    private void parseJson(InputStream in, Dataset ds, TaskType type) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        List<JsonInput> list = mapper.readValue(
                in,
                mapper.getTypeFactory()
                        .constructCollectionType(List.class, JsonInput.class));

        list.stream()
                .filter(r -> r.text1()!=null && r.text2()!=null)
                .forEach(r -> savePair(ds, type,
                        r.text1().trim(),
                        r.text2().trim(),
                        ""));         // goldLabel vide
    }

    /* ---------- factorisation ---------- */
    private void savePair(Dataset ds, TaskType type,
                          String s1, String s2, String gold) {

        TextPair p = new TextPair();
        p.setDataset(ds);
        p.setTaskType(type);
        p.setText1(s1);
        p.setText2(s2);
        p.setGoldLabel(gold);      // toujours vide dans ce scénario
        pairRepo.save(p);
    }
    /** Exporte : id_pair,texte,label,annotateur,timestamp */
    public ByteArrayResource exportAnnotationsCsv(Long datasetId) throws IOException {

        Dataset ds = datasetRepo.findById(datasetId)
                .orElseThrow(() -> new NoSuchElementException("Dataset introuvable"));

        /* récupère *toutes* les annotations du dataset, jointure via textPair */
        List<Annotation> rows = annotationRepo.findByTextPair_Dataset(ds);

        StringWriter sw = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(sw)) {
            bw.write("id,texte,classe,annotateur,date\n");

            for (Annotation a : rows) {
                TextPair p = a.getTextPair();
                String texte = (p.getText1() + " <SEP> " + p.getText2()).replace("\n"," ");

                bw.append(p.getId().toString()).append(',')
                        .append(csvEscape(texte)).append(',')
                        .append(csvEscape(a.getLabel())).append(',')
                        .append(csvEscape(a.getAnnotator().getUsername())).append(',')
                        .append(a.getTimestamp().toString()).append('\n');
            }
        }
        return new ByteArrayResource(sw.toString().getBytes(StandardCharsets.UTF_8));
    }

    /* échappe guillemets / virgules */
    private static String csvEscape(String s){
        if (s == null) return "";
        String out = s.replace("\"","\"\"");
        return "\"" + out + "\"";
    }


}
