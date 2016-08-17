$(function () {
    var connection = null;
    var gameId;
    var playerName;
    var basicUrl = "http://localhost:8080/Uno83Server/api/UnoGame/";
    var imgPath = "http://localhost:8080/Uno83Server/img/";
    $("#gameListRefresh").on("singletap", function () {
        $.getJSON(basicUrl + "GET/gameList")
                .done(function (result) {
                    $("#gameList").html("");
                    var listTemplate = Handlebars.compile($("#listTemplate").html());
                    var count = 0;
                    for (var i in result) {
                        count++;
                        var game = result[i];
                        $("#gameList").append(listTemplate(game));
                    }
                    $("#gameAmount").text(count);
                })
                .fail(function () {
                    alert("Can't Get the Game List!");
                });
    });
    $("#gameListRefresh").trigger("singletap");

    $("#gameList").on("doubletap", "li", function () {
        var id = $(this).find("#selected-id").text();
        if ($(this).find("#selected-status").text() === "GAME_START") {
            alert("The Game Already Started!");
            return;
        }
        $("#detailsGameId").text(id);
        $.UIGoToArticle("#player_details");
    });

    $("#back_game_list").on("singletap", function () {
        $.UIGoBack();
        $("#playerName").empty();
        $("#gameListRefresh").trigger("singletap");
    });
    $("#joinBtn").on("singletap", function () {
        gameId = $("#detailsGameId").text();
        playerName = $("#playerName").val();
        $("#playerName").empty();

        $.UIGoToArticle("#waitingGame");

        connection = new WebSocket("ws://localhost:8080/Uno83Server/table/" + gameId + "/" + playerName);
        connection.onopen = function () {
            console.log(">> The Player Has Joined in");
            $("#waitingGameId").text(gameId);
            $("#player").text(playerName);
            $.UIGoToArticle("#waitingGame");
            var message = {
                msg: "joinGame",
                gameId: gameId
            };
            connection.send(JSON.stringify(message));
        };

        connection.onmessage = function (msg) {
            console.info("incoming: %s", msg.data);

            var msg = JSON.parse(msg.data);
            var turn;
            switch (msg.msg) {
                case "deal":

                        turn = $("<h1>").text(msg.turn);
                        $("#turn").append(turn);
                        fan(msg, imgPath);
                        $("#playerNameInGame").text("Player: " + playerName);
                        $("#score").text("Score: " + msg.score);
                        $.isNavigating = false; //hack to make this work
                        $.UIGoToArticle("article#game_start");
                    
                    break;
                case "cantJoin":
                    alert("Can't Join this Game!");
                    break;
                case "returnCard":
                    
                        alert("Can't throw this card!");
                        $("#handCards").empty();
                        fan(msg, imgPath);
                    
                    break;
                case "refreshCard":
                    
                        $("#turn").empty();
                        turn = $("<h1>").text(msg.turn);
                        $("#turn").append(turn);
                        $("#handCards").empty();
                        $("#score").text("Score: " + msg.score);
                        fan(msg, imgPath);
                    
                    break;
                case "shift":
                    
                        $("#turn").empty();
                        turn = $("<h1>").text(msg.turn);
                        $("#turn").append(turn);
                    
                    break;
                case "gameEnd":
                    
                        var rankTemplate = Handlebars.compile($("#rankTemplate").html());
                        var lRank = rankTemplate({players: msg.players});
                        $("#Rank").append(lRank);
                        $.UIGoToArticle("#gameEnd");
                    
                    break;
                default:
                
                    break;
                
            }

        };
//        $.post(basicUrl + "POST/joinGame/" + gameId + "/" + playerName)
//                .done(function () {
//                    $("#waitingGameId").text(gameId);
//                    $("#player").text(playerName);
//                    $.UIGoToArticle("#waitingGame");
//                })
//                .fail(function () {
//                    alert("Can't Join the Game!");
//                });
    });
    $("#handCards").on("doubletap", ".special", function () {
        var first = $(this);
        var next = $(this).nextAll();
        var message = {
            msg: "throwCard",
            gameId: gameId,
            playerName: playerName,
            card: $(this).children().attr("cid")
        };
        if ($("#turn").children().text() === "It is not your turn") {
            alert("Not Your Turn Can't Throw");
            return;
        }
        next.each(function () {
            var left = $(this).position().left;
            left = left - 80;
            $(this).css("left", left + "px");
        });
        connection.send(JSON.stringify(message));

        first.remove();

    });
    $("#btnDrawCards").on("singletap", function () {
        if ($("#turn").text() === "It is not your turn")
        {
            alert("Can't draw Card It's Not Your Turn!");
            return;
        }
        var message = {
            msg: "drawCard",
            gameId: gameId,
            playerName: playerName,
        };
        connection.send(JSON.stringify(message));
    });
//    $("#refreshGameBtn").on("singletap", function () {
//        var gameId = $("#waitingGameId").text();
//        var playerName = $("#player").text();
//        $.get(basicUrl + "GET/gameInfo/" + gameId + "/" + playerName)
//                .done(function (result) {
//                    for (var i = 0; i < result.length; i++)
//                    {
//                        var cardUrl = $('<li class="special">');
//                        var img = $("<img>").attr("src", "http://localhost:8080/Uno83Server/img/" + result[i].card);
//                        cardUrl.append(img);
//                        $("#handCards").append(cardUrl);
//                    }
//                    $.UIGoToArticle("#gameStart");
//                })
//                .fail(function () {
//                    alert("Game Not Start!");
//                });
//    });
});
function fan(msg, imgPath) {
    for (var i in msg.hands)
    {
        var num = msg.handsCount;
        var perA;
        var perT;
        var perL;


        perA = 90 / (num - 1);

        perL = 40 / num;
        perT = 80 / ((num - 1) / 2);

        var ang = -45 + i * perA;
        var posL = 30 + i * perL;
        var posT = 200;

        if (i < (num / 2)) {
            posT = posT - i * perT;
        } else {
            posT = posT - (num - 1 - i) * perT;
        }
        var cardUrl = $('<li class="special">')
                .css('left', posL + '%')
                .css('top', posT + 'px')
                .css('transform', 'rotate(' + ang + 'deg)');
        var img = $("<img>")
                .attr("src", imgPath + msg.hands[i])
                .attr("cid", msg.hands[i]);
        cardUrl.append(img);
        $("#handCards").append(cardUrl);
    }
}