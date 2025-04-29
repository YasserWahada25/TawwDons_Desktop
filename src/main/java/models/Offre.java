package models;

public class Offre {
    private int id;
    private String titreOffre;        // correspond à titre_offre
    private String descriptionOffre;  // correspond à description_offre

    public Offre() { }

    public Offre(int id, String titreOffre, String descriptionOffre) {
        this.id = id;
        this.titreOffre = titreOffre;
        this.descriptionOffre = descriptionOffre;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreOffre() {
        return titreOffre;
    }

    public void setTitreOffre(String titreOffre) {
        this.titreOffre = titreOffre;
    }

    public String getDescriptionOffre() {
        return descriptionOffre;
    }

    public void setDescriptionOffre(String descriptionOffre) {
        this.descriptionOffre = descriptionOffre;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", titreOffre='" + titreOffre + '\'' +
                ", descriptionOffre='" + descriptionOffre + '\'' +
                '}';
    }
}
