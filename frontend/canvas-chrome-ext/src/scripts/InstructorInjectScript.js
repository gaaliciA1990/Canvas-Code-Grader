console.log("inside InstructorInjectScript.js")


initUrlChangeListener();

// Listen for on initial page load for student_id to get appended 
// and when new student is clicked in speedgrader
async function initUrlChangeListener() {
    let previousUrl = '';

    const urlObserver = new MutationObserver(async function (mutations) {
        if (window.location.href !== previousUrl) {
            previousUrl = window.location.href;
            console.log(`URL changed from ${previousUrl} to ${window.location.href}`);

            // On URL change, update UI with new student submission data
            await callGetFileSubmissionApi();
        }
    });
    const config = { subtree: true, childList: true };

    // begin listener for URL changes
    urlObserver.observe(document, config);
}


async function secondFunction() {
    //const result = await callGetFileSubmissionApi();
    return await callGetFileSubmissionApi()
}



async function callGetFileSubmissionApi() {
    let bearerToken = "Bearer 7~c5V3FHLUmCwn8II4CvwhMOqZ5HLjxwRt8mVZIspclX9hzlSx6aHg493QMtYidwXp";

    const params = (() => {
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
                params["courseId"] = urlParts[i + 1];
            }
        }

        params["assignmentId"] = queryParams["assignment_id"]
        params["studentId"] = queryParams["student_id"]
        console.log(params)

        return params;
    })();

    let endpoint = `http://localhost:8080/submission/courses/${params["courseId"]}/assignments/${params["assignmentId"]}/?studentId=${params["studentId"]}&userType=GRADER`

    await fetch(
        endpoint, {
        method: "GET",
        headers: new Headers({
            'Authorization': bearerToken
        })
    })
        .catch(console.error)
        .then((response) => response.json())
        .then((responseJson) => {
            console.log(responseJson);
            console.log(responseJson.submissionFiles);

            (() => {
                /******************  BOILERPLATE *****************/
                var displayCode = document.createElement("textarea");
                displayCode.value = responseJson.submissionFiles[0].fileContent;

                displayCode.style.border = "2px solid blue";
                displayCode.style.backgroundColor = "lightblue";
                displayCode.style.padding = "10px";
                displayCode.style.height = "300px";
                displayCode.style.margin = "auto";
                displayCode.style.display = "block";

                //document.getElementById("submissions_container").prepend(displayCode)
                document.getElementById("iframe_holder").style.display = "none";
                /******************************************************/
                var submissionFiles = responseJson.submissionFiles;
                var readOnlyContainer = document.createElement("div");
                readOnlyContainer.style.border = "2px solid blue";
                readOnlyContainer.id = "read-only-container";
                // readOnlyContainer.style.margin = "auto";
                // readOnlyContainer.style.display = "block";
                // readOnlyContainer.style.padding = "10px";
                // readOnlyContainer.style.width = "auto";
                // readOnlyContainer.style.height = "auto";

                var tabContainer = document.createElement("div");
                tabContainer.id = "tab-container";
                readOnlyContainer.appendChild(tabContainer);

                var codeContainer = document.createElement("div");
                codeContainer.id = "code-container";
                readOnlyContainer.appendChild(codeContainer);

                let previousCodeId = getCodeWindowId(submissionFiles[0].name);

                // Then create code windows
                for (var i = 0; i < submissionFiles.length; i++) {
                    let content = submissionFiles[i].fileContent;
                    let name = submissionFiles[i].name;

                    var tab = document.createElement("button");
                    tab.id = getTabId(name);
                    tab.textContent = name;
                    tabContainer.appendChild(tab);

                    var codeWindow = document.createElement("div");
                    codeWindow.id = getCodeWindowId(name);
                    var formattedCodeElement = document.createElement("textarea")
                    var readOnlyCodeView = formatCodeView(formattedCodeElement, content);
                    codeWindow.appendChild(readOnlyCodeView);

                    if (i == 0) {
                        codeWindow.style.display = "block";
                    } else {
                        codeWindow.style.display = "none";
                    }
                    codeContainer.appendChild(codeWindow);

                    const openTab = (name) => {
                        var currCodeWindow = document.getElementById(getCodeWindowId(name));
                        console.log(currCodeWindow);
                        currCodeWindow.style.display = "block";
                    }

                    tab.addEventListener("click", function () {
                        document.getElementById(previousCodeId).style.display = "none";
                        previousCodeId = getCodeWindowId(name);
                        openTab(name);
                    });

                }

                // Center readOnlyContainer
                readOnlyContainer.style.margin = "auto";
                readOnlyContainer.style.display = "block";
                readOnlyContainer.style.textAlign = "center"
                document.getElementById("submissions_container").prepend(readOnlyContainer);

                console.log("displayCode: " + displayCode);
            })();



            // document.body.prepend(displayCode);

            return responseJson;
        });
}

function formatCodeView(textAreaElement, content) {
    cleanCodeView = "";

    for (var i = 0; i < content.length; i++) {
        cleanCodeView += content[i] + "\n";
        textAreaElement.value += content[i] + "\r\n";

        // pTagElement.appendChild(document.createElement('br'));
    }

    console.log("formatted code:" + cleanCodeView);
    textAreaElement.style.height = "300px";
    textAreaElement.style.width = "700px";
    textAreaElement.style.fontFamily = "Consolas"
    textAreaElement.readOnly = true;
    textAreaElement.style.cursor = "default";

    return textAreaElement;
}

function getTabId(filename) {
    return filename + "-tab";
}

function getCodeWindowId(filename) {
    return filename + "-code";
}