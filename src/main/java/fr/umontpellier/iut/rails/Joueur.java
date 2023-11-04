package fr.umontpellier.iut.rails;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.umontpellier.iut.rails.data.*;
import org.glassfish.grizzly.nio.transport.DefaultStreamReader;
import org.glassfish.grizzly.streams.Input;

import javax.swing.*;
import javax.swing.text.Caret;
import java.util.HashSet;
import java.util.*;

import static fr.umontpellier.iut.rails.data.TypeCarteTransport.*;

public class Joueur {
    public enum CouleurJouer {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private final Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;

    private String pseudo;
    /**
     * CouleurJouer du joueur (pour représentation sur le plateau)
     */
    private final CouleurJouer couleur;
    /**
     * Liste des villes sur lesquelles le joueur a construit un port
     */
    private final List<Ville> ports;
    /**
     * Liste des routes capturées par le joueur
     */
    private final List<Route> routes;
    /**
     * Nombre de pions wagons que le joueur peut encore poser sur le plateau
     */
    private int nbPionsWagon;
    /**
     * Nombre de pions wagons que le joueur a dans sa réserve (dans la boîte)
     */
    private int nbPionsWagonEnReserve;
    /**
     * Nombre de pions bateaux que le joueur peut encore poser sur le plateau
     */
    private int nbPionsBateau;
    /**
     * Nombre de pions bateaux que le joueur a dans sa réserve (dans la boîte)
     */
    private int nbPionsBateauEnReserve;
    /**
     * Liste des destinations à réaliser pendant la partie
     */
    private final List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private final List<CarteTransport> cartesTransport;
    /**
     * Liste temporaire de cartes transport que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'un port
     */
    private final List<CarteTransport> cartesTransportPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées, et points
     * perdus lors des échanges de pions)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, CouleurJouer couleur) {
        this.pseudo = nom;
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        this.ports = new ArrayList<>();
        this.routes = new ArrayList<>();
        this.nbPionsWagon = 0;
        this.nbPionsWagonEnReserve = 25;
        this.nbPionsBateau = 0;
        this.nbPionsBateauEnReserve = 50;
        this.cartesTransport = new ArrayList<>();
        this.cartesTransportPosees = new ArrayList<>();
        this.destinations = new ArrayList<>();
        this.score = 0;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<CarteTransport> getCartesTransportPosees() {
        return cartesTransportPosees;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public List<Ville> getPorts() {
        return ports;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public int getNbPionsWagonEnReserve() {
        return nbPionsWagonEnReserve;
    }

    public int getNbPionsBateauEnReserve() {
        return nbPionsBateauEnReserve;
    }

    public void setNbPionsWagon(int nbPionsWagon) {
        this.nbPionsWagon = nbPionsWagon;
    }

    public void setNbPionsBateau(int nbPionsBateau) {
        this.nbPionsBateau = nbPionsBateau;
    }

    public void setNbPionsWagonEnReserve(int nbPionsWagonEnReserve) {
        this.nbPionsWagonEnReserve = nbPionsWagonEnReserve;
    }


    public void setNbPionsBateauEnReserve(int nbPionsBateauEnReserve) {
        this.nbPionsBateauEnReserve = nbPionsBateauEnReserve;
    }

    public int getNbPionsWagon() {
        return nbPionsWagon;
    }

    public int getNbPionsBateau() {
        return nbPionsBateau;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String new_name) {
        this.nom = new_name;
    }

    public List<CarteTransport> getCartesTransport() {
        return cartesTransport;
    }


    public boolean peutPoserPort(Ville v, List<CarteTransport> cartes) {
        int nb_carte_wagon = 0;
        int nb_carte_bateau = 0;

        int cpt = 0;

        if (v.estPort()) {
            if (ports.size() < 3) {

                for (Route r : routes) {
                    if (r.getVille1().nom().equals(v.nom())) {
                        cpt++;
                    }
                    if (r.getVille2().nom().equals(v.nom())) {
                        cpt++;
                    }
                }

                if (cpt >= 1) {
                    for (Couleur couleur : Couleur.values()) {
                        List<CarteTransport> trie_carte = cartes.stream().filter(c -> c.getAncre() && c.getCouleur().equals(couleur)).toList();
                        for (CarteTransport carte : trie_carte) {
                            nb_carte_wagon += carte.getType().equals(WAGON) ? 1 : 0;
                            nb_carte_bateau += carte.getType().equals(BATEAU) && carte.estDouble() ? 2 : 1;
                        }
                        /*if (nb_carte_wagon <= 2 && nb_carte_bateau <= 2
                        && nb_carte_wagon + nb_carte_bateau  + cartesTransport.stream().filter(c -> c.getType().equals(JOKER)).toList().size() >= 4)
                        {
                            return true;
                        }*/

                        if (nb_carte_wagon >= 2 && nb_carte_bateau >= 2) {
                            return true;
                        }
                        else if (cartesTransport.stream().filter(c -> c.getType().equals(JOKER)).toList().size() >= 4) {
                            return true;
                        }

                        else if (nb_carte_wagon <= 2
                        && nb_carte_wagon +  cartesTransport.stream().filter(c -> c.getType().equals(JOKER)).toList().size() >= 4
                        || nb_carte_bateau <= 2
                        && nb_carte_bateau + cartesTransport.stream().filter(c -> c.getType().equals(JOKER)).toList().size() >= 4) {
                            return true;
                        }
                        else if (nb_carte_wagon == 1 && nb_carte_bateau == 1 &&
                                    nb_carte_wagon + nb_carte_bateau + cartesTransport.stream().filter(c -> c.getType().equals(JOKER)).toList().size() >= 4) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public void poserPort(Ville ville) {


        if (peutPoserPort(ville, cartesTransport)) {
            log("caca prout");
            int nb_carte_wagon = 0;
            int nb_carte_bateau = 0;

            boolean good = false;

            while (!good) {
                log("caca");

                List<CarteTransport> choix_carte_possible = cartesTransport.stream().filter(CarteTransport::getAncre).toList();

                log(choix_carte_possible.toString());

                String choix_carte = choisir(
                        "Veuillez choisir les cartes à défausser pour poser votre Port",
                        choix_carte_possible.stream().map(CarteTransport::getNom).toList(),
                        List.of(new Bouton("Vider les cartes", "vd")),
                        false
                );

                if (choix_carte.equals("vd")) {
                    while (!cartesTransportPosees.isEmpty()) {
                        cartesTransport.add(cartesTransportPosees.remove(0));
                    }
                    nb_carte_bateau = 0;
                    nb_carte_wagon = 0;
                }
                else {
                    for (CarteTransport carte : cartesTransport) {
                        log("prout");
                        if (choix_carte.equals(carte.getNom())) {
                            log("choix carte : " +choix_carte);
                            cartesTransportPosees.add(carte);
                            if (nb_carte_wagon == 1) {
                                nb_carte_wagon += carte.getType().equals(WAGON) || carte.getType().equals(JOKER) ? 1 : 0;
                                nb_carte_bateau += carte.estDouble() ? 2 : 1;
                            }
                            else if (nb_carte_bateau == 0 && carte.estDouble()) {
                                nb_carte_wagon += carte.getType().equals(WAGON) || carte.getType().equals(JOKER) ? 1 : 0;
                                nb_carte_bateau += carte.estDouble() ? 2 : 1;
                            }
                            else {
                                nb_carte_wagon += carte.getType().equals(WAGON) || carte.getType().equals(JOKER) ? 1 : 0;
                                nb_carte_bateau += carte.estDouble() ? 2 : 1;
                            }
                            cartesTransport.remove(carte);
                            break;
                            /*if (nb_carte_wagon == 2 && nb_carte_bateau == 2) {
                                break;
                            }*/
                        }
                    }
                }

                boolean couleurUniq = true;
                if (!cartesTransportPosees.isEmpty()) {
                    Couleur firstCouleur = cartesTransportPosees.get(0).getCouleur();
                    couleurUniq = cartesTransportPosees.stream().filter(c -> c.getType() != JOKER).filter(c -> c.getCouleur() != firstCouleur).toList().size() == 0;
                }
                int nbWagons = cartesTransportPosees.stream().filter(c -> c.getType() == WAGON).toList().size();
                Optional<Integer> opt = cartesTransportPosees.stream().filter(c -> c.getType() == BATEAU).map(c -> c.estDouble() ? 2 : 1).reduce(Integer::sum);
                int nbBateau = opt.orElse(0);
                int nbJoker = cartesTransportPosees.stream().filter(c -> c.getType() == JOKER).toList().size();

                log(String.format("nbWagon = %d, nbBateau = %d, nbJoker = %d, couleurUniq = %b, somme = %d", nbWagons, nbBateau, nbJoker, couleurUniq, (nbBateau + nbBateau + nbJoker)));

                good = couleurUniq && nbWagons <= 2 && nbBateau <= 2 && ((nbWagons + nbBateau + nbJoker) == 4);
            }
            while (!this.getCartesTransportPosees().isEmpty()) {
                jeu.defausserCarteTransport(this.getCartesTransportPosees().remove(0));

            }
            this.getPorts().add(ville);
            jeu.getPortsLibres().remove(ville);
            //jeu.getPilesDeCartesWagon().getPileDefausse().addAll(cartesTransportPosees.stream().filter(c -> c.getType().equals(WAGON) || c.getType().equals(JOKER)).toList());
            //jeu.getPilesDeCartesBateau().getPileDefausse().addAll(cartesTransportPosees.stream().filter(c -> c.getType().equals(BATEAU)).toList());
        }
    }



    // Refaire PiocherCarteTransport de manière a ce que le joueur pioche une carte qui fais partie des options
    // et que la suppresion de cette carte dans la bonne pile se fasse dans un for avec des comparaison
    public void piocherCarteTransport(Bouton b) {

        // On vérifie que chaque pioche n'est pas vide afin de ne pas faire pioche une carte qui n'existerait pas
        ArrayList<CarteTransport> options_piocheCarteTrasnport_possible = new ArrayList<>();



        int nb_carte_choisi = 0;

        if (!(jeu.getCartesTransportVisibles().isEmpty())) {
            log("visible");
            for (CarteTransport carte : jeu.getCartesTransportVisibles()) {
                options_piocheCarteTrasnport_possible.add(carte);
            }
        }


        while (!(nb_carte_choisi == 2)) {

            if (b.valeur().equals("")) {
                nb_carte_choisi++;
            }

            if (!(jeu.getCartesTransportVisibles().isEmpty())) {
                log("visible");
                for (CarteTransport carte : jeu.getCartesTransportVisibles()) {
                    options_piocheCarteTrasnport_possible.add(carte);
                }
            }

            if (!jeu.getPilesDeCartesWagon().estVide()) {
                log("wagons");
                for (int i = 0; i < 1; i++) {
                    options_piocheCarteTrasnport_possible.add(jeu.getPilesDeCartesWagon().getPilePioche().get(i));
                }
            }

            if (!jeu.getPilesDeCartesBateau().estVide()) {
                log("bateaux");
                for (int i = 0; i < 1; i++) {
                    options_piocheCarteTrasnport_possible.add(jeu.getPilesDeCartesBateau().getPilePioche().get(i));
                }
            }

            if (nb_carte_choisi == 1) {

                ArrayList<Bouton> boutonsChoixWagons = new ArrayList<>();

                if (!jeu.getPilesDeCartesWagon().estVide()) {
                    boutonsChoixWagons.add(new Bouton("WAGON"));
                }
                if (!jeu.getPilesDeCartesBateau().estVide()) {
                    boutonsChoixWagons.add(new Bouton("BATEAU"));
                }

                // Idée provisoire est de créer des boutons piocher carte wagons et piocher carte bateaux
                String choix_carteTransport = choisir(
                        "Veuillez cliquez sur les cartes que vous souhaitez choisir",
                        options_piocheCarteTrasnport_possible.stream().map(c -> c.getNom()).toList(),
                        boutonsChoixWagons,
                        true
                );

                if (choix_carteTransport.equals("")) {
                    return;
                }

                else if (!choix_carteTransport.equals("") && !choix_carteTransport.equals("WAGON") && !choix_carteTransport.equals("BATEAU")) {
                    // On vérifie que le choix se trouve parmis les cartes visbles
                    log("" + cartesTransport.size());
                    log("choix : " +choix_carteTransport);
                    for (CarteTransport carte: jeu.getCartesTransportVisibles()) {
                        if (choix_carteTransport.equals(carte.getNom())) {
                            if (jeu.getPilesDeCartesWagon().estVide() && jeu.getPilesDeCartesBateau().estVide()) {
                                cartesTransport.add(carte);
                                options_piocheCarteTrasnport_possible.remove(carte);
                                jeu.updateCarteTransportVisivbles(carte);
                                nb_carte_choisi += carte.getType().equals(JOKER) ? 2 : 1;
                            }
                            else if (nb_carte_choisi == 1 && carte.getType() == JOKER) {
                                log("Vous ne pouvez choisir cette carte");
                                break;
                            }
                            else {
                                log("carte : " + choix_carteTransport);
                                cartesTransport.add(carte);
                                //jeu.getCartesTransportVisibles().remove(carte);
                                options_piocheCarteTrasnport_possible.remove(carte);
                                jeu.updateCarteTransportVisivbles(carte);
                                nb_carte_choisi += carte.getType().equals(JOKER) ? 2 : 1;
                                break;
                            }
                        }
                        log("" + cartesTransport.size());
                    }
                }

                else if (choix_carteTransport.equals("WAGON")) {
                    log("Vous avez choisi de piocher une carte Wagons dans la pioche");
                    cartesTransport.add(jeu.getPilesDeCartesWagon().piocher());
                    nb_carte_choisi++;
                }
                else if (choix_carteTransport.equals("BATEAU")) {
                    log("Vous avez choisi de piocher une carte bateau dans la pioche");
                    cartesTransport.add(jeu.getPilesDeCartesBateau().piocher());
                    nb_carte_choisi++;
                }
            }
            // Idée provisoire est de créer des boutons piocher carte wagons et piocher carte bateaux

            else if (!b.valeur().equals("") && !b.valeur().equals("WAGON") && !b.valeur().equals("BATEAU")) {
                    // On vérifie que le choix se trouve parmis les cartes visbles
                    log("" + cartesTransport.size());

                    for (CarteTransport carte: jeu.getCartesTransportVisibles()) {
                        log("choix valeur : " +b.valeur() + "carte valeur " + carte.getNom());
                        if (b.valeur().equals(carte.getNom())) {
                            if (jeu.getPilesDeCartesWagon().estVide() && jeu.getPilesDeCartesBateau().estVide()) {
                                cartesTransport.add(carte);
                                options_piocheCarteTrasnport_possible.remove(carte);
                                jeu.updateCarteTransportVisivbles(carte);
                                if (jeu.getCartesTransportVisibles().size() == 1) {
                                    nb_carte_choisi += 2;
                                }
                                else {
                                    nb_carte_choisi += carte.getType().equals(JOKER) ? 2 : 1;
                                }
                            }
                            else if (nb_carte_choisi == 1 && carte.getType() == JOKER) {
                                log("Vous ne pouvez choisir cette carte");
                                break;
                            }
                            else {
                                log("carte : " + b.valeur());
                                cartesTransport.add(carte);
                                //jeu.getCartesTransportVisibles().remove(carte);
                                options_piocheCarteTrasnport_possible.remove(carte);
                                jeu.updateCarteTransportVisivbles(carte);
                                nb_carte_choisi += carte.getType().equals(JOKER) ? 2 : 1;
                                break;
                            }
                        }
                        log("" + cartesTransport.size());
                    }
                }
            else if (b.valeur().equals("WAGON") && !jeu.getPilesDeCartesWagon().estVide()) {
                log("Vous avez choisi de piocher une carte Wagons dans la pioche");
                cartesTransport.add(jeu.getPilesDeCartesWagon().piocher());
                nb_carte_choisi++;
            }
            else if (b.valeur().equals("BATEAU") && !jeu.getPilesDeCartesBateau().estVide()) {
                log("Vous avez choisi de piocher une carte bateau dans la pioche");
                cartesTransport.add(jeu.getPilesDeCartesBateau().piocher());
                nb_carte_choisi++;
            }
        }
    }


    public void echangerPions(Bouton b) {

        ArrayList<String> options_choix_wagon = new ArrayList<>();

        ArrayList<String> options_choix_bateau = new ArrayList<>();

        for (int i = 1; i <= nbPionsWagonEnReserve; i++) {
            options_choix_wagon.add(String.valueOf(i));
        }
        for (int i = 1; i <= nbPionsBateauEnReserve; i++) {
            options_choix_bateau.add(String.valueOf(i));
        }

        HashSet<String> option_wagon = new HashSet<>(options_choix_wagon);
        HashSet<String> options_bateau= new HashSet<>(options_choix_bateau);



        boolean verif_wagons  = true;
        boolean verif_bateaux = true;

        if (b.valeur().equals("PIONS WAGON")) {
            while (verif_wagons) {

                String choix_wagons = choisir(
                        "Veuillez entrez le nombre wagons que vous souhaitez echanger",
                        options_choix_wagon,
                        null,
                        false
                );

                for (String s : options_choix_wagon) {
                    log("option choix " + options_choix_wagon);
                    if (choix_wagons.equals(s)) {
                        this.nbPionsBateauEnReserve += Integer.parseInt(s);
                        this.nbPionsBateau -= Integer.parseInt(s);
                        this.nbPionsWagonEnReserve -= Integer.parseInt(s);
                        this.nbPionsWagon += Integer.parseInt(s);
                        this.score -= Integer.parseInt(s);
                        verif_wagons = false;
                        break;
                    }
                }
            }
        }
        else if (b.valeur().equals("PIONS BATEAU")) {
            while (verif_bateaux) {
                String choix_bateaux = choisir(
                        "Veuillez entrez le nombre bateaux que vous souhaitez echanger",
                        options_choix_bateau,
                        null,
                        false
                );
                for (String s : options_choix_bateau) {
                    log("option choix " + options_choix_bateau);
                    if (choix_bateaux.equals(s)) {
                        this.nbPionsBateau += Integer.parseInt(s);
                        this.nbPionsBateauEnReserve -= Integer.parseInt(s);
                        this.nbPionsWagon -= Integer.parseInt(s);
                        this.nbPionsWagonEnReserve += Integer.parseInt(s);
                        this.score -= Integer.parseInt(s);
                        verif_bateaux = false;
                        break;
                    }
                }
            }
        }
    }

    public boolean peutPrendreRoute(Route r, List<CarteTransport> cartes) {

        int longueur_route = r.getLongueur();
        TypeCarteTransport t1 = r.estMaritime() ? TypeCarteTransport.BATEAU : TypeCarteTransport.WAGON;
        int cpt = 0;

        if (r.estPaire()) {
            return peutPrendreRoutePaire(r, cartes);
        }

        if ((r.estMaritime() ? this.nbPionsBateau : this.nbPionsWagon) < longueur_route) {
            return false;
        }

        if (r.getCouleur().equals(Couleur.GRIS)) {
            if (this.nb_Carte_max_M_couleur(cartes, r.estMaritime() ? TypeCarteTransport.BATEAU : TypeCarteTransport.WAGON) < longueur_route) {
                return false;
            }
            else {
                return true;
            }
        }

        for (CarteTransport carte : cartes) {
            if (carte.getType() == TypeCarteTransport.JOKER) {
                cpt++;
            }
            else if (carte.getCouleur().equals(r.getCouleur()) && carte.getType().equals(t1)) {
                cpt += carte.estDouble() ? 2 : 1;
            }
        }
        return cpt>= longueur_route; //caca
    }

    public boolean peutPrendreRoutePaire(Route r, List<CarteTransport> cartes) {

        int cpt_paire = 0;
        int cpt_nb_couleur_impair = 0;
        int nb_Joker = cartes.stream().filter(c -> c.getType().equals(JOKER)).toList().size();

        for (Couleur c : Arrays.stream(Couleur.values()).filter(c -> c != Couleur.GRIS).toList()) {
            int nb_Carte_couleur_courante = cartes.stream().filter(z -> z.getType().equals(WAGON) && z.getCouleur().equals(c)).toList().size();
            cpt_paire += nb_Carte_couleur_courante / 2;
            cpt_nb_couleur_impair += nb_Carte_couleur_courante % 2;
        }
        cpt_paire += Math.min(nb_Joker, cpt_nb_couleur_impair);

        cpt_paire += (nb_Joker - Math.min(nb_Joker, cpt_nb_couleur_impair)) / 2;

        return cpt_paire >= r.getLongueur();
    }

    public int paire_carteRoute_Paire(List<CarteTransport> cartes) {

        int cpt_paire = 0;
        int cpt_nb_couleur_impair = 0;
        int nb_Joker = cartes.stream().filter(c -> c.getType().equals(JOKER)).toList().size();

        for (Couleur c : Arrays.stream(Couleur.values()).filter(c -> c != Couleur.GRIS).toList()) {
            int nb_Carte_couleur_courante = cartes.stream().filter(z -> z.getType().equals(WAGON) && z.getCouleur().equals(c)).toList().size();
            cpt_paire += nb_Carte_couleur_courante / 2;
            cpt_nb_couleur_impair += nb_Carte_couleur_courante % 2;
        }
        cpt_paire += Math.min(nb_Joker, cpt_nb_couleur_impair);

        cpt_paire += (nb_Joker - Math.min(nb_Joker, cpt_nb_couleur_impair)) / 2;

        return cpt_paire;
    }
    public List<Couleur> peutAcheterRoutePaire(Route r, List<CarteTransport> cartes) {
        int cpt_paire = 0;
        int cpt_nb_couleur_impair = 0;
        int nb_Joker = cartes.stream().filter(c -> c.getType().equals(JOKER)).toList().size();

        ArrayList<Couleur> couleur_possible_joueur = new ArrayList<>();

        ArrayList<Couleur> couleur_possible = new ArrayList<>();

        for (CarteTransport c : cartesTransport) {
            if (!couleur_possible.contains(c.getCouleur())) {
                couleur_possible.add(c.getCouleur());
            }
        }


        Couleur couleurCourante = null;

        for (Couleur couleur : couleur_possible_joueur) {
            for (Couleur c : Arrays.stream(Couleur.values()).filter(c -> c != Couleur.GRIS).toList()) {
                if (couleur.equals(c)) {
                    //log(("Couleur courante" +couleurCourante));
                    couleurCourante = c;
                    int nb_Carte_couleur_courante = cartes.stream().map(z -> z.estDouble() ? 2 : 1).reduce(Integer::sum).orElse(0);
                    cpt_paire += nb_Carte_couleur_courante / 2;
                    cpt_nb_couleur_impair += nb_Carte_couleur_courante % 2;
                }
            }
            cpt_paire += Math.min(nb_Joker, cpt_nb_couleur_impair);

            cpt_paire += (nb_Joker - Math.min(nb_Joker, cpt_nb_couleur_impair)) / 2;

            if (cpt_paire >= r.getLongueur()) {
                couleur_possible.add(couleurCourante);
            }
        }

        return couleur_possible;
    }

    public void créditer_Route(Route r) {
        if (r.estMaritime()) {
            this.nbPionsBateau -= r.getLongueur();
        }
        else {
            this.nbPionsWagon -= r.getLongueur();
        }
    }

    public int nb_Carte_max_M_couleur(List<CarteTransport> cartes, TypeCarteTransport type) {
        int compteur_max = 0;
        for (Couleur c : Couleur.values()) {
            int compteur = 0;
            for (int i = 0; i < cartes.toArray().length; i++) {
                if (cartes.get(i).getCouleur().equals(c) && !cartes.get(i).getCouleur().equals(Couleur.GRIS) && (type == null || cartes.get(i).getType().equals(type))) {
                    compteur += cartes.get(i).estDouble() ? 2 : 1;
                }
            }
            if (compteur > compteur_max) {
                compteur_max = compteur;
            }
        }
        return compteur_max + (cartes.stream().filter(c -> c.getType() == JOKER).toList().size());
    }

    public boolean uniqueCouleur_for_RouteGrise(List<CarteTransport> cartes) {
        List<CarteTransport> carteTransport_sans_Joker = cartes.stream().filter(c -> c.getType() !=JOKER).toList();
        boolean res = carteTransport_sans_Joker.stream().filter(c -> c.getCouleur() == carteTransport_sans_Joker.get(0).getCouleur()).toList().equals(carteTransport_sans_Joker);
        log("bool : " + res);
        return res;
    }

    public List<Couleur> peutAcheterRouteGrise(TypeCarteTransport type, Route r) {
        int nb_Joker = cartesTransport.stream().filter(c -> c.getType().equals(JOKER)).toList().size();

        List<Couleur> couleur_possible = new ArrayList<>();

        for (Couleur c: Arrays.stream(Couleur.values()).filter(c -> c != Couleur.GRIS).toList()) {
            int cpt = 0;
            for (CarteTransport carte : cartesTransport) {
                if (carte.getCouleur().equals(c) && carte.getType().equals(type)) {
                    cpt += carte.estDouble() ? 2 : 1;
                }
            }
            if (cpt + nb_Joker >= r.getLongueur()) {
                couleur_possible.add(c);
            }
        }
        return couleur_possible;
    }



    public void prendreRoutePaire(Route r) {

        while (paire_carteRoute_Paire(cartesTransportPosees) < r.getLongueur()) {
            ArrayList<String> choix_possible = new ArrayList<>();

            for (CarteTransport carte : cartesTransport) {
                List<CarteTransport> copy_CarteTransport = new ArrayList<>(cartesTransport);
                List<CarteTransport> copy_carteTransportPoseees = new ArrayList<>(cartesTransportPosees);
                copy_CarteTransport.remove(carte);
                copy_carteTransportPoseees.add(carte);

                if (paire_carteRoute_Paire(cartesTransport) > paire_carteRoute_Paire(copy_CarteTransport)) {
                    choix_possible.add(carte.getNom());
                }
                else if (paire_carteRoute_Paire(copy_carteTransportPoseees) > paire_carteRoute_Paire(cartesTransportPosees)) {
                    choix_possible.add(carte.getNom());
                }
            }
            String choix =  choisir(
                    "Choisissez vos cartes",
                    choix_possible,
                    null,
                    false
            );

            for (CarteTransport carte : cartesTransport) {
                if (carte.getNom().equals(choix)) {
                    cartesTransportPosees.add(carte);
                    cartesTransport.remove(carte);
                    break;
                }
            }
        }
        while (!cartesTransportPosees.isEmpty()) {
            jeu.getPilesDeCartesWagon().getPileDefausse().add(cartesTransportPosees.remove(0));
        }

        switch (r.getLongueur()){
                case 1 -> {
                    this.score++;
                }
                case 2 -> {
                    this.score += 2;
                }
                case 3 -> {
                    this.score += 4;
                }
                case 4 -> {
                    this.score += 7;
                }
                case 5 ->  {
                    this.score += 10;
                }
                case 6 -> {
                    this.score += 15;
                }
                case 7 -> {
                    this.score += 18;
                }
                case 8 -> {
                    this.score += 21;
                }

        }
        routes.add(r);
        jeu.getRoutesLibres().remove(r);
    }

    public void prendreRoute(Route r) {

        boolean verif_choix_route = true;
        boolean verif_choix_cartes_actuelle = true;
        boolean verif_choix_cartes = true;

        ArrayList<String> choix_routes_libres = new ArrayList<>();

        if (r.estPaire()) {
            prendreRoutePaire(r);
            return;
        }
        while (verif_choix_route) {
            for (int i = 0; i < jeu.getRoutesLibres().toArray().length; i++) {
                log("Route choisie = " + r);
                if (r.getNom().equals(jeu.getRoutesLibres().get(i).getNom())) {
                        log("Route choisie = " + r);
                        // On a trouver la route sur laquelle la personne à cliquer

                        Couleur first_color = null;
                        List<Couleur> couleur_possible = this.peutAcheterRouteGrise(r.estMaritime() ? BATEAU : WAGON, r);
                        while (cartesTransportPosees.isEmpty() || !(cartesTransportPosees.stream().map(c -> c.estDouble() ? 2 : 1).reduce((a, b) -> a + b).get() >= r.getLongueur())) {

                            ArrayList<CarteTransport> choix_cartes_possibles = new ArrayList<>();


                            for (CarteTransport carte : cartesTransport) {
                                log("couleur possible" +couleur_possible);
                                if (carte.getType().equals(JOKER) || (couleur_possible.contains(carte.getCouleur()) && carte.getType().equals(r.estMaritime() ? BATEAU : WAGON))) {
                                    if (first_color == null //|| carte.getType().equals(JOKER)
                                            || carte.getCouleur().equals(first_color) || carte.getType().equals(JOKER)) {
                                        choix_cartes_possibles.add(carte);
                                    }
                                }
                            }
                            // On créer une liste des choix possible de cartes bateaux étant donné que la route est maritime

                            log(choix_cartes_possibles.stream().map(c -> c.getNom()).toList().toString());

                            String choix_carte_courante = this.choisir(
                                    "Veuillez choisir la carte à poser",
                                    choix_cartes_possibles.stream().map(c -> c.getNom()).toList(),
                                    List.of(new Bouton("Vider cartes posées")),
                                    false
                            );

                            if (choix_carte_courante.equals("Vider cartes posées")) {
                                first_color = null;
                                while (!this.getCartesTransportPosees().isEmpty()) {
                                    this.getCartesTransport().add(this.getCartesTransportPosees().remove(0));
                                }
                            }
                            else {
                                for (CarteTransport carte : choix_cartes_possibles) {
                                    if (choix_carte_courante.equals(carte.getNom())) {
                                        if (!carte.getType().equals(JOKER)) {
                                            first_color = carte.getCouleur();
                                        }
                                        this.getCartesTransportPosees().add(carte);
                                        this.getCartesTransport().remove(carte);
                                        break;
                                    }
                                }
                            }
                        }


                    // Cela ce passe une fois que la route à été prise par le joueur
                    while (!this.getCartesTransportPosees().isEmpty()) {
                        jeu.defausserCarteTransport(this.getCartesTransportPosees().remove(0));
                    }
                    this.getRoutes().add(jeu.getRoutesLibres().get(i));
                    jeu.getRoutesLibres().remove(jeu.getRoutesLibres().get(i));
                    this.créditer_Route(jeu.getRoutesLibres().get(i));
                    verif_choix_route = false;
                    break;
                }
            }
        }
        if (!verif_choix_route) {
            switch (r.getLongueur()){
                case 1 -> {
                    this.score++;
                }
                case 2 -> {
                    this.score += 2;
                }
                case 3 -> {
                    this.score += 4;
                }
                case 4 -> {
                    this.score += 7;
                }
                case 5 ->  {
                    this.score += 10;
                }
                case 6 -> {
                    this.score += 15;
                }
                case 7 -> {
                    this.score += 18;
                }
                case 8 -> {
                    this.score += 21;
                }
            }
        }
    }



    public void piocherDestination() {

        boolean verification = true;
        int nb_supp_destination = 0;


        ArrayList<Destination> optionsDestinations = new ArrayList<>();
        ArrayList<Bouton> boutonsDestinations = new ArrayList<>();

        // Initialisation des boutons et des choix de Destinations à supprimer pour chaque joueur
        for (int i = 0; i < 4; i++) {
            Destination courantDestination = this.piocherDestination_base();
            optionsDestinations.add(courantDestination);
            boutonsDestinations.add(new Bouton(courantDestination.toString(), courantDestination.getNom()));
            this.getDestinations().add(courantDestination);
        }

        // Selection des trois destinations
        while (verification) {

            String choix = this.choisir(
                    "Choisissez les villes que vous souhaitez supprimer",
                    Collections.singleton(String.valueOf(optionsDestinations)),
                    boutonsDestinations,
                    true);
            // Le joueur ne choisi pas cette carte et décide de la reposer au fond du paquet
            if (choix.equals("")) {
                log(String.format("Vous avez choisis les villes"));
                verification = false;
            }
            //
            //String.valueOf(optionsDestinations.get(est_contenu)).equals(choix)

            // Le joueur choisi la carte qu'il vient alors de piocher
            else {
                for (Destination d : optionsDestinations) {
                    if ((d.getNom().equals(choix))) {
                        log(String.format("%s a choisi de supprimer %s, %s", this.toLog(), choix, (d.toString())));
                        jeu.getPileDestinations().add(d);
                        this.getDestinations().remove(d);
                        log("le choix destination est :" + choix);
                        for (Bouton b : boutonsDestinations) {
                            log("bouton valeur : " + b.valeur() + " destination valeur : " + d.getNom());
                            if (b.valeur().equals(d.getNom())) {
                                boutonsDestinations.remove(b);
                                break;
                            }
                        }
                    }
                }
                nb_supp_destination++;

                if (nb_supp_destination == 3) {
                    log("option Destiantion" + optionsDestinations);
                    break;
                }
            }
        }
        /*while (!boutonsDestinations.isEmpty()) {
            for (Bouton b  : boutonsDestinations) {
                for (Destination d : optionsDestinations) {
                    if (b.valeur().equals(d.getNom())) {
                        destinations.add(d);
                        return;
                    }
                }
            }
        }*/
    }


    public Destination piocherDestination_base() {
        // On suppose que la pioche n'est pas vide
        return jeu.getPileDestinations().remove(0);
    }

    /**
     * Cette méthode est appelée à tour de rôle pour chacun des joueurs de la partie.
     * Elle doit réaliser un tour de jeu, pendant lequel le joueur a le choix entre 5 actions possibles :
     *  - piocher des cartes transport (visibles ou dans la pioche)
     *  - échanger des pions wagons ou bateau
     *  - prendre de nouvelles destinations
     *  - capturer une route
     *  - construire un port
     */
    // Tour d'initialisation
    void jouerTour() {
        // IMPORTANT : Le corps de cette fonction est à réécrire entièrement
        // Un exemple très simple est donné pour illustrer l'utilisation de certaines méthodes

        boolean action_valide = true;

        // Initialisation des option de jeux pour le joueur Courant
        ArrayList<String> option_choix_de_jeu = new ArrayList<>();
        ArrayList<Bouton> boutons_choix_de_jeu = new ArrayList<>();

        if (!jeu.getPilesDeCartesWagon().estVide()) {
            option_choix_de_jeu.add("WAGON");
        }

        if (!jeu.getPilesDeCartesBateau().estVide()) {
            option_choix_de_jeu.add("BATEAU");
        }


        if (!jeu.getPileDestinations().isEmpty()) {
            option_choix_de_jeu.add("DESTINATION");
        }

        if (this.nbPionsWagonEnReserve > 0 || this.nbPionsBateauEnReserve > 0) {
            option_choix_de_jeu.add("PIONS WAGON");
            option_choix_de_jeu.add("PIONS BATEAU");
        }

        // Créer les boutons qui donne les manières pour le joueurCourant de jouer le tour actuelle
        for (int i = 0; i < option_choix_de_jeu.toArray().length; i++) {
            boutons_choix_de_jeu.add(new Bouton(option_choix_de_jeu.get(i)));
        }

        // On créer les options de routes libre pour choisir
        for (int i = 0; i < jeu.getRoutesLibres().toArray().length; i++) {
            if (this.peutPrendreRoute(jeu.getRoutesLibres().get(i), this.getCartesTransport())) {
                option_choix_de_jeu.add(jeu.getRoutesLibres().get(i).getNom());
            }
        }
        for (Ville v : jeu.getPortsLibres()) {
            option_choix_de_jeu.add(v.nom());
        }

        option_choix_de_jeu.addAll(jeu.getCartesTransportVisibles().stream().map(c -> c.getNom()).toList());

        String choix = choisir(
                "Vous avez le choix de faire différence action qui seront représentés par les boutons ci-dessous",
                option_choix_de_jeu,
                boutons_choix_de_jeu,
                false
        );

        switch (choix) {

            case "DESTINATION" -> {
                piocherDestination();
            }

            case "WAGON" -> {
                if (!jeu.getPilesDeCartesBateau().estVide()) {
                    piocherCarteTransport(new Bouton("WAGON"));
                }
                else {
                    piocherCarteTransport(new Bouton(choix));
                }
            }

            case "BATEAU" -> {
                if (!jeu.getPilesDeCartesWagon().estVide()) {
                    piocherCarteTransport(new Bouton("BATEAU"));
                }
                else {
                    piocherCarteTransport(new Bouton(choix));
                }
            }

            case "PIONS WAGON" -> {
                echangerPions(new Bouton("PIONS WAGON"));

            }
            case "PIONS BATEAU" -> {
                    echangerPions(new Bouton("PIONS BATEAU"));
            }
        }

        for (CarteTransport carte : jeu.getCartesTransportVisibles()) {
           if (choix.equals(carte.getNom())) {
               jeu.addInput(choix);
               log("choix carte visible :" + choix);
               piocherCarteTransport(new Bouton(choix));
           }
       }

        for (Ville v : jeu.getPortsLibres()) {
            if (choix.equals(v.nom())) {
                poserPort(v);
                return;
                }
            }
        for (Route r : jeu.getRoutesLibres()) {
            if (r.getNom().equals(choix)) {
                prendreRoute(r);
            }
        }




        // Si le choix actuel fais partie des boutons ben je fais la funtion du bouton




    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     *
     * Cette méthode lit les entrées du jeu (`Jeu.lireligne()`) jusqu'à ce
     * qu'un choix valide (un élément de `choix` ou de `boutons` ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     *
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     *
     * ```
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez-vous faire ceci ?", choix, null, false);
     * ```
     *
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     *
     * ```
     * List<Bouton> boutons = Arrays.asList(new Bouton("Un", "1"), new Bouton("Deux", "2"), new Bouton("Trois", "3"));
     * String input = choisir("Choisissez un nombre.", null, boutons, false);
     * ```
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de `Bouton` représentés par deux String (label,
     *                    valeur) correspondant aux choix valides attendus du joueur
     *                    qui doivent être représentés par des boutons sur
     *                    l'interface graphique (le label est affiché sur le bouton,
     *                    la valeur est ce qui est envoyé au jeu quand le bouton est
     *                    cliqué)
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élement de `choix`, ou la valeur
     * d'un élément de `boutons` ou la chaîne vide)
     */
    public String choisir(
            String instruction,
            Collection<String> choix,
            Collection<Bouton> boutons,
            boolean peutPasser) {
        if (choix == null)
            choix = new ArrayList<>();
        if (boutons == null)
            boutons = new ArrayList<>();

        HashSet<String> choixDistincts = new HashSet<>(choix);
        choixDistincts.addAll(boutons.stream().map(Bouton::valeur).toList());
        if (peutPasser || choixDistincts.isEmpty()) {
            choixDistincts.add("");
        }

        String entree;
        // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
        while (true) {
            jeu.prompt(instruction, boutons, peutPasser);
            entree = jeu.lireLigne();
            // si une réponse valide est obtenue, elle est renvoyée
            if (choixDistincts.contains(entree)) {
                return entree;
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Wagons: %d  Bateaux: %d", nbPionsWagon, nbPionsBateau));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    public int nbDestinationPasseParPort(Ville ports, ArrayList<Destination> d){
        int nbDestination = 0;
        // on parcourt toutes les destinations completes
        for (Destination dest:d
             ) {
            // on parcourt l'ArrayList<Ville> de chaque destination
            for (int i = 0; i < dest.getVilles().size(); i++) {
                //on verifie si la ville du port est bien présente dans l'ArrayList<Ville> de chaque destination
                if (ports.nom() == dest.getVilles().get(i)) {
                    // on incrémente notre compteur de destinations qui passe par la ville du port
                    nbDestination++;
                    // on arrête la boucle car la condition a besoin d'être passée une seule fois
                    break;
                }
            }
        }
        return nbDestination;
    }

    public ArrayList<Ville> genererVilleFille(Ville v){
        ArrayList<Ville> villeFille = new ArrayList<>();
        for (Route r:routes
             ) {
            if (r.getVille1().equals(v) && !villeFille.contains(r.getVille2())){
                villeFille.add(r.getVille2());
            } else if (r.getVille2().equals(v)&& !villeFille.contains(r.getVille1())) {
                villeFille.add(r.getVille1());
            }
        }
        return villeFille;
    }

    public void mettreAjour(ArrayList<Ville> frontiere, ArrayList<Ville> dejaVus, Ville v){
        ArrayList<Ville> listeFille = genererVilleFille(v);
        for (Ville villeFille:listeFille
             ) {
            if (!dejaVus.contains(villeFille)) {
                frontiere.add(villeFille);
                dejaVus.add(villeFille);
            }
        }
    }


    boolean destinationEstComplete(Destination d) {
        // Cette méthode pour l'instant renvoie false pour que le jeu puisse s'exécuter.
        // À vous de modifier le corps de cette fonction pour qu'elle retourne la valeur attendue.

        // Vérifier pour chaque route du joueur si il possède les ville qui forment la destination d
        // On vérifie jusqu'a ce qu'une ville manque sinon on renvoie true
        ArrayList<Ville> dejaVus = new ArrayList<>();
        ArrayList<Ville> frontiere = new ArrayList<>();
        ArrayList<Ville> villeDestination = new ArrayList<>();
        for (String villeDest:d.getVilles()
             ) {
            for (Route r:routes
            ) {
                if (r.getVille1().nom()==villeDest && !villeDestination.contains(r.getVille1())){
                    villeDestination.add(r.getVille1());
                } else if (r.getVille2().nom()==villeDest && !villeDestination.contains(r.getVille2())) {
                    villeDestination.add(r.getVille2());
                }
            }
        }
        if (villeDestination.size()<d.getVilles().size()){
            return false;
        }
        dejaVus.add(villeDestination.get(0));
        mettreAjour(frontiere,dejaVus,villeDestination.get(0));
        while(!dejaVus.containsAll(villeDestination) && !frontiere.isEmpty()){
            Ville villeRetirée = frontiere.remove(0);
            mettreAjour(frontiere, dejaVus, villeRetirée);
        }
        if (dejaVus.containsAll(villeDestination)){
            return true;
        }
        return false;
    }

    public int calculerScoreFinal() {

        // Ajoute la valeur simple si la destination est complete sinon cela soustrait la pénalité
        int scoreFinal = score;
        for (Destination d : destinations
        ) {
            if (destinationEstComplete(d)) {
                scoreFinal = scoreFinal + d.getValeurSimple();
            } else {
                scoreFinal = scoreFinal - d.getPenalite();
            }
        }

        // prend le nombre de ports non construits et soustrait le score de 4 points pour chaque port non construit
        int nbPortNonConstruit = 3 - ports.size();
        for (int i = 0; i < nbPortNonConstruit; i++) {
            scoreFinal = scoreFinal - 4;
        }
        // créer une ArrayList de toutes les destinations complètes
        ArrayList<Destination> destinationsComplete = new ArrayList<>();
        for (Destination d2 :
                destinations) {
            if (destinationEstComplete(d2)) {
                destinationsComplete.add(d2);
            }
        }
        // vérifie pour chaque port construit le nb de destinations qui passe dessus et ajoute les points dans le score en conséquence
        for (Ville v : ports
        ) {
            int nbDestinationsPorts = nbDestinationPasseParPort(v, destinationsComplete);
            if (nbDestinationsPorts == 1) {
                scoreFinal = scoreFinal + 20;
            } else if (nbDestinationsPorts == 2) {
                scoreFinal = scoreFinal + 30;
            } else if (nbDestinationsPorts >= 3) {
                scoreFinal = scoreFinal + 40;
            }
        }
        return scoreFinal;
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un dictionnaire de
     * valeurs sérialisables
     * (qui sera converti en JSON pour l'envoyer à l'interface graphique)
     */
    Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("nom", nom),
                Map.entry("couleur", couleur),
                Map.entry("score", score),
                Map.entry("pionsWagon", nbPionsWagon),
                Map.entry("pionsWagonReserve", nbPionsWagonEnReserve),
                Map.entry("pionsBateau", nbPionsBateau),
                Map.entry("pionsBateauReserve", nbPionsBateauEnReserve),
                Map.entry("destinationsIncompletes",
                        destinations.stream().filter(d -> !destinationEstComplete(d)).toList()),
                Map.entry("destinationsCompletes", destinations.stream().filter(this::destinationEstComplete).toList()),
                Map.entry("main", cartesTransport.stream().sorted().toList()),
                Map.entry("inPlay", cartesTransportPosees.stream().sorted().toList()),
                Map.entry("ports", ports.stream().map(Ville::nom).toList()),
                Map.entry("routes", routes.stream().map(Route::getNom).toList()));
    }

    public static void main(String[] args) {

    }
}
