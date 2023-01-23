console.log("inside InstructorInjectScript.js")

initUrlChangeListener();

// Listen for on initial page load for student_id to get appended 
// and when new student is clicked in speedgrader
function initUrlChangeListener() {
    let previousUrl = '';

    const urlObserver = new MutationObserver(function (mutations) {
        if (window.location.href !== previousUrl) {
            previousUrl = window.location.href;
            console.log(`URL changed from ${previousUrl} to ${window.location.href}`);

            // On URL change, update UI with new student submission data
            callGetFileSubmissionApi();
        }
    });
    const config = { subtree: true, childList: true };

    // begin listener for URL changes
    urlObserver.observe(document, config);
}

function callGetFileSubmissionApi() {
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

    fetch(
        endpoint, {
        method: "GET",
        headers: new Headers({
            'Authorization': bearerToken
        })
    }).catch(console.error).then(async response => {
        //console.log(response);
        const serv_response = await response.text();
        //console.log(serv_response)
        console.log(serv_response);

        chrome.runtime.sendMessage({ "message": serv_response });

        alert("sent message to background");

    });
}

