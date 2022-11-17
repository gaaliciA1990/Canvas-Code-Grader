
window.addEventListener('DOMContentLoaded', () => {
    let background = chrome.extension.getBackgroundPage();

    chrome.tabs.quert({active:true, currentWindow:true}, (tabs) => {
        let currentTabId = tabs[0].id;
        let results = background.results[currentTabId];

        background.console.log(results);
    })
})