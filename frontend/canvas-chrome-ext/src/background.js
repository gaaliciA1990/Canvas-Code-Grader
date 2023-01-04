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

        sendResponse("msg recieved from content");
        if (message.type === 'waiting') {

            const notification_window = await chrome.tabs.create({
                url: chrome.runtime.getURL('components/compilation_notification.html'),
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
                height: 500,
                width:600
                // incognito, top, left, ...
            })

        }
    })
}

catch (e) {
    console.log(e)
}


