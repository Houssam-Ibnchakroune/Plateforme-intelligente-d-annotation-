package com.ensah.annonateur.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exécute un script Python présent dans le dossier {@code scripts/} à la
 * racine du projet (ou du jar exécutable).
 *
 *   pythonService.run("spam.py",  pathVersCsv)
 */
@Service
public class PythonService {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Racine du projet, injectée à partir de la propriété spring :
     *   annoncateur.scripts.root = ./scripts
     *
     * Si vous ne touchez à rien, 'scripts' est pris relativement au
     * répertoire de lancement (utile en dev ET après packaging).
     */
    private final Path scriptRoot;

    public PythonService(
            @Value("${annoncateur.scripts.root:scripts}") String root) {
        this.scriptRoot = Paths.get(root).toAbsolutePath().normalize();
    }

    public JsonNode run(String scriptFileName, Path arg) {
        try {

            /* 1) chemin absolu du script  -------------------------------- */
            Path script = scriptRoot.resolve(scriptFileName).normalize();
            if (!Files.isRegularFile(script))
                throw new RuntimeException(
                        "Script introuvable : " + script);

            /* 2) lance Python -------------------------------------------- */
            Process p = new ProcessBuilder("python",
                    script.toString(),
                    arg.toString())
                    .redirectErrorStream(true)   // stdout+stderr ensemble
                    .start();

            String out = new String(p.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            int rc = p.waitFor();
            if (rc != 0)
                throw new RuntimeException(
                        "Erreur Python (code " + rc + ") :\n" + out);

            /* 3) extrait la 1ʳᵉ accolade pour ignorer d’éventuels logs --- */
            int i = out.indexOf('{');
            if (i < 0)
                throw new RuntimeException(
                        "Le script n’a pas renvoyé de JSON :\n" + out);

            return mapper.readTree(out.substring(i));

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erreur Python : " + e.getMessage(), e);
        }
    }
}
