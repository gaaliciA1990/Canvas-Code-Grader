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

    async function windowCreation(action, data, log_statement) {

        if(log_statement !== undefined){
            console.log(log_statement);
        }

        const notification_window = await chrome.tabs.create({
            url: chrome.runtime.getURL('compilation-notification.html'),
            active: false
        });
        await onTabLoaded(notification_window.id);

        await chrome.tabs.sendMessage(notification_window.id, {
            action: action,
            data: data,
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

    chrome.runtime.onMessage.addListener(async function (message, sender, sendResponse) {
        console.log(message);
        let log_statement;

        sendResponse("Message received from content");
        if (message.type === 'evaluation') {
            await windowCreation('compilation_results', message.output.fileSubmit_response);
        }
        else if(message.type === 'evaluate_error'){

            log_statement = "Error 424: The method could not be performed on" +
                " the resource because the requested action" +
                " depended on another action and that action failed";

            await windowCreation('424error', message.output.fileSubmit_response, log_statement);
        }
        else if(message.type === 'internal_server_error'){
            log_statement = "ERROR: 500\n " +
                "Internal Server Error";
            await windowCreation('500error', message.output.fileSubmit_response, log_statement);
        }
    })
}

catch (e) {
    console.log(e)
}


