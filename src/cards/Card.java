package cards; //PACKAGE CARDS

import java.util.Set; 					//Import interface per collezione elementi unici 
import java.util.function.BiConsumer; 	//Accetta selezione di due input e non restituisce valori di return

import core.ClassType; 		//import locale classi
import core.Game; 			//import locale struttura gioco
import core.Phase; 			//import locale fasi di gioco
import core.Player; 		//import locale giocatore

public class Card { //CLASSE CARTA
    public final String name; 			 			//nome carta
    public final Phase phase; 			 			//nome fase
    public final Set<ClassType> allowed; 			//build carta | vuoto = comune
    public final BiConsumer<Game, Player> effect;   //effetto carta

    public Card(String name, Phase phase, Set<ClassType> allowed, BiConsumer<Game, Player> effect) { //COSTRUTTORE CARTA
        this.name = name;
        this.phase = phase;
        this.allowed = (allowed == null) ? Set.of() : Set.copyOf(allowed);
        this.effect = effect;
    }

    //CHECK CLASSE PER USO CARTA
    public boolean isAllowedFor(Player p) { 
    	/*
    	* allowed = insieme di classi che possono usare la carta
    	* Se allowed.is Empty() = true, la carta è generica. Uso di tutti
    	* ...altrimenti, la carta è di classe e ritorna true solo se allowe.contains(p.cls) 
    	* ...ovvero la classe del giocatore è presente nell' insieme
    	* 
    	* || è "Short circuit". Se la carta è comune, non serve controllare contains
    	* 
    	* allowed = {} → tutti i giocatori: true.
		* allowed = {BARDO} → BARDO: true, MAGO/GUERRIERO/LADRO: false.
		* allowed = {LADRO, MAGO} → LADRO/MAGO: true, altri: false.
    	* 
    	*/
        return allowed.isEmpty() || allowed.contains(p.cls);
    }

    @Override public String toString() { return name; } //override metodo toString per stampare solo nome carta
}
