
$(function () {
    var basicUrl = "http://localhost:8080/api/UnoGame/";
    $("#create").on("singletap", function () {
        var gameTitle = $("#gameTitle").val();
        var capacity = $("#capacity").val();
        $("#gameTitle").empty();
        $("#capacity").empty();
        $.post(basicUrl + "POST/game/" + gameTitle + "/" + capacity)
                .done(function (result) {
                    $("#waitGameId").text(result.gameId);
                    $("#waitGameTitle").text(result.gameTitle);
                    $("#waitGameCapacity").text(result.capacity);
                    $.UIGoToArticle("#waitGame");
                });
    });
    $("#refreshBtn").on("singletap", function () {
        var gameId = $("#waitGameId").text();
        $.getJSON(basicUrl + "Get/Count/" + gameId).done(function (result) {
            $("#playerAmount").text(result.amount);
        });
    });
    $("#gameStartBtn").on("singletap", function () {
        var gameId = $("#waitGameId").text();
        if ($("#playerAmount").text() <= 1) {
            alert("Can't Start the Game");
            return;
        }
        $.post(basicUrl + "POST/Start/" + gameId).done(function (result) {
            $("#discrad").attr("src", "img/" + result[0].img2);
            var playerTemplate = Handlebars.compile($("#playerTemplate").html());
            for (var i = 1; i < result.length; i++) {
                $("#playerList").append(playerTemplate(result[i]));
            }
            $.UIGoToArticle("#gameTable");
        });
    });
});