/**
 * Event manager
 */

try {

    function onTabLoaded(tabId) {
        return new Promise(resolve => {
            chrome.tabs.onUpdated.addListener(function onUpdated(id, change) {
                if (id === tabId && change.status === 'complete') {
                    chrome.tabs.onUpdated.removeListener(onUpdated);
                    resolve();
                }
            });
        });
    }

    chrome.runtime.onMessage.addListener(async function (message, sender, sendResponse) {
        console.log(message);

        sendResponse("msg received from content");
        if (message.type === 'evaluation') {

            const notification_window = await chrome.tabs.create({
                url: chrome.runtime.getURL('compilation-notification.html'),
                active: false
            });
            await onTabLoaded(notification_window.id);

            await chrome.tabs.sendMessage(notification_window.id, {
                action: 'compilation_results',
                data: message.output,
            });

            //TODO does this need to be changed
            //After the tab has been created, open a window to inject the tab

            chrome.windows.create({
                tabId: notification_window.id,
                type: 'popup',
                focused: true,
                height: 600,
                width: 800
            })
        }
        else if(message.type === 'evaluate_error'){
            console.log("Error 424: The method could not be performed on the resource because the requested action" +
                " depended on another action and that action failed");

            const notification_window = await chrome.tabs.create({
                url: chrome.runtime.getURL('compilation-notification.html'),
                active: false
            });
            await onTabLoaded(notification_window.id);

            console.log("MESSAGE");
            console.log(message.output.fileSubmit_response);
            await chrome.tabs.sendMessage(notification_window.id, {
                action: 'compilation_results',
                data: message.output.fileSubmit_response.message,
            });

            //TODO does this need to be changed
            //After the tab has been created, open a window to inject the tab

            chrome.windows.create({
                tabId: notification_window.id,
                type: 'popup',
                focused: true,
                height: 600,
                width: 800
            })
        }
    })
}

catch (e) {
    console.log(e)
}


