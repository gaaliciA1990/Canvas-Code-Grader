
console.log("Chrome extension content script is ready");

/**
 * InjectScript used to create new DOM elements that are embedded into approved
 * web urls (mainly SU links).
 * @param file_path
 */
function injectScript(file_path){

    let node = document.body;
    //create a scripting DOM element and append it to the body of the webpage
    let script = document.createElement("script");
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', file_path);

    console.log(script);
    node.appendChild(script);
}

//pulling script file from chrome extension files
let script_url = chrome.runtime.getURL("scripts/inject_script.js");

//regex used to ensure url extension is injecting web elements onto the correct page
const regex = /^https:\/\/canvas\.instructure\.com\/courses\/[0-9][0-9][0-9][0-9][0-9][0-9][0-9]\/assignments/;

//event listener that makes sure all DOM elements are loaded prior to running injection script
document.addEventListener('readystatechange', event => {

    if (event.target.readyState === "complete") {
        if(window.location.toString().match(regex)) {
            console.log("matched regex");
            //onload inject the script
            //'ag-list'
            injectScript(script_url);
        }
    }
})


try {
    window.addEventListener("message", function(msg) {

        if (msg.data.type
            && (msg.data.type == "FROM_PAGE")) {

            console.log("sending message to background script");
            //console.log(msg.data.type);
            //console.log(msg.data.output);

            //by default do these msgs alaways go to BG?
            chrome.runtime.sendMessage({type: "waiting", output: msg.data.output }, (response) => {
                console.log(response);
            });
        }

    }, false);
}catch (e) {
    console.log(e);
}

