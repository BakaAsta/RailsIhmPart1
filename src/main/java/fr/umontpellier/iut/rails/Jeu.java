package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;
import fr.umontpellier.iut.rails.data.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static fr.umontpellier.iut.rails.data.TypeCarteTransport.*;

public class Jeu implements Runnable {
    /**
     * Liste des joueurs
     */
    private final List<Joueur> joueurs;

    /**
     * Le joueur dont c'est le tour
     */
    private Joueur joueurCourant;
    /**
     * Liste des villes disponibles sur le plateau de jeu
     */
    private final List<Ville> portsLibres;
    /**
     * Liste des routes disponibles sur le plateau de jeu
     */
    private final List<Route> routesLibres;
    /**
     * Pile de pioche et défausse des cartes wagon
     */
    private final PilesCartesTransport pilesDeCartesWagon;
    /**
     * Pile de pioche et défausse des cartes bateau
     */
    private final PilesCartesTransport pilesDeCartesBateau;
    /**
     * Cartes de la pioche face visible (normalement il y a 6 cartes face visible)
     */
    private final List<CarteTransport> cartesTransportVisibles;
    /**
     * Pile des cartes "Destination"
     */
    private final List<Destination> pileDestinations;
    /**
     * File d'attente des instructions recues par le serveur
     */
    private final BlockingQueue<String> inputQueue;
    /**
     * Messages d'information du jeu
     */
    private final List<String> log;

    private String instruction;

    private Collection<Bouton> boutons;


    public Jeu(String[] nomJoueurs) {
        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauMonde();
        portsLibres = plateau.getPorts();
        routesLibres = plateau.getRoutes();

        // création des piles de pioche et défausses des cartes Transport (wagon et
        // bateau)
        ArrayList<CarteTransport> cartesWagon = new ArrayList<>();
        ArrayList<CarteTransport> cartesBateau = new ArrayList<>();
        for (Couleur c : Couleur.values()) {
            if (c == Couleur.GRIS) {
                continue;
            }
            for (int i = 0; i < 4; i++) {
                // Cartes wagon simples avec une ancre
                cartesWagon.add(new CarteTransport(TypeCarteTransport.WAGON, c, false, true));
            }
            for (int i = 0; i < 7; i++) {
                // Cartes wagon simples sans ancre
                cartesWagon.add(new CarteTransport(TypeCarteTransport.WAGON, c, false, false));
            }
            for (int i = 0; i < 4; i++) {
                // Cartes bateau simples (toutes avec une ancre)
                cartesBateau.add(new CarteTransport(TypeCarteTransport.BATEAU, c, false, true));
            }
            for (int i = 0; i < 6; i++) {
                // Cartes bateau doubles (toutes sans ancre)
                cartesBateau.add(new CarteTransport(TypeCarteTransport.BATEAU, c, true, false));
            }
        }
        for (int i = 0; i < 14; i++) {
            // Cartes wagon joker
            cartesWagon.add(new CarteTransport(JOKER, Couleur.GRIS, false, true));
        }

        // Mélange des cartes Wagons

        Collections.shuffle(cartesWagon);
        pilesDeCartesWagon = new PilesCartesTransport(cartesWagon);

        // Mélange des cartes bateaux
        Collections.shuffle(cartesBateau);
        pilesDeCartesBateau = new PilesCartesTransport(cartesBateau);


        // création de la liste pile de cartes transport visibles
        // (les cartes seront retournées plus tard, au début de la partie dans run())
        cartesTransportVisibles = new ArrayList<>();

        // création des destinations
        pileDestinations = Destination.makeDestinationsMonde();
        Collections.shuffle(pileDestinations);

        // création des joueurs
        ArrayList<Joueur.CouleurJouer> couleurs = new ArrayList<>(Arrays.asList(Joueur.CouleurJouer.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();


        for (String pseudo : nomJoueurs) {
            joueurs.add(new Joueur(pseudo, this, couleurs.remove(0)));
        }
        this.joueurCourant = joueurs.get(0);

    }

    public PilesCartesTransport getPilesDeCartesWagon() {
        return pilesDeCartesWagon;
    }

    public PilesCartesTransport getPilesDeCartesBateau() {
        return pilesDeCartesBateau;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public List<Ville> getPortsLibres() {
        return new ArrayList<>(portsLibres);
    }

    public List<Route> getRoutesLibres() {
        return new ArrayList<>(routesLibres);
    }

    public List<CarteTransport> getCartesTransportVisibles() {
        return new ArrayList<>(cartesTransportVisibles);
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }

    public String getNom(String choix) {
        return choix;
    }

    // Permet au jour de choisir une carte parmis les 4 piocher, il seras obliger d'en prendre une et une seul


    // Permet de savoir si le joueur possède le nombre de CarteTransport de la Route actuelle
    public boolean player_has_theRight_CarteTansport(Route route, int longueur) {
        ArrayList<CarteTransport> carte_posee_possibilities = new ArrayList<>();

        ArrayList<Couleur> colors_possibilities = new ArrayList<>();
        colors_possibilities.add(Couleur.VERT);
        colors_possibilities.add(Couleur.JAUNE);
        colors_possibilities.add(Couleur.ROUGE);
        colors_possibilities.add(Couleur.VIOLET);
        colors_possibilities.add(Couleur.BLANC);
        colors_possibilities.add(Couleur.NOIR);

        int max_carte_couleur = 0;
        int compteur_max = 0;
        int compteur = 0;
        Couleur couleur_Max = null;

        int cpt = 0;

        if (route.getClass().equals("class fr.umontpellier.iut.rails.RouteMaritime")) {
            for (int i = 0; i < joueurCourant.getCartesTransport().toArray().length; i++) {
                if (route.getCouleur().equals(joueurCourant.getCartesTransport().get(i).getCouleur()) && joueurCourant.getCartesTransport().get(i).getType().equals(TypeCarteTransport.BATEAU)) {
                    cpt++;
                }
            }
        } else if (route.getClass().equals("class fr.umontpellier.iut.rails.RouteTerrestre")) {
            for (int i = 0; i < joueurCourant.getCartesTransport().toArray().length; i++) {
                if (route.getCouleur().equals(joueurCourant.getCartesTransport().get(i).getCouleur()) && joueurCourant.getCartesTransport().get(i).getType().equals(TypeCarteTransport.WAGON)) {
                    cpt++;
                }
            }
        } else if (route.getClass().equals("class fr.umontpellier.iut.rails.RoutePaire")) {
            // On vérifie pour chaque couleur le nombre de carte posséder par le joueur
            for (Couleur c : colors_possibilities) {
                for (int i = 0; i < joueurCourant.getCartesTransport().toArray().length; i++) {
                    if (joueurCourant.getCartesTransport().get(i).getCouleur().equals(c)) {
                        compteur++;
                    }
                }
                if (compteur > compteur_max) {
                    compteur_max = compteur;
                    couleur_Max = c;
                }
            }
            /*for (int i = 0; i < joueurCourant.getCartesTransport().toArray().length; i++) {
                Couleur couleur_courante = colors_possibilities.get(i);
                for(int j = 0; j < joueurCourant.getCartesTransport().toArray().length; j++) {
                    CarteTransport carte_courante = joueurCourant.getCartesTransport().get(j);
                    if (couleur_courante.equals(carte_courante.getCouleur())) {
                        compteur_max++;
                    }
                }
                if (compteur_max > max_carte_couleur) {
                    max_carte_couleur = compteur_max;
                    couleur_Max = couleur_courante;
                }
            }
            cpt = compteur_max;*/
        }

        if (cpt == longueur) {
            int compteur_carte_crediter = 0;
            while (compteur_carte_crediter != longueur) {
                for (int i = 0; i < joueurCourant.getCartesTransport().toArray().length; i++) {
                    if (joueurCourant.getCartesTransport().get(i).getCouleur().equals(couleur_Max)) {
                        carte_posee_possibilities.add(joueurCourant.getCartesTransport().get(i));
                    }
                }
            }
            return true;
        } else if (cpt >= longueur) {
            return true;
        } else if (cpt <= longueur) {
            // Chercher les cartes jokers pour combler les cartes de couleur manquante
            int différence = longueur - cpt;
            int compteur_carte_joker = 0;
            for (int i = 0; i < joueurCourant.getCartesTransport().toArray().length; i++) {
                if (joueurCourant.getCartesTransport().get(i).getType().equals(JOKER)) {
                    compteur_carte_joker++;
                }
            }
            if (cpt + compteur_carte_joker >= longueur) {
                return true;
            }

        }
        log("Vous ne disposez pas des cartes suffisantes pour prendre possesion de la destination actuelle");
        return false;
    }

    public void defausserDestination(Destination destination, ArrayList<Destination> d) {
        if (!d.isEmpty()) {
            joueurCourant.getDestinations().remove(destination);
        }
    }

    public void defausserCarteTransport(CarteTransport c) {
        if (c.getType().equals(TypeCarteTransport.BATEAU)) {
            pilesDeCartesBateau.getPileDefausse().add(c);
        } else if (c.getType().equals(TypeCarteTransport.WAGON) || c.getType().equals(JOKER)) {
            pilesDeCartesWagon.getPileDefausse().add(c);
        }
    }

    /**
     * Exécute la partie
     * <p>
     * C'est cette méthode qui est appelée pour démarrer la partie. Elle doit intialiser le jeu
     * (retourner les cartes transport visibles, puis demander à chaque joueur de choisir ses destinations initiales
     * et le nombre de pions wagon qu'il souhaite prendre) puis exécuter les tours des joueurs en appelant la
     * méthode Joueur.jouerTour() jusqu'à ce que la condition de fin de partie soit réalisée.
     */

    public void jouerTourInitialization() {

        for (Joueur j : joueurs) {

            joueurCourant = j;

            // Pioche des Cartes Wagons
            for (int i = 0; i < 3; i++) {
                joueurCourant.getCartesTransport().add(piocherCarteWagon());
                //pilesDeCartesWagon.getCartes().remove(0);
            }

            // Pioche des Cartes Bateaux
            for (int i = 0; i < 7; i++) {
                joueurCourant.getCartesTransport().add(piocherCarteBateau());
                //pilesDeCartesBateau.getCartes().remove(0);
            }


            boolean verification = true;
            int nb_supp_destination = 0;

            ArrayList<Destination> optionsDestinations = new ArrayList<>();
            ArrayList<Bouton> boutonsDestinations = new ArrayList<>();

            // Initialisation des boutons et des choix de Destinations à supprimer pour chaque joueur
            for (int i = 0; i < 5; i++) {
                Destination courantDestination = joueurCourant.piocherDestination_base();
                optionsDestinations.add(courantDestination);
                boutonsDestinations.add(new Bouton(courantDestination.toString(), courantDestination.getNom()));
                joueurCourant.getDestinations().add(courantDestination);
            }

            // Selection des trois destinations
            while (verification) {

                if (nb_supp_destination == 2) {
                    break;
                }
                String choix = joueurCourant.choisir(
                        "Choisissez les villes que vous souhaitez supprimer",
                        Collections.singleton(String.valueOf(optionsDestinations)),
                        boutonsDestinations,
                        true);
                // Le joueur ne choisi pas cette carte et décide de la reposer au fond du paquet
                if (choix.equals("")) {
                    log(String.format("Vous avez choisis les villes"));
                    verification = false;
                }
                //String.valueOf(optionsDestinations.get(est_contenu)).equals(choix)

                // Le joueur choisi la carte qu'il vient alors de piocher
                else {
                    for (Destination d : optionsDestinations) {
                        if ((d.getNom().equals(choix))) {
                            log(String.format("%s a choisi de supprimer %s, %s", joueurCourant.toLog(), choix, (d.toString())));
                            this.getPileDestinations().add(d);
                            joueurCourant.getDestinations().remove(d);
                            log("le choix caca est :" + choix);
                            for (Bouton b : boutonsDestinations) {
                                log("bouton caca : " + b.valeur() + " dedstination valeur : " + String.valueOf(d));
                                if (b.valeur().equals(d.getNom())) {
                                    boutonsDestinations.remove(b);
                                    break;
                                }
                            }
                        }
                    }
                    nb_supp_destination++;
                }
            }
            System.out.println(joueurCourant.getDestinations().size());
            System.out.println(joueurCourant.getDestinations());


            boolean check_pions_choice_wagons = true;

            // Initialization des la liste des pions wagons
            ArrayList<String> choix_nb_pions_wagons_possibilities = new ArrayList<>();
            for (int i = 10; i <= 25; i++) {
                choix_nb_pions_wagons_possibilities.add(String.valueOf(i));
            }


            int nb_pions_wagons = joueurCourant.getNbPionsWagon();

            boolean verif = true;

            while (verif) {
                log("Vous allez choisir les wagons en premier");
                ArrayList<Bouton> boutons_nb_wagons = new ArrayList<>();

                while (check_pions_choice_wagons) {
                    String choix = joueurCourant.choisir(
                            "Veuillez insérer le nombre de wagons souhaitez\n" +
                                    "20 wagons (conseillé)",
                            choix_nb_pions_wagons_possibilities,
                            boutons_nb_wagons,
                            true
                    );
                    for (int i = 0; i < choix_nb_pions_wagons_possibilities.toArray().length; i++) {
                        //choix.equals(String.valueOf(choix_nb_pions_wagons_possibilities.get(i)))
                        if (choix.equals(choix_nb_pions_wagons_possibilities.get(i))) {
                            joueurCourant.setNbPionsWagon(Integer.parseInt(choix_nb_pions_wagons_possibilities.get(i)));
                            joueurCourant.setNbPionsBateau(60 - joueurCourant.getNbPionsWagon());
                            check_pions_choice_wagons = false;
                            verif = false;
                        }
                    }
                }
            }
            joueurCourant.setNbPionsWagonEnReserve(joueurCourant.getNbPionsWagonEnReserve() - joueurCourant.getNbPionsWagon());
            joueurCourant.setNbPionsBateauEnReserve(joueurCourant.getNbPionsBateauEnReserve() - joueurCourant.getNbPionsBateau());
        }
    }

    // A refaire pour vérifier les condtions suivantes :
    // Si jamais on update un wagons piocher mais que la pile pioche Wagons et vide et que la defausse aussi il faudra aller piocher dans la pioche BAteaux
    public void updateCarteTransportVisivbles(CarteTransport c) {
        cartesTransportVisibles.remove(c);

        boolean verif_pioche = true;

        while (verif_pioche) {

            ArrayList<String> choix_pioche = new ArrayList<>();

            ArrayList<Bouton> boutons_pioche = new ArrayList<>();

            if (!pilesDeCartesWagon.estVide()) {
                choix_pioche.add("WAGON");
                boutons_pioche.add(new Bouton("WAGON"));
            }
            if (!pilesDeCartesBateau.estVide()) {
                choix_pioche.add("BATEAU");
                boutons_pioche.add(new Bouton("BATEAU"));
            }
            if (pilesDeCartesWagon.estVide() && pilesDeCartesBateau.estVide()) {
                log("Les deux pioche sont vides");
                break;
            }
            else {
                String choix_remplacerCarte = joueurCourant.choisir(
                        "Veuillez choisir la pioche pour remplacer la carte actuelle piocher dans les cartes visibles",
                        choix_pioche,
                        boutons_pioche,
                        false
                );

                if (pilesDeCartesBateau.estVide() && pilesDeCartesWagon.estVide()) {
                    log("La seule pioche restantes la pile de cartes visibles");
                    verif_pioche = false;
                } else if (!pilesDeCartesBateau.estVide() && !pilesDeCartesWagon.estVide()) {
                    if (choix_remplacerCarte.equals("WAGON")) {
                        cartesTransportVisibles.add(piocherCarteWagon());
                        verif_pioche = false;

                    } else if (choix_remplacerCarte.equals("BATEAU")) {
                        cartesTransportVisibles.add(piocherCarteBateau());
                        verif_pioche = false;
                    }

                } else if (pilesDeCartesWagon.estVide() && !pilesDeCartesBateau.estVide() && choix_remplacerCarte.equals("BATEAU")) {
                    log("La pile de carte wagon est vide, nous remplacerons donc la carte par une carte bateau");
                    cartesTransportVisibles.add(piocherCarteBateau());
                    verif_pioche = false;

                } else if (!pilesDeCartesWagon.estVide() && pilesDeCartesBateau.estVide() && choix_remplacerCarte.equals("WAGON")) {
                    log("La pile de carte bateau est vide, nous remplacerons donc la carte par une carte wagon");
                    cartesTransportVisibles.add(piocherCarteWagon());
                    verif_pioche = false;
                }

            }
        }
    }
    /*
        if (pilesDeCartesBateau.estVide()  && pilesDeCartesWagon.estVide()) {
            cartesTransportVisibles.remove(c);
        }

        else if (((c.getType().equals(WAGON) || c.getType().equals(JOKER)) && !pilesDeCartesWagon.estVide())) {
            cartesTransportVisibles.remove(c);
            cartesTransportVisibles.add(piocherCarteWagon());
        }

        else if ((c.getType().equals(BATEAU) && !pilesDeCartesBateau.estVide())) {
            cartesTransportVisibles.remove(c);
            cartesTransportVisibles.add(piocherCarteBateau());
        }*/




    public void run() {
        // IMPORTANT : Le corps de cette fonction est à réécrire entièrement
        // Un exemple très simple est donné pour illustrer l'utilisation de certaines méthodes
        int nb_tour = 0;

        boolean fin_de_partie = false;
        while (!fin_de_partie) {

            if (joueurCourant.getNbPionsWagon() + joueurCourant.getNbPionsBateau()<=6){
                int copie_Nb_tour= nb_tour +2;
                if (copie_Nb_tour == nb_tour){
                    fin_de_partie = true;
                }
            }

            /*
            String affichage_tour = joueurCourant.choisir(
                    "Tour : " + nb_tour,
                    null,
                    null,
                    true
            );*/


            if (nb_tour == 0) {
                // Initialization de la pioche des Destination

                // Ajout des 3 cartes Wagons à la pioche visibles
                for (int i = 0; i < 3; i++) {
                    cartesTransportVisibles.add(piocherCarteWagon());
                }

                // Ajout des 3 cartes Bateau à la pioche visibles
                for (int i = 0; i < 3; i++) {
                    cartesTransportVisibles.add(piocherCarteBateau());
                }
                jouerTourInitialization();
            }


            else {
                for (Joueur j : joueurs) {
                    joueurCourant = j;
                    j.jouerTour();
                }
            }
            nb_tour++;
        }


        // Fin de la partie
        prompt("Fin de la partie.", new ArrayList<>(), true);
    }


    /**
     * Pioche une carte de la pile de pioche des cartes wagon.
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CarteTransport piocherCarteWagon() {
        return pilesDeCartesWagon.piocher();
    }

    public boolean piocheWagonEstVide() {
        return pilesDeCartesWagon.estVide();
    }

    /**
     * Pioche une carte de la pile de pioche des cartes bateau.
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CarteTransport piocherCarteBateau() {
        return pilesDeCartesBateau.piocher();
    }

    public boolean piocheBateauEstVide() {
        return pilesDeCartesBateau.estVide();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     * file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<Bouton> boutons, boolean peutPasser) {
        this.instruction = instruction;
        this.boutons = boutons;


        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<\n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (Bouton bouton : boutons) {
                joiner.add(bouton.toPrompt());
            }
            System.out.printf(">>> %s: %s [%s] <<<\n", joueurCourant.getNom(), instruction, joiner);
        }
        GameServer.setEtatJeu(new Gson().toJson(dataMap()));
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    public Map<String, Object> dataMap() {
        return Map.ofEntries(
                Map.entry("joueurs", joueurs.stream().map(Joueur::dataMap).toList()),
                Map.entry("joueurCourant", joueurs.indexOf(joueurCourant)),
                Map.entry("piocheWagon", pilesDeCartesWagon.dataMap()),
                Map.entry("piocheBateau", pilesDeCartesBateau.dataMap()),
                Map.entry("cartesTransportVisibles", cartesTransportVisibles),
                Map.entry("nbDestinations", pileDestinations.size()),
                Map.entry("instruction", instruction),
                Map.entry("boutons", boutons),
                Map.entry("log", log));
    }

    public static void main(String[] args) {

        /*ArrayList choix_nb_pions_wagons_possibilities = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            choix_nb_pions_wagons_possibilities.add(i);
        }
        ArrayList<Bouton> boutons_nb_wagons = new ArrayList<>();

        for (int i = 0; i < choix_nb_pions_wagons_possibilities.toArray().length; i++) {
            boutons_nb_wagons.add(new Bouton(String.valueOf(choix_nb_pions_wagons_possibilities.get(i))));
        }

        System.out.println(choix_nb_pions_wagons_possibilities);
        System.out.println(boutons_nb_wagons);*/

        ArrayList<Route> routes = new ArrayList<>();
        routes.add(new RouteMaritime(new Ville("Al-Qahira", false), new Ville("Athina", false), Couleur.VERT, 1)); // R1
        //routes.add(new RouteTerrestre(villes.get("Al-Qahira"), villes.get("Casablanca"), Couleur.GRIS, 3)); // R2

        CarteTransport c = new CarteTransport(TypeCarteTransport.BATEAU, Couleur.ROUGE, false, false);


        /*for (int i = 0; i < routes.toArray().length; i++) {
            System.out.println(routes.get(i).getClass());
            c.getType();
            if (String.valueOf(routes.get(i).getClass()).equals("class fr.umontpellier.iut.rails.RouteMaritime")) {

                System.out.println(true);
            }

            else {
                System.out.println(false);
            }

        }*/

    }
}
