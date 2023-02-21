console.log("inside instructor-inject-script.jsx")

window.addEventListener('beforeunload', async (event) => {
    await closeSSHSession();
    return 'closing ssh session'
})

let isFirstStudent = true;
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
    })
        .catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
            console.log(responseJson.submissionFiles);

            // Check current student page matches the studentId in case
            // new student submission was clicked before API call is resolved
            if (window.location.href.includes(params["studentId"])) {
                if (!isFirstStudent) {
                    erasePreviousStudentView();
                }

                let instructorViewContainer = initInstructorViewContainer();
                generateReadOnlyCodeView(responseJson.submissionFiles, instructorViewContainer);
                generateTerminalView(responseJson.submissionDirectory, instructorViewContainer);
                isFirstStudent = false;
            }
        });
}

async function erasePreviousStudentView() {
    // This will remove everything inside the container (RO-view and terminal)
    let prevInstructorViewContainer = document.getElementById('instructor-view-container');
    prevInstructorViewContainer.remove();

    await closeSSHSession();
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

function generateReadOnlyCodeView(submissionFiles, instructorViewContainer) {
    document.getElementById("iframe_holder").style.display = "none";

    let appBar = instructorViewAppBar();
    let tabContainer = initTabContainer();
    let codeContainer = initCodeContainer();

    let isInDarkMode = false;
    let fileName = submissionFiles[0].name;
    let previousCodeWindowId = getCodeWindowId(fileName);
    let previousTabId = getTabId(fileName)


    let darkModeButton = initDarkModeButton();
    let compileButton = initCompileButton();
    let abortButton = initAbortButton();

    //Darkmode toggle
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
        isInDarkMode = !isInDarkMode; // Flip to other mode
    });

    //Compile button listener
    compileButton.addEventListener("click", async function (){
        //send post request to backend to start compilation or send makefile?
        console.log('calling compileButtonCommand from instructor view');
        await compileButtonCommand();
    });


    //Abort button listener
    abortButton.addEventListener("click", async function (){
        //send post request to backend to start compilation or send makefile?
        console.log('calling abort from instructor view');
        await abortScriptExecution();
    });


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


        appBar.appendChild(compileButton);
        appBar.appendChild(abortButton);
        appBar.appendChild(darkModeButton);


        instructorViewContainer.appendChild(appBar);
        instructorViewContainer.appendChild(tabContainer);
        instructorViewContainer.appendChild(codeContainer);


    }

    document.getElementById("submissions_container").prepend(instructorViewContainer);
}

function generateTerminalView(submissionDirectory, instructorViewContainer) {
    console.log("generate terminal view function");
    let terminalFrame = initTerminalFrame();
    instructorViewContainer.appendChild(terminalFrame);

    terminalFrame.addEventListener('load', async function () {
        console.log('waiting for everything else to load in terminal')
        setTimeout(async () => {
            console.log('waited 5 seconds');
            await changeToSubmissionDirectory(submissionDirectory)
        }, 5000);
    });
}

async function closeSSHSession() {
    await fetch('http://localhost:7000/logout', {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
        });
}

async function changeToSubmissionDirectory(submissionDirectory) {
    let port = 7000;
    await fetch(`http://localhost:${port}/dir`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            dir: submissionDirectory
        })
    })
        .catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
        });
}

/**
 * Instructor view play button was pressed and a call to compile the current
 * read only script was made.
 * @returns {Promise<void>}
 */
async function compileButtonCommand(){
    let port = 7000;
    await fetch(`http://localhost:${port}/compile`, {
        method: "POST",
    })
        .catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
        });
}

/**
 * Instructor view abort button was pressed and a call to end current
 * read only script was made.
 * @returns {Promise<void>}
 */
async function abortScriptExecution(){
    let port = 7000;
    await fetch(`http://localhost:${port}/abort`, {
        method: "POST",
    })
        .catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
        });
}


function initTerminalFrame() {
    let terminalFrame = document.createElement("iframe");
    terminalFrame.id = "terminal-frame";
    terminalFrame.src = "http://localhost:8000/"
    terminalFrame.height = "40%"
    terminalFrame.style.resize = "both"
    terminalFrame.style.position = "relative";
    return terminalFrame;
}

//APP BAR SPECIFIC FUNCTIONS
function instructorViewAppBar(){
    let instructorViewAppBar = document.createElement("div");
    instructorViewAppBar.className = "instructor-appbar";
    instructorViewAppBar.id = "instructor-view-appbar";
    instructorViewAppBar.style.width = "100%";

    return instructorViewAppBar;
}

function initAbortButton(){
    let abortButton = document.createElement("button");
    abortButton.icon = document.createElement("icon");
    abortButton.innerHTML ='&#x2716;'; //abort button icon
    abortButton.className= 'abort-button';

    return abortButton;
}

function initCompileButton(){
    let compileButton = document.createElement("button");
    compileButton.icon = document.createElement("icon");
    compileButton.innerHTML ='&#x1F528;'; //play button icon
    compileButton.className= 'abort-button';

    return compileButton;
}

function initDarkModeButton() {
    let darkModeButton = document.createElement("button");
    darkModeButton.innerHTML = '&#x1F319;'; //hex for wanning crescent
    darkModeButton.className = 'dark-mode';

    return darkModeButton;
}

function initTabContainer() {
    let tabContainer = document.createElement("div");
    tabContainer.id = "tab-container";
    tabContainer.style.border = "2px solid #43A6C6";
    tabContainer.style.backgroundColor = "#f1f1f1";
    return tabContainer;
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

function initInstructorViewContainer() {
    let instructorViewContainer = document.createElement("div");
    instructorViewContainer.id = "instructor-view-container";
    instructorViewContainer.style.margin = "20px";
    instructorViewContainer.style.height = "100%";
    instructorViewContainer.style.overflowY = "scroll";
    return instructorViewContainer;
}

function initCodeContainer() {
    let codeContainer = document.createElement("div");
    codeContainer.id = "code-container";
    codeContainer.style.height = "70%";
    return codeContainer;
}

function initCodeWindow(fileName, isDisplayed) {
    let codeWindow = document.createElement("div");
    codeWindow.id = getCodeWindowId(fileName);
    codeWindow.style.display = isDisplayed ? "inline-block" : "none";
    codeWindow.style.height = "100%";
    codeWindow.style.width = "100%";
    return codeWindow;
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
