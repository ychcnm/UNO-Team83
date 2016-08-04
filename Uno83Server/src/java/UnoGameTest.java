/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.List;
import uno.Value;
import uno.unoCard;
import uno.unoDeck;
import uno.unoGame;
import uno.unoPlayer;

/**
 *
 * @author Mitch
 */
public class UnoGameTest {

    static public unoGame loadGame() {
        unoDeck deck = unoDeck.createNewDeck();
        String id = "ABCDEF01";
        List<unoPlayer> players = new ArrayList<unoPlayer>();
        players.add(new unoPlayer("Mitch", "Mitch"));
        players.add(new unoPlayer("Melody", "Melody"));
        players.add(new unoPlayer("ThinSwe", "ThinSwe"));
        players.add(new unoPlayer("Johnson", "Johnson"));
        players.add(new unoPlayer("John", "John"));
        System.out.println("The Player is Ready");

        System.out.println("The Game is Created");
        return new unoGame(id, players, Value.GAME_STARTED, deck, deck.takeCard());

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        unoGame newGame = loadGame();
        System.out.println("Start to Deal The Card");

        for (unoPlayer p : newGame.getGamePlayers()) {
            for (int i = 0; i < 7; i++) {
                p.addToHand(newGame.getGameDeck().takeCard());
            }
        }

        System.out.println("run: ");
        System.out.println("Id: " + newGame.getId());
        System.out.println("Discard: card: " + newGame.getDicardPile());
        System.out.println("Cards on deck: " + newGame.getGameDeck().getAmount());
        for (unoPlayer p : newGame.getGamePlayers()) {
            System.out.println("        Player:" + p);
            System.out.println("        Cards in hand:");
            for (unoCard c : p.getHandCards()) {
                System.out.println("                Card: " + c);
            }
            System.out.println();
        }
    }
}
