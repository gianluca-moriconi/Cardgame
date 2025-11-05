package core; //PACKAGE CORE

import cards.Card; //import libreria java util
import java.util.*;  //import locale Card

public class Player { //CLASSE GIOCATORE
    public final String name;   //Nome
    public final boolean human; //Player umano/bot
    public final ClassType cls; //Classe mazzo

    public int hp = 10; 		//Hp
    public int nextAttackBonus = 0; // bonus personale consumato al prossimo attacco


    //Dichiarazioni di campo 
    public final List<Card> hand = new ArrayList<>(); 				//List| Mano del giocatore
    public final Deque<Card> deckExploration = new ArrayDeque<>(); 	//Double-ended queue| Mazzo esplorazione
    public final Deque<Card> deckCombat = new ArrayDeque<>(); 		//Double-ended queue| Mazzo combattimento
    public final List<Card> discardExploration = new ArrayList<>(); //List| Pila degli scarti, esplorazione
    public final List<Card> discardCombat = new ArrayList<>(); 		//List| Pila degli scarti, combattimento

    public Player(String name, boolean human, ClassType cls) { 		//Costruttore Player
        this.name = name; this.human = human; this.cls = cls;
    }

    public boolean alive() { return hp > 0; } //metodo status alive

    public Deque<Card> deckFor(Phase phase) { //Selettore mazzo 
        return (phase == Phase.EXPLORATION) ? deckExploration : deckCombat; //condizione ? valoreSeVero : valoreSeFalso
    }
    public List<Card> discardFor(Phase phase) { //Selettore pila scarti
        return (phase == Phase.EXPLORATION) ? discardExploration : discardCombat; //condizione ? valoreSeVero : valoreSeFalso
    }
}
