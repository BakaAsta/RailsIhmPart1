package fr.umontpellier.iut.rails;

import java.util.Objects;

public record Bouton(
        String label,
        String valeur) {

    public Bouton(String valeur) {
        this(valeur, valeur);
    }

    @Override
    public String valeur() {
        return valeur;
    }

    public String toPrompt() {
        if (label.equals(valeur)) {
            return valeur;
        } else {
            return label + " (" + valeur + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bouton bouton = (Bouton) o;
        return Objects.equals(label, bouton.label) && Objects.equals(valeur, bouton.valeur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, valeur);
    }
}