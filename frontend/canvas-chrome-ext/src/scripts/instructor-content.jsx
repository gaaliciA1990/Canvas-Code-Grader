console.log("Chrome ext ready");

const site = window.location.hostname;

function injectScript(file_path, tag) {

    let node = document.body;
    let script = document.createElement("script");
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', file_path);
    console.log(script);

    node.appendChild(script);
}
// reference the js file created by the webpack
injectScript(chrome.runtime.getURL("instructorInject.js"), 'body');

try {
    window.addEventListener("message", function (msg) {

        if (msg.data.type
            && (msg.data.type == "FROM_PAGE")) {

            console.log("sending message to background script");
            //console.log(msg.data.type);
            //console.log(msg.data.output);

            //by default do these msgs alaways go to BG?
            chrome.runtime.sendMessage({ type: "waiting", output: msg.data.output }, (response) => {
                console.log(response);
            });
        }

    }, false);
} catch (e) {
    console.log(e);
}

