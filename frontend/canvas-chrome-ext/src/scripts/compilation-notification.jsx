/**
 * script that is being used in compilation-notification.html
 */

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {

    if (msg.action === 'compilation_results') {
        console.log(msg.data);
        sendResponse("confirmed");

        document.getElementById('update-text').textContent  = JSON.stringify(msg.data, null, '  ');
    }
});