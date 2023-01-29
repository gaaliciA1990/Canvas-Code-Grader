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

            if (studentHasSubmission()) {
                // On URL change, update UI with new student submission data
                await updateStudentSubmissionView();
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
    /******************  BOILERPLATE *****************/
    var displayCode = document.createElement("textarea");
    displayCode.value = submissionFiles[0].fileContent;
    displayCode.style.border = "2px solid blue";
    displayCode.style.backgroundColor = "lightblue";
    displayCode.style.padding = "10px";
    displayCode.style.height = "300px";
    displayCode.style.margin = "auto";
    displayCode.style.display = "block";
    /******************************************************/

    document.getElementById("iframe_holder").style.display = "none";

    let readOnlyContainer = initReadOnlyContainer();
    let tabContainer = initTabContainer();
    let codeContainer = initCodeContainer();

    readOnlyContainer.appendChild(tabContainer);
    readOnlyContainer.appendChild(codeContainer);

    let previousCodeWindowId = getCodeWindowId(submissionFiles[0].name);

    for (var i = 0; i < submissionFiles.length; i++) {
        let content = submissionFiles[i].fileContent;
        let name = submissionFiles[i].name;

        let tab = initTabElement(name);
        tabContainer.appendChild(tab);

        var codeWindow;
        if (i == 0) {
            codeWindow = initCodeWindow(name, false);
        } else {
            codeWindow = initCodeWindow(name, true);
        }

        let formattedCodeElement = formatCodeView(content);
        codeWindow.appendChild(formattedCodeElement);
        codeContainer.appendChild(codeWindow);

        tab.addEventListener("click", function () {
            document.getElementById(previousCodeWindowId).style.display = "none";
            let currentCodeWindowId = getCodeWindowId(name);
            previousCodeWindowId = currentCodeWindowId;
            document.getElementById(currentCodeWindowId).style.display = "block";
        });

    }

    // Center readOnlyContainer
    readOnlyContainer.style.margin = "auto";
    readOnlyContainer.style.display = "block";
    readOnlyContainer.style.textAlign = "center"
    document.getElementById("submissions_container").prepend(readOnlyContainer);

    console.log("displayCode: " + displayCode);
}

function generateTerminalView(submissionDirectory) {
    // TODO: terminal view function
}

function initReadOnlyContainer() {
    let readOnlyContainer = document.createElement("div");
    readOnlyContainer.style.border = "2px solid blue";
    readOnlyContainer.id = "read-only-container";
    // readOnlyContainer.style.margin = "auto";
    // readOnlyContainer.style.display = "block";
    // readOnlyContainer.style.padding = "10px";
    // readOnlyContainer.style.width = "auto";
    // readOnlyContainer.style.height = "auto";
    return readOnlyContainer;
}

function initTabContainer() {
    let tabContainer = document.createElement("div");
    tabContainer.id = "tab-container";
    return tabContainer;
}

function initCodeContainer() {
    let codeContainer = document.createElement("div");
    codeContainer.id = "code-container";
    return codeContainer;
}

function initTabElement(fileName) {
    let tab = document.createElement("button");
    tab.id = getTabId(fileName);
    tab.textContent = fileName;
    return tab;
}

function initCodeWindow(fileName, isHidden) {
    let codeWindow = document.createElement("div");
    codeWindow.id = getCodeWindowId(fileName);
    codeWindow.style.display = isHidden ? "none" : "block";
    return codeWindow;
}

function formatCodeView(content) {
    let formattedCodeElement = document.createElement("textarea")

    for (var i = 0; i < content.length; i++) {
        formattedCodeElement.value += content[i] + "\r\n";
    }

    console.log("formatted code:" + formattedCodeElement.value);

    formattedCodeElement.style.height = "300px";
    formattedCodeElement.style.width = "700px";
    formattedCodeElement.style.fontFamily = "Consolas"
    formattedCodeElement.readOnly = true;
    formattedCodeElement.style.cursor = "default";

    return formattedCodeElement;
}

function getTabId(filename) {
    return filename + "-tab";
}

function getCodeWindowId(filename) {
    return filename + "-code";
}