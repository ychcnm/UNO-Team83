
import model.Table;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import enums.Status;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import model.unoGame;

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

    @Inject
    private Table table;

    @POST
    @Path("POST/game/{gameTitle}/{capacity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(@PathParam("gameTitle") String title, @PathParam("capacity") int capacity) {
        UUID id = UUID.randomUUID();
        String gameId = id.toString().substring(0, 6);
        unoGame game = new unoGame(gameId, title, Status.GAME_WAITING, capacity);
        table.getGameList().put(gameId, game);
        System.out.print(">> Create the game：ID:" + gameId + " TITLE:" + title);
        return Response.ok(game.toJson())
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @GET
    @Path("GET/gameList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGameList() {
        System.out.print(">> Get the Game List");
        JsonArrayBuilder gameJsonArray = Json.createArrayBuilder();
        for (Map.Entry<String, unoGame> game : table.getGameList().entrySet()) {
            gameJsonArray.add(game.getValue().toJson());
        }
        return Response.ok(gameJsonArray.build())
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
}
