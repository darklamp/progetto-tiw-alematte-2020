function getInitialStyle(){
    if(localStorage.getItem("style") != null) return localStorage.getItem("style");
    else return window.matchMedia("(prefers-color-scheme:dark)").matches ? "dark" : "light";
}


let style = getInitialStyle();

let cssURL = document.getElementById("mainCssLink").href.replace("style","dark");

var mapStyle  = "mapbox://styles/darklamp/ck94eyqp01woc1iq7nbshmvmc/draft";

if (style === "dark"){
    $('head').append(`<link rel="stylesheet" id="darkStyleSheet" media="all" href="${cssURL}"/>`);
    mapStyle = "mapbox://styles/darklamp/ck9b3godx092k1iqs5c9zxuqm/draft";
}
else {
    $('head').append(`<link rel="stylesheet" id="darkStyleSheet" media="none" href="${cssURL}"/>`);
}

function switchStyle(){
    if (style === "light") {
        style = "dark";
    }
    else {
        style = "light";
    }
    setStyle(style);
}


function setStyle(style) {
    if (style === "dark") {
        document.getElementById('darkStyleSheet').media = "all";
        try{
            map.setStyle('mapbox://styles/darklamp/ck9b3godx092k1iqs5c9zxuqm/draft');
        }
        catch (e) {

        }
        localStorage.setItem("style", "dark");
    }
    else {
        document.getElementById('darkStyleSheet').media = "none";
        try{
            map.setStyle('mapbox://styles/darklamp/ck94eyqp01woc1iq7nbshmvmc/draft');
        }
        catch (e) {

        }
        localStorage.setItem("style", "light");
    }
}