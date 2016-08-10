$(function () {
    var basicUrl = "http://localhost:8080/api/UnoGame/";
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
    })
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
        $("#playerName").empty()
        $.post(basicUrl + "POST/joinGame/" + gameId + "/" + playerName)
                .done(function () {
                    $("#waitingGameId").text(gameId);
                    $("#player").text(playerName);
                    $.UIGoToArticle("#waitingGame");
                })
                .fail(function () {
                    alert("Can't Join the Game!");
                });
    });
    $("#refreshGameBtn").on("singletap", function () {
        var gameId = $("#waitingGameId").text();
        var playerName = $("#player").text();
        $.get(basicUrl + "GET/gameInfo/" + gameId + "/" + playerName)
                .done(function (result) {
                    for (var i = 0; i < result.length; i++)
                    {
                        var cardUrl = $('<li class="special">');
                        var img = $("<img>").attr("src", "img/" + result[i].card);
                        cardUrl.append(img);
                        $("#handCards").append(cardUrl);
                    }
                    $.UIGoToArticle("#gameStart");
                })
                .fail(function () {
                    alert("Game Not Start!");
                });
    });
});