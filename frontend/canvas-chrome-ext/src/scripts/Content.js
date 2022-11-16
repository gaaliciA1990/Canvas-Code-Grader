
console.log("Chrome ext ready");

const site = window.location.hostname;
alert("Canvas-Code JS has been injected into: " + site);

if (site.includes("https://seattleu.instructure.com/")) {
    alert("inside SU");
}

if (site.includes("https://canvas.instructure.com/")) {
    alert("inside canvas");
}

function injectScript(file_path, tag){
    var node = document.getElementsByTagName(tag)[0];
    var script = document.createElement("script");
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', file_path);
    node.appendChild(script);
}

injectScript(chrome.extension.getURL('scripts/inject_script.js'), 'body');
