
window.addEventListener('DOMContentLoaded', () => {
    let background = chrome.extension.getBackgroundPage();

    chrome.tabs.query({active:true, currentWindow:true}, (tabs) => {
        let currentTabId = tabs[0].id;
        let results = background.results[currentTabId];

        background.console.log(results);
    })
})


chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {

    if (request.msg === "done") {
        console.log("got msg from background page change text")
        document.getElementsByClassName("update-text").innerHTML = "Got the message";

        sendResponse("background recv msg");
    }

})