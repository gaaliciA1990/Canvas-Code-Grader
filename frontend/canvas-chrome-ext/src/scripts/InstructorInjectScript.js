console.log("inside InstructorInjectScript.js")

fetch(chrome.runtime.getURL('components/submission_read_only_view.html')).then(r => r.text()).then(html => {
    document.body.insertAdjacentHTML('beforeend', html);
    // not using innerHTML as it would break js event listeners of the page
});