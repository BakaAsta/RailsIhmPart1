package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IOJeu extends Jeu {
    /**
     * Liste contenant les instructions à lire (qui remplacent les entrées au clavier)
     */
    private List<String> instructions;
    
    public List<String> getInstructions() {
        return instructions;
    }

    /**
     * Constructeur, qui reprend exactement le constructeur de Game
     */
    public IOJeu(String[] nomJoueurs) {
        super(nomJoueurs);
        this.instructions = new ArrayList<>();
    }

    /**
     * Lit et renvoie une instruction dans la liste
     */
    public String lireLigne() {
        return instructions.remove(0);
    }

    /*@Override
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {

    }*/

    /**
     * Fixe la liste d'instructions du jeu.
     *
     * @param args liste de chaînes de caractères. Chaque élément est une instruction (sans '\n' à la fin)
     */
    public void setInput(String... args) {
        instructions.clear();
        Collections.addAll(instructions, args);
    }
    
    /**
     * Fixe la liste d'instructions du jeu.
     *
     * @param args liste de chaînes de caractères. Chaque élément est une instruction (sans '\n' à la fin)
     */
    public void setInput(List<String> args) {
        instructions = args;
    }
}
