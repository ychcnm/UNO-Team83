
import engine.unoEngine;
import enums.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import model.unoCard;
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
            g.setSession(session);
        } else {
            unoPlayer player = new unoPlayer(playerName, playerId);
            player.setSession(session);
            if (g.getCapcity() - g.getGamePlayers().size() > 1) {
                g.addPlayer(player);
            } else if (g.getCapcity() - g.getGamePlayers().size() == 1) {
                g.addPlayer(player);
                newMessage = Json.createObjectBuilder()
                        .add("msg", "forceStart")
                        .build().toString();
                try {
                    g.getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                newMessage = Json.createObjectBuilder()
                        .add("msg", "cantJoin")
                        .build().toString();
                try {
                    session.getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        InputStream is = new ByteArrayInputStream(msg.getBytes());
        JsonReader reader = Json.createReader(is);
        JsonObject data = reader.readObject();

        String flag = data.getString("msg");
        unoGame g = table.getGameList().get(data.getString("gameId"));

        switch (flag) {
            case "joinGame": {
                int count = g.getGamePlayers().size();
                newMessage = Json.createObjectBuilder()
                        .add("msg", "refresh")
                        .add("count", count)
                        .add("players", g.playersToJson().build())
                        .build().toString();
                try {
                    g.getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case "startGame": {
                if (g.getGamePlayers().size() < 2) {
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "cantStart").build()
                            .toString();
                    try {
                        g.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    unoEngine.initDeck(g.getGameDeck());
                    unoEngine.initGame(g);
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "start")
                            .add("pile", g.getDicardPile().getImage())
                            .add("players", g.playersToJson().build())
                            .add("cardsCount", g.getGameDeck().getDeck().size())
                            .build()
                            .toString();
                    try {
                        g.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    for (int i = 0; i < g.getGamePlayers().size(); i++) {
                        int score = g.getGamePlayers().get(i).getScore();
                        if (i == 0) {
                            newMessage = Json.createObjectBuilder()
                                    .add("msg", "deal")
                                    .add("turn", "It is your turn")
                                    .add("hands", g.getGamePlayers().get(i).handsToJson().build())
                                    .add("score", score)
                                    .add("handsCount", g.getGamePlayers().get(i).getHandCards().size())
                                    .build().toString();
                        } else {
                            newMessage = Json.createObjectBuilder()
                                    .add("msg", "deal")
                                    .add("turn", "It is not your turn")
                                    .add("score", score)
                                    .add("hands", g.getGamePlayers().get(i).handsToJson().build())
                                    .add("handsCount", g.getGamePlayers().get(i).getHandCards().size())
                                    .build().toString();
                        }
                        try {
                            g.getGamePlayers().get(i).getSession().getBasicRemote().sendText(newMessage);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            break;
            case "throwCard": {
                unoPlayer player = g.findPlayer(data.getString("playerName"));
                String a = data.getString("card");
                unoCard card = player.findCard(a);
                int score = 0;
                switch (g.judgeCard(card)) {
                    case NORMAL:
                        g.setDrawCount(1);
                        g.setShiftCount(1);
                        break;
                    case SKIP:
                        g.setShiftCount(2);
                        break;
                    case REVERSE:
                        g.setShiftCount(0);
                        g.turnDirection();
                        break;
                    case DRAW_2:
                        g.setShiftCount(1);
                        g.setDrawCount(g.getDrawCount() + 2);
                        break;
                    case DRAW_4:
                        g.setShiftCount(1);
                        g.setDrawCount(g.getDrawCount() + 4);
                        break;
                    case WILD:
                        g.setShiftCount(1);
                        g.setFlag(true);
                        break;
                    case PASS:
                        g.setShiftCount(1);
                        g.setFlag(false);
                        break;
                    default:
                        newMessage = Json.createObjectBuilder()
                                .add("msg", "returnCard")
                                .add("hands", player.handsToJson().build())
                                .add("handsCount", player.getHandCards().size())
                                .build()
                                .toString();
                        try {
                            player.getSession().getBasicRemote().sendText(newMessage);
                            g.setFlag(false);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return;
                };
                g.setDicardPile(player.removeFormHand(card));
                score = player.getScore();
                if (player.getHandCards().size() == 0) {
                    g.setGameStatus(Status.GAME_END);
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "gameEnd")
                            .add("players", g.rankToJson().build())
                            .build()
                            .toString();
                    try {
                        g.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    for (unoPlayer p : g.getGamePlayers()) {
                        try {
                            p.getSession().getBasicRemote().sendText(newMessage);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return;
                }
                newMessage = Json.createObjectBuilder()
                        .add("msg", "changePile")
                        .add("pile", g.getDicardPile().getImage())
                        .build()
                        .toString();
                try {
                    g.getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                newMessage = Json.createObjectBuilder()
                        .add("msg", "refreshCard")
                        .add("hands", player.handsToJson().build())
                        .add("handsCount", player.getHandCards().size())
                        .add("score", score)
                        .add("turn", "It is not your turn")
                        .build()
                        .toString();
                try {
                    player.getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                for (int i = 0; i < g.getShiftCount(); i++) {
                    g.addPlayer(g.getGamePlayers().remove(0));
                }
                newMessage = Json.createObjectBuilder()
                        .add("msg", "shift")
                        .add("turn", "It is your turn")
                        .build()
                        .toString();
                try {
                    g.getGamePlayers().get(0).getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
            break;
            case "drawCard": {
                unoPlayer player = g.findPlayer(data.getString("playerName"));
                unoCard card = null;
                if (g.getDrawCount() == 1) {
                    card = g.takeCard();
                    player.addToHand(card);
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "refreshCardAmount")
                            .add("amount", g.getGameDeck().getAmount())
                            .build()
                            .toString();
                    try {
                        g.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    switch (g.judgeCard(card)) {
                        case NORMAL: {
                            newMessage = Json.createObjectBuilder()
                                    .add("msg", "refreshCard")
                                    .add("hands", player.handsToJson().build())
                                    .add("handsCount", player.getHandCards().size())
                                    .add("score", player.getScore())
                                    .add("turn", "It is your turn")
                                    .build()
                                    .toString();
                            try {
                                player.getSession().getBasicRemote().sendText(newMessage);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        break;
                        default: {
                            newMessage = Json.createObjectBuilder()
                                    .add("msg", "refreshCard")
                                    .add("hands", player.handsToJson().build())
                                    .add("handsCount", player.getHandCards().size())
                                    .add("score", player.getScore())
                                    .add("turn", "It is not your turn")
                                    .build()
                                    .toString();
                            try {
                                player.getSession().getBasicRemote().sendText(newMessage);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            g.addPlayer(g.getGamePlayers().remove(0));
                            newMessage = Json.createObjectBuilder()
                                    .add("msg", "shift")
                                    .add("turn", "It is your turn")
                                    .build()
                                    .toString();
                            try {
                                g.getGamePlayers().get(0).getSession().getBasicRemote().sendText(newMessage);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        break;
                    }
                    g.setFlag(false);
                } else {
                    for (int i = 0; i < g.getDrawCount(); i++) {
                        card = g.takeCard();
                        player.addToHand(card);
                    }
                    g.setDrawCount(1);
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "refreshCardAmount")
                            .add("amount", g.getGameDeck().getAmount())
                            .build()
                            .toString();
                    try {
                        g.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "refreshCard")
                            .add("hands", player.handsToJson().build())
                            .add("handsCount", player.getHandCards().size())
                            .add("score", player.getScore())
                            .add("turn", "It is not your turn")
                            .build()
                            .toString();
                    try {
                        player.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    g.addPlayer(g.getGamePlayers().remove(0));
                    player = g.getGamePlayers().get(0);
                    newMessage = Json.createObjectBuilder()
                            .add("msg", "refreshCard")
                            .add("hands", player.handsToJson().build())
                            .add("handsCount", player.getHandCards().size())
                            .add("score", player.getScore())
                            .add("turn", "It is your turn")
                            .build()
                            .toString();

                    try {
                        player.getSession().getBasicRemote().sendText(newMessage);
                        g.setFlag(true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            break;
            case "endGame": {
                g.setGameStatus(Status.GAME_END);
                newMessage = Json.createObjectBuilder()
                        .add("msg", "gameEnd")
                        .add("players", g.rankToJson().build())
                        .build()
                        .toString();
                try {
                    g.getSession().getBasicRemote().sendText(newMessage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                for (unoPlayer p : g.getGamePlayers()) {
                    try {
                        p.getSession().getBasicRemote().sendText(newMessage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            break;
            default:
                break;
        }
    }
}
