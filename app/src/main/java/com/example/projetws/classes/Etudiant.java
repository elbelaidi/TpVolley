package com.example.projetws.classes;


import com.google.gson.annotations.SerializedName;

public class Etudiant {
    private int id;
    private String nom;
    private String prenom;
    private String ville;
    private String sexe;
    private String photo;

    @SerializedName("date_naissance")
    private String dateNaissance;

    // Constructeur vide
    public Etudiant() {
    }

    // Constructeur complet
    public Etudiant(int id, String nom, String prenom, String ville, String sexe, String photo, String dateNaissance) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.ville = ville;
        this.sexe = sexe;
        this.photo = photo;
        this.dateNaissance = dateNaissance;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getVille() {
        return ville;
    }

    public String getSexe() {
        return sexe;
    }

    public String getPhoto() {
        return photo;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}