package core; //PACKAGE CORE

import cards.*; //import locale Card e CardFactory
import java.util.*; //import libreria java util

public class Game { //CLASSE GAME
    public final Random rng = new Random();                //generatore di numeri casuali
    private final Scanner in = new Scanner(System.in);     //scanner per input da console

    public final List<Player> players = new ArrayList<>(); //List| Lista giocatori

    public Phase phase = Phase.EXPLORATION;                // FASE INIZIALE
    public int turnInPhase = 0;                            // contatore turni nella fase corrente
    public int activeIndex = 0;                            // indice del giocatore attivo

    // RISORSE DI SQUADRA
    public int teamPotions = 0;
    

    public Monster monster = null; 

    // TUNING
    public static final int HAND_SIZE = 3;                  // numero max di carte in mano
    public static final int COPIES_GENERIC = 2;             // numero di copie per le carte generiche
    public static final int COPIES_CLASS = 2;               // numero di copie per le carte di classe

    public void start() {                                   //AVVIO DEL GIOCO
        banner("Mini Card Game — pacchetti + mano");      //titolo
        setupPlayers();                                     //setup giocatori
        buildPhaseDecks(Phase.EXPLORATION);                 //costruzione mazzi
        loop();                                             //loop di gioco
    }

    private void banner(String s){ System.out.println("\n=== " + s + " ===\n"); } 

    private void setupPlayers() {                           //SETUP GIOCATORI
        System.out.println("Scegli la tua classe:");
        ClassType[] all = ClassType.values();                                       //restituisce il valore array di classtype
        for (int i=0;i<all.length;i++) System.out.println((i+1)+") "+all[i]);       //stampa le classi
        int choice = readInt("Numero [1-"+all.length+"]: ", 1, all.length) - 1; //legge la scelta
        ClassType humanCls = all[choice];                                           //assegna la classe scelta a humanCls
        players.add(new Player("Tu ("+humanCls+")", true, humanCls));         //aggiunge il giocatore umano alla lista

        List<ClassType> pool = new ArrayList<>(Arrays.asList(all));       //assegna le classi rimanenti a bot
        pool.remove(humanCls);                                            //rimuove la classe del giocatore umano dalle selezionabili
        Collections.shuffle(pool, rng);                                   //mescola le classi rimanenti
        for (int i=0;i<3;i++) {                                           //crea 3 bot
            ClassType botCls = pool.get(i % pool.size());                 //assegna la classe scelta a botCls
            players.add(new Player("Bot"+(i+1)+" ("+botCls+")", false, botCls));
        }

        System.out.println("\nPartecipanti:");                            //stampa i giocatori
        players.forEach(p -> System.out.println(" - " + p.name)); 
    }

    private void buildPhaseDecks(Phase ph) {                //COSTRUZIONE DEI MAZZI
        for (Player p : players) {                  //per ogni giocatore
            Deque<Card> deck = p.deckFor(ph);       //...seleziona il mazzo in base alla fase
            List<Card> discard = p.discardFor(ph);  //...genera la pila degli scarti in base alla fase
            deck.clear(); discard.clear();          //svuota mazzo e pila scarti

            if (ph == Phase.EXPLORATION) { //se fase di esplorazione
                for (Card c : CardFactory.explorationGeneric(this)) repeatAdd(deck, c, COPIES_GENERIC); //aggiunge carte generiche esplorazione
                for (Card c : CardFactory.explorationByClass(this, p.cls)) repeatAdd(deck, c, COPIES_CLASS); //aggiunge carte di classe esplorazione
            } else {
                for (Card c : CardFactory.combatGeneric(this)) repeatAdd(deck, c, COPIES_GENERIC); //aggiunge carte generiche combattimento
                for (Card c : CardFactory.combatByClass(this, p.cls)) repeatAdd(deck, c, COPIES_CLASS); //aggiunge carte di classe combattimento
            }

            List<Card> tmp = new ArrayList<>(deck); // mescola il mazzo
            Collections.shuffle(tmp, rng);          // mescola la lista temporanea
            deck.clear();                           // svuota il mazzo 
            for (Card c : tmp) deck.push(c);        // top del mazzo
        }

        for (Player p : players) p.hand.clear();    //svuota la mano di tutti i giocatori

        if (ph == Phase.COMBAT) { // se fase di combattimento
            monster = new Monster(generateMonsterName(), 25 + rng.nextInt(11)); // crea un mostro con hp casuali tra 25 e 35
            System.out.println("\n>> Inizia la BATTAGLIA! Appare " + monster.name + " (HP: " + monster.hp + ")\n");
        } else {
            // reset di tutti i bonus personali a inizio esplorazione
            for (Player pl : players) pl.nextAttackBonus = 0;
            monster = null;
            System.out.println("\n>> Fase di ESPLORAZIONE (bonus danno resettati). Preparati a potenziarti per lo scontro!\n");
}


        for (Player p : players) ensureHand(p);  // riempie la mano di tutti i giocatori fino a HAND_SIZE
    }

    private String generateMonsterName() { // nome casuale del mostro
        String[] names = {"Mostro comune","Orco mugghiante","Gelatina verdastra","Ratto gigante","Spirito rancoroso"};
        return names[rng.nextInt(names.length)];
    }

    private void repeatAdd(Deque<Card> deck, Card card, int times) { //aggiunge carte al mazzo
        for (int i=0;i<times;i++) deck.add(card);
    }

    private void loop() { //LOOP DI GIOCO
        while (true) {
            Player p = players.get(activeIndex); //giocatore attivo
            if (!p.alive()) { advanceTurn(); continue; }  // se il giocatore attivo e' morto, passa al giocatore successivo

            if (phase == Phase.EXPLORATION) {   //se fase di esplorazione
                doExplorationTurn(p);           //esegue il turno di esplorazione
            } else {                            //se fase di combattimento
                doCombatTurn(p);                //esegue il turno di combattimento
                // esito battaglia
                if (monster.hp <= 0) {          // se il mostro e' stato sconfitto
                    System.out.println("\n>>> VITTORIA! Il mostro è stato sconfitto.\n");
                    if (askYesNo("Vuoi terminare l'avventura e chiudere il gioco? (s/n): ")) {
                        System.out.println("Grazie per aver giocato!");
                        return;
                    }
                    // Riparti SUBITO con ESPLORAZIONE
                    System.out.println(">> Si torna in ESPLORAZIONE!");
                    monster = null;              // nessun mostro in esplorazione
                    phase = Phase.EXPLORATION;   // switch di fase immediato
                    turnInPhase = 0;             // azzera il contatore dei turni di fase
                    buildPhaseDecks(Phase.EXPLORATION); // ricostruisci mazzi e mani per l'esplorazione
                    // Nota: il giro di turni prosegue normalmente; l'advanceTurn() a fine loop
                    // passerà la mano al prossimo giocatore, ora in ESPLORAZIONE.
                }
                if (players.stream().noneMatch(Player::alive)) { // se tutti i giocatori sono morti
                    System.out.println("\n>>> SCONFITTA! Tutto il party è caduto.\n");
                    return;
                }
            }

            advanceTurn();
        }
    }

    private void doExplorationTurn(Player p) { //TURNO DI ESPLORAZIONE
        ensureHand(p); //riempie la mano
        System.out.println("\n[Turno "+(turnInPhase+1)+"/16] "+p.name+" (HP:"+p.hp+") — Pozioni: "+teamPotions+" — BonusDmg: +"+p.nextAttackBonus);
        if (p.human) {
            showHand(p); //mostra la mano
            System.out.println("0) Passa"); 
            int sel = readInt("Scegli carta da giocare: ", 0, p.hand.size()); //legge la scelta
            if (sel == 0) { System.out.println(p.name+" osserva i dintorni e passa.\n"); return; } //se 0, passa
            playFromHand(p, sel-1); //gioca la carta selezionata
        } else {
            if (!p.hand.isEmpty()) playFromHand(p, rng.nextInt(p.hand.size()));   //gioca una carta a caso
            else System.out.println(p.name+" non trova carte utili e passa.\n");  //se non ha carte, passa
        }
    }

    private void doCombatTurn(Player p) { //TURNO DI COMBATTIMENTO
        ensureHand(p);
        System.out.println("\n[Turno "+(turnInPhase+1)+"/16] "+p.name+" (HP:"+p.hp+") vs "+monster.name+" (HP:"+monster.hp+") — Pozioni: "+teamPotions+" — BonusDmg: +"+p.nextAttackBonus);
        if (p.human) {
            System.out.println("Azioni:");
            System.out.println("0) Passa");
            System.out.println("U) Usa pozione (+3 HP)"+(teamPotions>0?"":" [nessuna]"));
            showHand(p);
            String choice = readLine("Scegli indice carta (1-"+p.hand.size()+"), oppure 'U'/'0': ");
            if (choice.equalsIgnoreCase("U")) {
                if (teamPotions>0) { teamPotions--; heal(p,3,"Pozione"); } else System.out.println("Nessuna pozione.");
            } else {
                try {
                    int n = Integer.parseInt(choice);
                    if (n>=1 && n<=p.hand.size()) playFromHand(p, n-1);
                    else System.out.println("Scelta non valida.");
                } catch (NumberFormatException ex) {
                    System.out.println("Scelta non valida.");
                }
            }
        } else {
            boolean usedPotion = false;
            if (p.hp<=4 && teamPotions>0 && rng.nextInt(100)<30) {
                teamPotions--; heal(p,3,p.name+" (bot) beve una pozione");
                usedPotion = true;
            }
            if (!usedPotion && !p.hand.isEmpty()) playFromHand(p, rng.nextInt(p.hand.size()));
            else if (!usedPotion) System.out.println(p.name+" esita e passa.");
        }

        if (monster != null && monster.hp > 0) {
            int dmg = 1;
            p.hp -= dmg;
            System.out.println(monster.name+" colpisce "+p.name+" per "+dmg+" danno. HP: "+Math.max(0,p.hp));
            if (!p.alive()) System.out.println(p.name+" è KO!");
        }
        System.out.println();
    }

    private void showHand(Player p) {
        System.out.println("Mano di "+p.name+":");
        for (int i=0;i<p.hand.size();i++) System.out.println((i+1)+") "+p.hand.get(i).name);
    }

    private void playFromHand(Player p, int index) {
        if (index<0 || index>=p.hand.size()) return;
        Card c = p.hand.remove(index);
        System.out.println(p.name+" gioca: "+c.name);
        c.effect.accept(this, p);
        p.discardFor(phase).add(c);
    }

    private void ensureHand(Player p) {
        while (p.hand.size() < HAND_SIZE) {
            if (!drawOne(p)) break;
        }
    }

    private boolean drawOne(Player p) {
        Deque<Card> deck = p.deckFor(phase);
        if (deck.isEmpty()) {
            reshuffleFromDiscard(p, phase);
            deck = p.deckFor(phase);
            if (deck.isEmpty()) return false;
        }
        p.hand.add(deck.pop());
        return true;
    }

    private void reshuffleFromDiscard(Player p, Phase ph) {
        List<Card> disc = p.discardFor(ph);
        if (disc.isEmpty()) return;
        Collections.shuffle(disc, rng);
        Deque<Card> deck = p.deckFor(ph);
        for (Card c : disc) deck.push(c);
        disc.clear();
    }

    // ==== Effetti base usati dalle carte ====
    public void dealDamage(int base, Player src, String srcName) {
        if (monster == null) { System.out.println("…ma non c'è alcun mostro."); return; }
        int extra = src.nextAttackBonus;
        src.nextAttackBonus = 0; // consumato subito

        int total = Math.max(0, base + extra);
        monster.hp -= total;
        System.out.println(">>> "+src.name+" infligge "+total+" danni con \""+srcName+"\"" +
                (extra>0? " (bonus +"+extra+")":"") + ". HP Mostro: "+Math.max(0, monster.hp));
    }

    public void heal(Player p, int amount, String why) {
        int before = p.hp;
        p.hp = Math.min(10, p.hp + amount);
        System.out.println(why+": "+p.name+" recupera "+(p.hp-before)+" HP (ora "+p.hp+"/10).");
    }

    private void advanceTurn() {
        activeIndex = (activeIndex + 1) % players.size();
        turnInPhase++;
        if (turnInPhase >= 16) {
            phase = (phase == Phase.EXPLORATION) ? Phase.COMBAT : Phase.EXPLORATION;
            turnInPhase = 0;
             
            System.out.println("\n--- CAMBIO FASE -> " + phase + " ---");
            buildPhaseDecks(phase);
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v>=min && v<=max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Inserisci un numero tra "+min+" e "+max+".");
        }
    }
    private String readLine(String prompt) { System.out.print(prompt); return in.nextLine(); }

    private boolean askYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim().toLowerCase(Locale.ITALY);
            if (s.equals("s") || s.equals("si") || s.equals("sì")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Rispondi con s/n.");
        }
    }
}
