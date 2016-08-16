$(function () {
    var connection = null;
    var basicUrl = "http://localhost:8080/Uno83Server/api/UnoGame/";
    $("#gameListRefresh").on("singletap", function () {
        $.getJSON(basicUrl + "GET/gameList")
                .done(function (result) {
                    $("#gameList").html("");
                    var listTemplate = Handlebars.compile($("#listTemplate").html());
                    var count = 0;
                    for (var i in result) {
                        count++;
                        var game = result[i];
                        game.playerAmount--;
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
        if ($(this).find("#selected-status").text() == "GAME_STARTED") {
            alert("The Game Already Started!");
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
        var gameId = $("#detailsGameId").text();
        var playerName = $("#playerName").val();
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
            switch (msg.msg) {
                case "deal":
                    {
                        for (var i in msg.hands)
                        {
                            var pos = i * 80 + 80;
                            var cardUrl = $('<li class="special">')
                                    .css("left", pos + "px")
                                    .css("z-index", i);
                            var img = $("<img>")
                                    .attr("src", "http://localhost:8080/Uno83Server/img/" + msg.hands[i])
                                    .attr("cID", msg.hands[i]);
                            cardUrl.append(img);
                            $("#handCards").append(cardUrl);
                        }
                        $.UIGoToArticle("#gameStart");
                    }
                    break;
                case "cantJoin":
                    alert("Can't Join this Game!");
                    break;
                default:
                {
                    break;
                }
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

        next.each(function () {
            var left = $(this).position().left;
            left = left - 80;
            $(this).css("left", left + "px");
        });
        
        
        first.remove();
        
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