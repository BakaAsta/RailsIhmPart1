package fr.umontpellier.iut.rails;

import fr.umontpellier.iut.rails.data.CarteTransport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PilesCartesTransport {
    private final List<CarteTransport> pilePioche;
    private final List<CarteTransport> pileDefausse;

    public PilesCartesTransport(List<CarteTransport> cartes) {
        pilePioche = cartes;
        pileDefausse = new ArrayList<>();
        // on mélange la pile de pioche
        Collections.shuffle(pilePioche);
    }

    /**
     * Retire une carte. Si la pioche est vide, alors on mélange les cartes de la défausse et on les transfère dans la pioche.
     * La fonction retire et renvoie la première carte de la pioche (si elle existe) et null sinon.
     * @return la carte retirée ou null
     */
    public CarteTransport piocher() {

        // si la pioche est vide, faire appel une fonction qui melangeras
        if (pilePioche.isEmpty()) {
            melanger();
        }
        return pilePioche.remove(0);
    }

    public void melanger() {
        Collections.shuffle(pileDefausse);
        for (int i = 0; i < pileDefausse.toArray().length; i++) {
            pilePioche.add(i, pileDefausse.get(i));
        }
    }

    public List<CarteTransport> getPilePioche() {
        return pilePioche;
    }

    public List<CarteTransport> getPileDefausse() {
        return pileDefausse;
    }

    public void defausser(CarteTransport carte) {
            pileDefausse.add(carte);
    }

    /**
     * @return true si aucune carte ne peut être prise (si les deux piles sont vides)
     */
    public boolean estVide() {
        int cpt = 0;
        if (pilePioche.isEmpty()) {
            cpt++;
        }
        if (pileDefausse.isEmpty()) {
            cpt++;
        }
        if (cpt == 2) {
            return true;
        }
        return false;
    }

    public List<CarteTransport> getCartes() {
        ArrayList<CarteTransport> cartes = new ArrayList<>();
        cartes.addAll(pilePioche);
        cartes.addAll(pileDefausse);
        return cartes;
    }

    public Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("pioche", pilePioche.size()),
                Map.entry("defausse", pileDefausse));
    }
}
