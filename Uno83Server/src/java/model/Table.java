package model;


import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mitch
 */
@ApplicationScoped
public class Table {

    private Map<String, unoGame> gameList = new HashMap<>();

    public Map<String, unoGame> getGameList() {
        return gameList;
    }

    public void setGameList(Map<String, unoGame> gameList) {
        this.gameList = gameList;
    }

}
