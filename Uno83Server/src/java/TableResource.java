
import engine.unoEngine;
import enums.Image;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import enums.Status;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
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
@RequestScoped
@Path("/UnoGame")
public class TableResource {

    //private static List<unoGame> gameList = new ArrayList<unoGame>();
    private static Map<String, unoGame> gameList = new HashMap<>();

    @POST
    @Path("POST/game/{gameTitle}/{capacity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(@PathParam("gameTitle") String title, @PathParam("capacity") int capacity) {
        UUID id = UUID.randomUUID();
        String gameId = id.toString().substring(0, 6);
        unoGame game = new unoGame(gameId, title, Status.GAME_WAITING, capacity);
        gameList.put(gameId, game);
        System.out.print(">> Create the game：ID:" + gameId + " TITLE:" + title);

        return Response.ok(game.toJson()).header("Access-Control-Allow-Origin","*").build();
    }

    @GET
    @Path("GET/gameList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGameList() {
        System.out.print(">> Get the Game List");
        JsonArrayBuilder gameJsonArray = Json.createArrayBuilder();
        for (Map.Entry<String, unoGame> game : gameList.entrySet()) {
            gameJsonArray.add(game.getValue().toJson());
        }
        return Response.ok(gameJsonArray.build())
                .header("Access-Control-Allow-Origin","*")
                .build();
    }

    @POST
    @Path("POST/joinGame/{gameId}/{playerName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response joinGame(@PathParam("gameId") String gameId, @PathParam("playerName") String playerName) {
        UUID id = UUID.randomUUID();
        String playerId = id.toString().substring(0, 6);
        unoPlayer player = new unoPlayer(playerName, playerId);

        unoGame g = gameList.get(gameId);
        if (g.getCapcity() == g.getGamePlayers().size()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        g.addPlayer(player);

        return Response.ok()
                .header("Access-Control-Allow-Origin","*")
                .build();
    }

    @GET
    @Path("Get/Count/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayerAmount(@PathParam("gameId") String gameId) {
        int amount = 0;
        unoGame g = gameList.get(gameId);
        amount = g.getGamePlayers().size();
        JsonObject json = Json.createObjectBuilder()
                .add("amount", amount)
                .build();
        return Response.ok(json)
                .header("Access-Control-Allow-Origin","*")
                .build();
    }

    @POST
    @Path("POST/Start/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startGame(@PathParam("gameId") String gameId) {
        unoGame g = gameList.get(gameId);
        unoEngine.initDeck(g.getGameDeck());
        unoEngine.initGame(g);
        JsonArrayBuilder gameInfoJsonArray = Json.createArrayBuilder();

        JsonObject gameJson = Json.createObjectBuilder()
                .add("img1", Image.BACK)
                .add("img2", g.getDicardPile().getImage())
                .build();

        gameInfoJsonArray.add(gameJson);

        for (unoPlayer p : g.getGamePlayers()) {
            JsonObject gameJson1 = Json.createObjectBuilder()
                    .add("name", p.getName())
                    .build();
            gameInfoJsonArray.add(gameJson1);
        }
        return Response.ok(gameInfoJsonArray.build())
                .header("Access-Control-Allow-Origin","*")
                .build();
    }

    @GET
    @Path("GET/gameInfo/{gameId}/{playerName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGameInfo(@PathParam("gameId") String gameId, @PathParam("playerName") String name) {
        unoGame game = gameList.get(gameId);
        if (!game.getGameStatus().equals(Status.GAME_START)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .header("Access-Control-Allow-Origin","*")
                    .build();
        } else {

            JsonArrayBuilder handCards = Json.createArrayBuilder();
            unoPlayer player = null;
            for (unoPlayer p : game.getGamePlayers()) {
                if (p.getName().equals(name)) {
                    player = p;
                }
            }
            for (unoCard c : player.getHandCards()) {
                JsonObject cards = Json.createObjectBuilder()
                        .add("card", c.getImage())
                        .build();
                handCards.add(cards);
            }
            return Response.ok(handCards.build())
                    .header("Access-Control-Allow-Origin","*")
                    .build();
        }
    }
}
