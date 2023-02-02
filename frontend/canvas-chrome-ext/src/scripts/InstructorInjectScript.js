console.log("inside InstructorInjectScript.js")

beginUrlChangeListener();

// Listen for on initial page load for student_id to get appended 
// and when new student is clicked in speedgrader
async function beginUrlChangeListener() {
    let previousUrl = "";

    const urlObserver = new MutationObserver(async function (mutations) {
        if (window.location.href !== previousUrl) {
            console.log(`URL changed from ${previousUrl} to ${window.location.href}`);
            previousUrl = window.location.href;

            // TODO: call delete API to erase previous student submission directory

            if (studentHasSubmission()) {
                // On URL change, update UI with new student submission data
                await updateStudentSubmissionView();
            } else {
                console.log("student missing submission, did not update view");
            }
        }
    });

    const config = { subtree: true, childList: true };

    // Begin listener for URL changes
    urlObserver.observe(document, config);
}

async function updateStudentSubmissionView() {
    const params = getParameters();

    let endpoint = `http://localhost:8080/submission/courses/${params.courseId}/assignments/${params.assignmentId}/?`

    await fetch(endpoint + new URLSearchParams({
        studentId: params.studentId,
        userType: params.userType
    }), {
        method: "GET",
        headers: new Headers({
            'Authorization': params.bearerToken
        })
    }).catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
            console.log(responseJson.submissionFiles);

            // Check current student page matches the studentId in case
            // new student submission was clicked before API call is resolved
            if (window.location.href.includes(params["studentId"])) {
                generateReadOnlyCodeView(responseJson.submissionFiles);
                generateTerminalView(responseJson.submissionDirectory);
            }
        });
}

function studentHasSubmission() {
    return document.getElementById("this_student_does_not_have_a_submission").style.display === "none";
}

function getParameters() {
    let canvasUrl = window.location.href;
    console.log(canvasUrl);

    const urlSearchParamsObj = new URLSearchParams(window.location.search);
    const queryParams = Object.fromEntries(urlSearchParamsObj.entries());

    console.log('query params: ' + queryParams);

    let urlParts = canvasUrl.split('/');
    console.log(urlParts);

    let params = {};

    for (var i = 0; i < urlParts.length; i++) {
        if (urlParts[i] === "courses") {
            params.courseId = urlParts[i + 1];
        }
    }

    params.bearerToken = "Bearer 7~c5V3FHLUmCwn8II4CvwhMOqZ5HLjxwRt8mVZIspclX9hzlSx6aHg493QMtYidwXp";
    params.assignmentId = queryParams["assignment_id"];
    params.studentId = queryParams["student_id"];
    params.userType = "GRADER";
    console.log(params)

    return params;
}

function generateReadOnlyCodeView(submissionFiles) {
    document.getElementById("iframe_holder").style.display = "none";

    let readOnlyContainer = initReadOnlyContainer();
    let tabContainer = initTabContainer();
    let codeContainer = initCodeContainer();
    let darkModeButton = initDarkModeButton();

    readOnlyContainer.appendChild(darkModeButton);
    readOnlyContainer.appendChild(tabContainer);
    readOnlyContainer.appendChild(codeContainer);

    let isInDarkMode = false;
    let fileName = submissionFiles[0].name;
    let previousCodeWindowId = getCodeWindowId(fileName);
    let previousTabId = getTabId(fileName)

    for (var i = 0; i < submissionFiles.length; i++) {
        let content = submissionFiles[i].fileContent;
        let name = submissionFiles[i].name;

        let tab = initTabElement(name);
        tabContainer.appendChild(tab);

        var codeWindow;
        if (i == 0) {
            codeWindow = initCodeWindow(name, true);
            tab.style.backgroundColor = "#ccc";
        } else {
            codeWindow = initCodeWindow(name, false);
        }

        let formattedCodeElement = formatCodeView(name, content);
        codeWindow.appendChild(formattedCodeElement);
        codeContainer.appendChild(codeWindow);

        tab.addEventListener("click", function () {
            document.getElementById(previousTabId).style.backgroundColor = "#f1f1f1";
            document.getElementById(previousCodeWindowId).style.display = "none";
            let currentTabId = getTabId(name);
            let currentCodeWindowId = getCodeWindowId(name);
            previousTabId = currentTabId;
            previousCodeWindowId = currentCodeWindowId;
            document.getElementById(currentTabId).style.backgroundColor = "#ccc";
            document.getElementById(currentCodeWindowId).style.display = "block";
        });

        tab.addEventListener("mouseenter", () => {
            let previousColor = tab.style.backgroundColor;
            tab.style.backgroundColor = "#ddd";

            tab.addEventListener("mouseleave", () => {
                if (document.getElementById(getCodeWindowId(name)).style.display === "none") {
                    tab.style.backgroundColor = previousColor;
                }
            })
        });

        darkModeButton.addEventListener("click", function () {
            let codeContainerChildElements = codeContainer.getElementsByTagName("textarea");
            for (var i = 0; i < codeContainerChildElements.length; i++) {
                if (isInDarkMode) {
                    let textAreaElement = codeContainerChildElements[i];
                    textAreaElement.style.backgroundColor = "#f1f1f1";
                    textAreaElement.style.color = "black"
                } else {
                    let textAreaElement = codeContainerChildElements[i];
                    textAreaElement.style.backgroundColor = "black";
                    textAreaElement.style.color = "white";
                }
            }

            // Flip to other mode
            isInDarkMode = !isInDarkMode;
        });

    }

    document.getElementById("submissions_container").prepend(readOnlyContainer);
}

function generateTerminalView(submissionDirectory) {
    // TODO: terminal view function
}

function initReadOnlyContainer() {
    let readOnlyContainer = document.createElement("div");
    readOnlyContainer.id = "read-only-container";
    readOnlyContainer.style.margin = "20px";
    readOnlyContainer.style.height = "50%";
    return readOnlyContainer;
}

function initTabContainer() {
    let tabContainer = document.createElement("div");
    tabContainer.id = "tab-container";
    tabContainer.style.border = "2px solid #43A6C6";
    tabContainer.style.backgroundColor = "#f1f1f1";
    return tabContainer;
}

function initCodeContainer() {
    let codeContainer = document.createElement("div");
    codeContainer.id = "code-container";
    codeContainer.style.height = "100%";

    return codeContainer;
}

function initTabElement(fileName) {
    let tab = document.createElement("button");
    tab.id = getTabId(fileName);
    tab.textContent = fileName;
    tab.style.transition = "0.3s";
    tab.style.border = "none";
    tab.style.padding = "14px 16px";
    return tab;
}

function initCodeWindow(fileName, isDisplayed) {
    let codeWindow = document.createElement("div");
    codeWindow.id = getCodeWindowId(fileName);
    codeWindow.style.display = isDisplayed ? "inline-block" : "none";
    codeWindow.style.height = "100%";
    codeWindow.style.width = "100%";
    return codeWindow;
}

function initDarkModeButton() {
    let darkModeButton = document.createElement("button");
    darkModeButton.textContent = "Toggle Dark Mode";
    darkModeButton.style.padding = "10px";
    darkModeButton.style.margin = "5px";
    return darkModeButton;
}

function formatCodeView(name, content) {
    let formattedCodeElement = document.createElement("textarea")
    formattedCodeElement.id = name + "-textarea"

    for (var i = 0; i < content.length; i++) {
        formattedCodeElement.value += content[i] + "\r\n";
    }

    console.log("formatted code:" + formattedCodeElement.value);

    formattedCodeElement.style.width = "100%";
    formattedCodeElement.style.height = "100%";
    formattedCodeElement.style.boxSizing = "border-box";
    formattedCodeElement.style.fontFamily = "Consolas"
    formattedCodeElement.readOnly = true;
    formattedCodeElement.style.cursor = "default";
    formattedCodeElement.style.border = "2px solid #43A6C6";
    formattedCodeElement.style.resize = "none";
    formattedCodeElement.style.backgroundColor = "#f1f1f1";

    return formattedCodeElement;
}

function getTabId(filename) {
    return filename + "-tab";
}

function getCodeWindowId(filename) {
    return filename + "-code";
}