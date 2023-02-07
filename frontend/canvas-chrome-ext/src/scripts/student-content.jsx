console.log("Chrome ext ready");


function injectScript(file_path) {
    let node = document.body;
    let script = document.createElement("script");
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', file_path);
    console.log(script);
    node.appendChild(script);
}

// reference the js file created by the webpack
let script_url = chrome.runtime.getURL("studentInject.js");

//event listener that makes sure all DOM elements are loaded prior to running injection script
document.addEventListener('readystatechange', event => {
    if (event.target.readyState === "complete") {
        //onload inject the script
        injectScript(script_url, 'ag-list');
    }
})


try {
    window.addEventListener("message", function (msg) {

        if (msg.data.type
            && (msg.data.type == "FROM_PAGE")) {

            console.log("sending message to background script");
            //console.log(msg.data.type);
            //console.log(msg.data.output);

            //by default do these msgs alaways go to BG?
            chrome.runtime.sendMessage({type: "waiting", output: msg.data.output}, (response) => {
                console.log(response);
            });
        }

    }, false);
} catch (e) {
    console.log(e);
}

