/*chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
        console.log(sender);
        console.log(request.message);
        sendResponse("complete");

        return true;
    }
);*/


window.results = {};

crhome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    console.log(message.output);
    window.results[sender.tab.id] = message.output;
})