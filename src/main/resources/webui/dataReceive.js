let theme;
let style;
let refreshRate;
let graphTimeFrame;
let doAnimations;
//non-option var
let statRequest;
//stat vars
let hitmiss,
    byte,
    cached,
    req;

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
    loadStuff();
    fetch("/api/allStats")
        .then(response => async function () {
            let respj = JSON.parse(await response.text());
            updateValues(respj);
            console.log(respj);
        });
    statRequest = setInterval(getStats, refreshRate);
});

function loadStuff() {
    hitmiss = new Chart(document.getElementById('hitpie').getContext('2d'), {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [0, 0, 0]
            }],
            labels: [
                'Hits',
                'Misses',
                'Browser Cached'
            ]
        },
        options: {}
    });
    req = new Chart(document.getElementById('requestsserved').getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Requests Served',
                backgroundColor: "#f00",
                borderColor: "#f00",
                data: [],
                fill: false
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });
    byte = new Chart(document.getElementById('bytessent').getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Bytes Sent',
                backgroundColor: "#f00",
                borderColor: "#f00",
                data: [],
                fill: false
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });
    cached = new Chart(document.getElementById('browsercached').getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Bytes On Disk',
                backgroundColor: "#f00",
                borderColor: "#f00",
                data: [],
                fill: false
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });
}

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
            graph_time_frame: 30000,
            do_animations: true,
        }
    }
    theme = options.theme;
    style = options.style;
    refreshRate = options.refresh_rate;
    graphTimeFrame = options.graph_time_frame;
    doAnimations = options.do_animations;
    $("#dataRefreshRate").val(refreshRate);
    $("#graphTimeFrame").val(graphTimeFrame);
    $("#themeIn").val(theme);
    $("#styleIn").val(style);
    $("#doAnimations").prop("checked", doAnimations);
}

function resetOptions() {
    if (confirm("Do you really want to reset all customizations to defaults?")) {
        $("#dataRefreshRate").val(5000);
        $("#graphTimeFrame").val(30000);
        $("#themeIn").val("lightTheme");
        $("#styleIn").val("sharpStyle");
        $("#doAnimations").prop("checked", true);
        selectTab('dash', 'dashb');
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
        do_animations: $("#doAnimations").prop("checked"),
        lock_dashboard: $("#lockDash").prop("checked")
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
        clearInterval(statRequest);
        statRequest = setInterval(getStats, refreshRate);
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
    if (options.graph_time_frame !== graphTimeFrame) {
        graphTimeFrame = Math.max(options.graph_time_frame, 5000);
        $("#graphTimeFrame").addClass("updated").on(
            "animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd",
            function () {
                $(this).removeClass("updated");
            }
        ).val(graphTimeFrame);
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
        $(document.body).children().each(function () {
            if (!($(this).attr("hidden")))
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
        $(document.body).children().each(function () {
            if (!($(this).attr("hidden")))
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

function getStats() {
    fetch("/api/stats")
        .then(response => response.json())
        .then(response => {
            updateValues(response);
        });
}

function updateValues(data) {
    for (let key in data) {
        if (data.hasOwnProperty(key)) {
            let x = data[key];
            hitmiss.data.datasets[0].data[0] = x.cache_hits;
            hitmiss.data.datasets[0].data[1] = x.cache_misses;
            hitmiss.data.datasets[0].data[2] = x.browser_cached;
            hitmiss.update();
            req.data.labels.push(key.substring(key.indexOf("T") + 1, key.indexOf("Z")));
            req.data.datasets.forEach((dataset) => {
                dataset.data.push(x.requests_served);
            });
            req.update();
            byte.data.labels.push(key.substring(key.indexOf("T") + 1, key.indexOf("Z")));
            byte.data.datasets.forEach((dataset) => {
                dataset.data.push(x.bytes_sent);
            });
            byte.update();
            cached.data.labels.push(key.substring(key.indexOf("T") + 1, key.indexOf("Z")));
            cached.data.datasets.forEach((dataset) => {
                dataset.data.push(x.bytes_on_disk);
            });
            cached.update();
        }
    }
    let points = graphTimeFrame / refreshRate;
    if (req.data.labels.length > points) {
        req.data.labels.splice(0, req.data.labels.length - points);
        req.data.datasets.splice(0, req.data.datasets.length - points);
    }
    if (byte.data.labels.length > points) {
        byte.data.labels.splice(0, byte.data.labels.length - points);
        byte.data.datasets.splice(0, byte.data.datasets.length - points);
    }
    if (cached.data.labels.length > points) {
        cached.data.labels.splice(0, cached.data.labels.length - points);
        cached.data.datasets.splice(0, cached.data.datasets.length - points);
    }
}
