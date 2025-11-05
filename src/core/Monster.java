package core; //PACKAGE CORE

public class Monster { 			//CLASSE MONSTER
    public final String name; 	//Nome mostro, non modificabile (final)
    public int hp; 				//Hp scalabili con danno
    
    public Monster(String name, int hp) { this.name = name; this.hp = hp; } //costruttore mostro
}
