let connection;
let theme;
let style;
let port;
let ip;
let refreshRate;
let maxConsoleLines;
let graphTimeFrame;
let showConsoleLatest;
let doAnimations;
//non-option var
let statRequest;
//stat vars


jQuery(document).ready(function () {
    loadOptions();
    $("#theme").attr("href", "themes/" + theme + ".css");
    $("#style").attr("href", "themes/" + style + ".css");
    if (doAnimations) {
        $(".optionInput").addClass("smooth");
        $(".slider").addClass("smoothslider").addClass("smooth");
        $(".content").addClass("slide_up");
        $(".sideOption").addClass("smooth");
        $(".button").addClass("smooth");
    }
    if (showConsoleLatest)
        $("#consoleLatest").attr("hidden", false);
    reconnect();
    $("#console_input").keyup(function (e) {
        if (e.keyCode === 13) {
            sendCommand($(this).text());
            $(this).text("");
            $('#console_text').scrollTop($("#console_text")[0].scrollHeight)
        }
    })
});

//site functions, no connections involved

$(window).on("click", function () {
    let sideBar = $("#sideBar");
    if (sideBar.hasClass("expanded")) {
        sideBar.removeClass("expanded").addClass("retract").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("retract").removeClass("expanded");
            }
        );
    }
});

function loadOptions() {
    let options = JSON.parse(localStorage.getItem("options"));
    if (options === null) {
        options = {
            refresh_rate: 5000,
            theme: "lightTheme",
            style: "sharpStyle",
            client_port: 33333,
            client_ip: "localhost",
            max_console_lines: 1000,
            show_console_latest: false,
            graph_time_frame: 30000,
            do_animations: true
        }
    }
    theme = options.theme;
    style = options.style;
    port = options.client_port;
    ip = options.client_ip;
    refreshRate = options.refresh_rate;
    maxConsoleLines = options.max_console_lines;
    graphTimeFrame = options.graph_time_frame;
    showConsoleLatest = options.show_console_latest;
    doAnimations = options.do_animations;
    $("#dataRefreshRate").val(refreshRate);
    $("#port").val(port);
    $("#ip").val(ip);
    $("#maxConsoleLines").val(maxConsoleLines);
    $("#graphTimeFrame").val(graphTimeFrame);
    $("#themeIn").val(theme);
    $("#styleIn").val(style);
    $("#newestconsole").prop("checked", showConsoleLatest);
    $("#doAnimations").prop("checked", doAnimations);
}

function resetOptions() {
    if (confirm("Do you really want to reset to defaults?")) {
        $("#dataRefreshRate").val(5000);
        $("#port").val(33333);
        $("#ip").val("localhost");
        $("#maxConsoleLines").val(1000);
        $("#graphTimeFrame").val(30000);
        $("#themeIn").val("lightTheme");
        $("#styleIn").val("sharpStyle");
        $("#newestconsole").prop("checked", false);
        $("#doAnimations").prop("checked", true);
        applyOptions()
    }
}

function applyOptions() {
    let options = {
        refresh_rate: parseInt($("#dataRefreshRate").val()),
        theme: $("#themeIn").val(),
        style: $("#styleIn").val(),
        client_port: parseInt($("#port").val()),
        client_ip: $("#ip").val(),
        max_console_lines: parseInt($("#maxConsoleLines").val()),
        show_console_latest: $("#newestconsole").prop("checked"),
        graph_time_frame: parseInt($("#graphTimeFrame").val()),
        do_animations: $("#doAnimations").prop("checked")
    };
    if (options.do_animations !== doAnimations) {
        doAnimations = options.do_animations;
        if (doAnimations) {
            $(".optionInput").addClass("smooth");
            $(".slider").addClass("smoothslider").addClass("smooth");
            $(".content").addClass("slide_up");
            $(".sideOption").addClass("smooth");
            $(".button").addClass("smooth");
        } else {
            $(".optionInput").removeClass("smooth");
            $(".slider").removeClass("smoothslider").removeClass("smooth");
            $(".content").removeClass("slide_up");
            $(".sideOption").removeClass("smooth");
            $(".button").removeClass("smooth");
        }
        $("#doAnimationscb").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).prop("checked", doAnimations);
    }
    if (options.refresh_rate !== refreshRate) {
        console.log(options.refresh_rate + " " + refreshRate);
        refreshRate = Math.max(options.refresh_rate, 500);
        $("#dataRefreshRate").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).val(refreshRate);
    }
    if (options.style !== style) {
        style = options.style;
        applyStyle(options.style);
        $("#styleIn").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        );
    }
    if (options.theme !== theme) {
        theme = options.theme;
        applyTheme(options.theme);
        $("#themeIn").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        );
    }
    if (options.client_port !== port) {
        port = options.client_port;
        $("#port").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).val(port);
        reconnect();
    }
    if (options.client_ip !== ip) {
        ip = options.client_ip;
        $("#ip").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).val(ip);
        reconnect();
    }
    if (options.graph_time_frame !== graphTimeFrame) {
        graphTimeFrame = Math.max(options.graph_time_frame, 5000);
        $("#graphTimeFrame").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).val(graphTimeFrame);
    }
    if (options.max_console_lines !== maxConsoleLines) {
        maxConsoleLines = Math.max(options.max_console_lines, 100);
        $("#maxConsoleLines").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).val(maxConsoleLines);
    }
    if (options.show_console_latest !== showConsoleLatest) {
        showConsoleLatest = options.show_console_latest;
        if (showConsoleLatest)
            $("#consoleLatest").attr("hidden", false);
        else
            $("#consoleLatest").attr("hidden", true);
        $("#newestconsolecb").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).prop("checked", showConsoleLatest);
    }
    localStorage.setItem("options", JSON.stringify(options));
}

function selectTab(t, l) {
    let sideBar = $("#sideBar");
    sideBar.children("div").each(function () {
        let tmp = $(this);
        if (tmp.attr("id") === t) {
            tmp.addClass("sideSelected");
        } else
            tmp.removeClass("sideSelected");
    });
    $("#content").children("div").each(function () {
        let tmp = $(this);
        if (tmp.attr("id") === l) {
            tmp.attr("hidden", false);
        } else
            tmp.attr("hidden", true);
    });
    if (sideBar.hasClass("expanded")) {
        sideBar.removeClass("expanded").addClass("retract").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("retract").removeClass("expanded");
            }
        );
    }
}

function expSide() {
    let sideBar = $("#sideBar");
    if (sideBar.hasClass("expanded")) {
        sideBar.removeClass("expanded").addClass("retract").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("retract").removeClass("expanded");
            }
        );
    } else {
        sideBar.addClass("expand").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).addClass("expanded").removeClass("expand");
            }
        );
    }

}

function applyTheme(t) {
    if (doAnimations)
        $("*").each(function () {
            if (!$(this).attr("hidden"))
                $(this).addClass("tempsmooth").on(
                    "webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend",
                    function () {
                        $(this).removeClass("tempsmooth");
                    }
                );
        });
    $("#theme").attr("href", "themes/" + t + ".css");
}

function applyStyle(s) {
    if (doAnimations)
        $("*").each(function () {
            if (!$(this).attr("hidden"))
                $(this).addClass("tempsmooth").on(
                    "webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend",
                    function () {
                        $(this).removeClass("tempsmooth");
                    }
                );
        });
    $("#style").attr("href", "themes/" + s + ".css");
}

//update data functions

function updateWithMessage(m) {
    //TODO: get this to talk with client
    let result;
    try {
        result = JSON.parse(m);
        switch (result.type) {
            case "command":
                updateConsole(result.data, 2);
                break;
            case "stats":

                updateValues();
                break;
            default:
                updateConsole("[WEB-INFO] The message received is improperly formatted: " + result.data, 2);
                break;
        }
    } catch (e) {
        updateConsole("[WEB-INFO] There was an error parsing the data \n" + e, 2);
    }
}

function updateValues() {
    //TODO: use values and update web info
}

//console functions

function updateConsole(x, status) {
    let scroll = false;
    let temp = $('#console_text');
    let latest = $("#consoleLatest");
    if (temp.scrollTop() === (temp[0].scrollHeight - temp[0].clientHeight))
        scroll = true;
    switch (status) {
        case 1:
            temp.append('<div class="consoleLine sent">' + x + '</div>');
            break;
        case 0:
            temp.append('<div class="consoleLine unsent">' + x + '</div>');
            break;
        default:
            temp.append('<div class="consoleLine">' + x + '</div>');
            latest.html('<div class="consoleLine">' + x + '</div>');
    }
    let childs = temp.children();
    if (childs.length > maxConsoleLines) {
        let length = childs.length;
        for (let i = 0; i < length - maxConsoleLines; i++) {
            childs[i].remove();
        }
    }
    if (scroll)
        temp.scrollTop(temp[0].scrollHeight);
}

function sendCommand(x) {
    if (x === "")
        return;
    if (connection.readyState === "OPEN") {
        let data = {
            type: "command",
            data: x
        };
        let message = JSON.stringify(data);
        connection.send(message);
    } else {
        updateConsole(x, 0);
    }
}

//network commuication

function reconnect() {
    if (connection != null)
        connection.close();
    updateConsole("[WEB-CONSOLE] Attempting to connect to client on " + ip + ":" + port, 2);
    connection = new WebSocket("ws://" + ip + ":" + port);
    $("#connection").removeClass("disconnected").removeClass("connected").addClass("connecting").text("Connecting");
    addListeners(connection)
}

function addListeners(c) {
    let opened = false;
    c.onopen = function (event) {
        $("#connection").removeClass("disconnected").removeClass("connecting").addClass("connected").text("Connected");
        opened = true;
        updateConsole("[WEB-CONSOLE] Successfully to connect to client on " + ip + ":" + port, 2);
        statRequest = setInterval(function () {
            requestStats();
        }, refreshRate);
    };
    c.onclose = function (event) {
        $("#connection").addClass("disconnected").removeClass("connecting").removeClass("connected").text("Disconnected");
        if (opened)
            updateConsole("[WEB-CONSOLE] Disconnected from client");
        else
            updateConsole("[WEB-CONSOLE] Failed to connect to client on " + ip + ":" + port, 2);
        clearInterval(statRequest);
    };
    c.onmessage = function (event) {
        updateWithMessage(event.data());
    };
}

function requestStats() {
    let req = {type: "stats"};
    connection.send(JSON.stringify(req));
}