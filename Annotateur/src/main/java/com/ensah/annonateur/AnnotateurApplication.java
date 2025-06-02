package com.ensah.annonateur;

import com.ensah.annonateur.models.Admin;
import com.ensah.annonateur.models.Annotator;
import com.ensah.annonateur.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AnnotateurApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnnotateurApplication.class, args);
    }
    @Bean
    public CommandLineRunner initAdmin(UserRepository repo, PasswordEncoder enc){
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                Admin admin = new Admin("admin", enc.encode("admin123"));
                admin.setFirstName("Super"); admin.setLastName("Admin");
                repo.save(admin);
            }
        };
    }

    @Bean
    public CommandLineRunner initAnnot(UserRepository repo, PasswordEncoder enc){
        return args -> {
            if (repo.findByUsername("annot1").isEmpty()) {
                Annotator an = new Annotator();
                an.setUsername("annot1");
                an.setPassword(enc.encode("annotpass"));
                an.setFirstName("Alpha"); an.setLastName("Tester");
                repo.save(an);
            }
        };
    }

}
