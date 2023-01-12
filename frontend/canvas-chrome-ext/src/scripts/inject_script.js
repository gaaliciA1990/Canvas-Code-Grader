
(function (tag) {


    var fileInput = document.createElement("input");
    fileInput.id = "fileInput";
    fileInput.type = "file";
    fileInput.hidden = true;
    fileInput.setAttribute("multiple", "");

    fileInput.onclick = function () {
        this.value = null;
    };

    var btn_code_submission = document.createElement("input");
    btn_code_submission.value = "Submit Check";
    btn_code_submission.id = "submit_compile_code";
    btn_code_submission.type = "button";
    //btn.onclick = fileInput.click();

    var btn_response = document.createElement("input");
    btn_response.value = "Get response";
    btn_response.id = "response_bth";
    btn_response.type = "button"


    btn_response.addEventListener("click", function () {
        //fileInput.click();

        let endpoint = "http://127.0.0.1:3002/hello"
        //default multipart form data
        fetch(endpoint, {
            method: "GET",
        }).catch(console.error).then(async response => {
            //console.log(response);
            const serv_response = await response.text();
            //console.log(serv_response)
            alert(serv_response);

            chrome.runtime.sendMessage({ "message": serv_response });

            alert("sent message to background");

        });
    })



    btn_code_submission.addEventListener("click", function () {
        fileInput.click();

        //need to wait for file to be input
        //http://csrh51.cslab.seattleu.edu:8080
        //"http://127.0.0.1:8080/evaluate"
        const endpoint = "http://127.0.0.1:8080/evaluate"; //"https://csrh51.cslab.seattleu.edu:8080/evaluate";
        const formData = new FormData();

        const params = (() => {
            let canvasUrl = window.location.href;
            console.log(canvasUrl);
            let urlParts = canvasUrl.split('/');
            console.log(urlParts);

            let params = {};

            for (var i = 0; i < urlParts.length; i++) {
                if (urlParts[i] === "courses") {
                    params["courseId"] = urlParts[i + 1];
                }
                if (urlParts[i] === "assignments") {
                    params["assignmentId"] = urlParts[i + 1];
                }
            }

            return params;
        })();

        let courseId = params["courseId"];
        let assignmentId = params["assignmentId"];
        let userType = "STUDENT";
        // FIXME: extract oauth token
        let bearerToken = "Bearer 7~cQ7XoNd23PrQhB5XBAp8v9osuQsPnyQVsDsZcHb7oTjgnoWYh2lU5qg5RMRMN8rr";

        fileInput.addEventListener("change", function requestFunction() {
            for (var i = 0; i < this.files.length; i++) {
                formData.append("files", fileInput.files[i]);
            }
            formData.append("courseId", courseId);
            formData.append("assignmentId", assignmentId);
            formData.append("userType", userType);

            fetch(endpoint, {
                method: "POST",
                headers: new Headers({
                    'Authorization': bearerToken
                }),
                body: formData
            }).catch(console.error).then(async response => {
                let fileSubmit_response = await response.json();

                console.log(fileSubmit_response);
                alert(fileSubmit_response.output);

                let output = fileSubmit_response.output;

                console.log("sending message to content")
                window.postMessage({ type: "FROM_PAGE", output });

            });

            fileInput.removeEventListener("change", requestFunction);
        });

    })

    //making assumption all assignment pages have an "ag-list"
    const el = document.getElementById("ag-list");
    console.log(el);
    el.appendChild(fileInput);
    el.appendChild(btn_code_submission);

    el.appendChild(btn_response);

})();