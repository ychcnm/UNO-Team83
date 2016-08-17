
$(function () {
    var connection = null;
    var gameTitle;
    var capacity;
    var gameId;
    var imgPath = "http://localhost:8080/Uno83Server/img/";

    var basicUrl = "http://localhost:8080/Uno83Server/api/UnoGame/";
    $("#create").on("singletap", function () {
        gameTitle = $("#gameTitle").val();
        capacity = $("#capacity").val();

        $("#gameTitle").empty();
        $("#capacity").empty();

        $.post(basicUrl + "POST/game/" + gameTitle + "/" + capacity)
                .done(function (result) {
                    gameId = result.gameId;

                    $("#waitGameId").text(gameId);
                    $("#waitGameTitle").text(result.gameTitle);
                    $("#waitGameCapacity").text(result.capacity);
                    $.UIGoToArticle("#waitGame");

                    connection = new WebSocket("ws://localhost:8080/Uno83Server/table/" + gameId + "/" + "table");
                    connection.onopen = function () {
                        console.log(">> Game Has Been Created");
                    };
                    connection.onmessage = function (msg) {

                        console.info("incoming: %s", msg.data);
                        var msg = JSON.parse(msg.data);

                        switch (msg.msg) {
                            case "refresh":
                            {
                                $("#players").empty();
                                var l = $("<h3>").text("Player List");
                                $("#players").append(l);


                                $("#playerAmount").text(msg.count);

                                for (var i in msg.players)
                                {
                                    var player = $("<h4>").text(msg.players[i]);
                                    $("#players").append(player);
                                }
                                break;
                            }
                            case "start":
                            {
                                $("#discrad").attr("src", imgPath + msg.pile);
                                var playerTemplate = Handlebars.compile($("#playerTemplate").html());
                                for (var i in msg.players) {
                                    var profile = $("<img>").attr("src", imgPath + "Fred.jpg");
                                    var player = $("<h3>").text(msg.players[i]);
                                    $("#playerList").append(profile).append(player);
                                }
                                $("#cardRemain").text(msg.cardsCount);
                                $.UIGoToArticle("#gameTable");
                                break;
                            }
                            case "forceStart":
                                $("#gameStartBtn").trigger("singletap");
                                break;
                            case "cantStart":
                                alert("Can't Start this Game!");
                                break;
                            case "changePile":
                                $("#discrad").attr("src", imgPath + msg.pile);
                                break;
                            case "refreshCardAmount":
                                $("#cardRemain").text(msg.amount);
                                break;
                            case "gameEnd":
                                {
                                    var rankTemplate = Handlebars.compile($("#rankTemplate").html());
                                    var lRank = rankTemplate({players: msg.players});
                                    $("#Rank").append(lRank);
                                    $.UIGoToArticle("#gameEnd");
                                }
                                break;
                            default:
                                break;
                        }
                    };
                });
    });
//    $("#refreshBtn").on("singletap", function () {
//        var gameId = $("#waitGameId").text();
//        $.getJSON(basicUrl + "Get/Count/" + gameId).done(function (result) {
//            $("#playerAmount").text(result.amount);
//        });
//    });
    $("#gameStartBtn").on("singletap", function () {
        gameId = $("#waitGameId").text();
        var message = {
            msg: "startGame",
            gameId: gameId
        };
        connection.send(JSON.stringify(message));
//        $.post(basicUrl + "POST/Start/" + gameId).done(function (result) {
//            $("#discrad").attr("src", "http://localhost:8080/Uno83Server/img/" + result[0].img2);
//            var playerTemplate = Handlebars.compile($("#playerTemplate").html());
//            for (var i = 1; i < result.length; i++) {
//                $("#playerList").append(playerTemplate(result[i]));
//            }
//            $.UIGoToArticle("#gameTable");
//        });
    });
    $("#btnGameEnd").on("singletap", function () {
        var message = {
            msg: "endGame",
            gameId: gameId
        };
        connection.send(JSON.stringify(message));
    });
});