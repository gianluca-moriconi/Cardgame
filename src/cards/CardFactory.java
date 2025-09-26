package cards; //PACKAGE CARDS

import core.*;
import java.util.*;

public class CardFactory { //CLASSE CARDFACTORY

    // --- ESPLORAZIONE: generiche
    public static List<Card> explorationGeneric(Game G) { 	//Metodo statico che restituisce una lista carte (explorationGeneric)
        
    	return List.of( //List.of crea una lista immutabile
    			
            new Card("Sgranchisciti (cura 2)", Phase.EXPLORATION, Set.of(), //Costruttore carta | Nome carta : Fase : Allowed=Set.of vuoto=mazzo generico
                (g,p) -> g.heal(p, 2, "Si stiracchia per curare 2 HP")), 	//Lambda che implementa BiConsumer<Game, Player> | 
            											 	//quando giochi la carta, chiama heal sul Game passato a runtime (g) curando il player p di 2.
           
            new Card("Distilla pozione(Pozioni +1)", Phase.EXPLORATION, Set.of(),
                (g,p) -> { g.teamPotions++; System.out.println(p.name+" Pesta e mescola! Pozioni del party: "+g.teamPotions); })
        );
    }

    // --- ESPLORAZIONE: per classe
    public static List<Card> explorationByClass(Game G, ClassType cls) {
        List<Card> list = new ArrayList<>();
        switch (cls) {
            case BARDO -> list.add(new Card("Assolo alternativo (+1 danno prossimo attacco)", Phase.EXPLORATION, Set.of(ClassType.BARDO),
                (g,p) -> { p.nextAttackBonus += 1; System.out.println(p.name+" esegue un assolo di liuto. Bonus danno: +"+p.nextAttackBonus); }));
            case GUERRIERO -> list.add(new Card("Carica eroica (+1 danno prossimo attacco)", Phase.EXPLORATION, Set.of(ClassType.GUERRIERO),
                (g,p) -> { p.nextAttackBonus += 1; System.out.println(p.name+" gonfia i muscoli. Bonus danno: +"+p.nextAttackBonus); }));
            case MAGO -> list.add(new Card("Riflessione intellettuale (+1 danno prossimo attacco)", Phase.EXPLORATION, Set.of(ClassType.MAGO),
                (g,p) -> { p.nextAttackBonus += 1; System.out.println(p.name+" esplora i suoi pensieri. Bonus danno: +"+p.nextAttackBonus); }));
            case LADRO -> list.add(new Card("Fai scorta (Pozioni +1)", Phase.EXPLORATION, Set.of(ClassType.LADRO),
                (g,p) -> { g.teamPotions++; System.out.println(p.name+" sfila qualche oggetto dalle tasche dei passanti. Pozioni: "+g.teamPotions); }));
        }
        return list;
    }

    // --- COMBATTIMENTO: generiche
    public static List<Card> combatGeneric(Game G) {
        return List.of(
            new Card("Calcio basso (1 danni)", Phase.COMBAT, Set.of(),
                (g,p) -> g.dealDamage(1, p, " un calcio basso")),
            new Card("Colpo rapido (2 danni)", Phase.COMBAT, Set.of(),
                (g,p) -> g.dealDamage(2, p, " un attacco rapido"))
        );
    }

    // --- COMBATTIMENTO: per classe
    public static List<Card> combatByClass(Game G, ClassType cls) {
        List<Card> list = new ArrayList<>();
        switch (cls) {
            case BARDO -> list.add(new Card("Acuto potente (+2 al prossimo attacco)", Phase.COMBAT, Set.of(ClassType.BARDO),
                (g,p) -> { p.nextAttackBonus += 2; System.out.println(p.name+" si lascia andare. Bonus danno: +"+p.nextAttackBonus); }));
            case GUERRIERO -> list.add(new Card("Colpo possente (4 danni)", Phase.COMBAT, Set.of(ClassType.GUERRIERO),
                (g,p) -> g.dealDamage(4, p, "Colpo possente")));
            case MAGO -> list.add(new Card("Dardo incantato (3 danni sicuri)", Phase.COMBAT, Set.of(ClassType.MAGO),
                (g,p) -> g.dealDamage(3, p, "Dardo incantato")));
            case LADRO -> list.add(new Card("Pugnale furtivo (3-5 danni)", Phase.COMBAT, Set.of(ClassType.LADRO),
                (g,p) -> g.dealDamage(g.rng.nextInt(3)+3, p, "Pugnale furtivo")));
        }
        return list;
    }
}
