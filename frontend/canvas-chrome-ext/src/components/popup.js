
window.addEventListener('DOMContentLoaded', () => {
    let background = chrome.extension.getBackgroundPage();

    chrome.tabs.query({active:true, currentWindow:true}, (tabs) => {
        let currentTabId = tabs[0].id;
        let results = background.results[currentTabId];

        background.console.log(results);
    })


    document.getElementById("oAuth-signIn").addEventListener('click', canvasOauthRedirect);

    //oAuth redirect link should go here
    function canvasOauthRedirect(){
        chrome.tabs.create({active: true, url: "https://www.google.com/"});
    }


})


chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {

    if (request.msg === "done") {
        console.log("got msg from background page change text")
        document.getElementsByClassName("update-text").innerHTML = "Got the message";

        sendResponse("background recv msg");
    }

})



