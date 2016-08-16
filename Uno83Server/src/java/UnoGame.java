
import engine.unoEngine;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import model.Table;
import model.unoGame;
import model.unoPlayer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Mitch
 */
@ServerEndpoint("/table/{gameId}/{playerName}")
public class UnoGame {

    @Inject
    private Table table;

    private String newMessage = null;

    @OnOpen
    public void onOpen(Session session, @PathParam("gameId") String gameId, @PathParam("playerName") String playerName) {

        UUID id = UUID.randomUUID();
        String playerId = id.toString().substring(0, 6);
        unoGame g = table.getGameList().get(gameId);

        if ("table".equals(playerName)) {
            unoPlayer one = new unoPlayer("table", playerId);
            one.setSession(session);
            g.addPlayer(one);
        } else {
            unoPlayer player = new unoPlayer(playerName, playerId);
            player.setSession(session);
            if (g.getCapcity() + 1 - g.getGamePlayers().size() > 0) {
                g.addPlayer(player);
                if (g.getCapcity() + 1 == g.getGamePlayers().size()) {
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "forceStart")
                            .build().toString();
                    try {
                        g.getGamePlayers().get(0).getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                newMessage = Json.createObjectBuilder()
                        .add("msg", "cantJoin")
                        .build().toString();
                try {
                    g.getGamePlayers().get(0).getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        System.out.println("sss");
        InputStream is = new ByteArrayInputStream(msg.getBytes());
        JsonReader reader = Json.createReader(is);
        JsonObject data = reader.readObject();

        String flag = data.getString("msg");
        unoGame g = table.getGameList().get(data.getString("gameId"));

        switch (flag) {
            case "joinGame":
//                if (g.getGamePlayers().size() == g.getCapcity() + 1) {
//                    newMessage = Json.createObjectBuilder()
//                            .add("msg", "cantJoin")
//                            .build().toString();
//                    try {
//                        session.getBasicRemote().sendText(newMessage);
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
                for (unoPlayer p : g.getGamePlayers()) {
                    if ("table".equals(p.getName())) {
                        int count = g.getGamePlayers().size() - 1;
                        newMessage = Json.createObjectBuilder()
                                .add("msg", "refresh")
                                .add("count", count)
                                .add("players", g.playersToJson().build())
                                .build().toString();
                        try {
                            p.getSession().getBasicRemote().sendText(newMessage);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                break;
            case "startGame":
                if (g.getGamePlayers().size() <= 2) {
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "cantStart").build()
                            .toString();
                    try {
                        g.getGamePlayers().get(0).getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                unoEngine.initDeck(g.getGameDeck());
                unoEngine.initGame(g);
                for (unoPlayer p : g.getGamePlayers()) {
                    if ("table".equals(p.getName())) {
                        newMessage = Json.createObjectBuilder()
                                .add("msg", "start")
                                .add("pile", g.getDicardPile().getImage())
                                .add("players", g.playersToJson().build())
                                .add("cardsCount", g.getGameDeck().getDeck().size())
                                .build()
                                .toString();
                    } else {
                        newMessage = Json.createObjectBuilder()
                                .add("msg", "deal")
                                .add("hands", p.handsToJson().build())
                                .build()
                                .toString();
                    }
                    try {
                        p.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
