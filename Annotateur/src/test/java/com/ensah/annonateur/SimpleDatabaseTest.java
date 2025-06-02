package com.ensah.annonateur;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleDatabaseTest {

    @Test
    public void testDatabaseConnection() throws SQLException {
        // Configurer la source de données manuellement
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/annonateur_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("0000");

        // Tester la connexion
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1), "La connexion à la base de données devrait être valide");
            System.out.println("Connexion à la base de données réussie !");
            System.out.println("URL de la base de données: " + connection.getMetaData().getURL());
            System.out.println("Utilisateur de la base de données: " + connection.getMetaData().getUserName());
            System.out.println("Produit de base de données: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("Version de la base de données: " + connection.getMetaData().getDatabaseProductVersion());

            // Compter les tables
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'annonateur_db'");
            if (rs.next()) {
                System.out.println("Nombre de tables dans la base de données: " + rs.getInt(1));
            }
        }
    }
}