
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
    btn_code_submission.value = "Evaluate";
    btn_code_submission.id = "submit_compile_code";
    btn_code_submission.type = "button";
    btn_code_submission.style.textAlign = "center";
    btn_code_submission.style.padding = "10px 32px";
    btn_code_submission.style.backgroundColor = "#0066cc";
    btn_code_submission.style.color = "white";
    btn_code_submission.style.border = "0";
    btn_code_submission.style.fontSize = "18px";
    btn_code_submission.style.borderRadius = "4px";
    btn_code_submission.style.color = "pointer";
    btn_code_submission.style.backgroundPosition = "center left 5px, center left 5px"
    btn_code_submission.style.boxShadow = "0 2px 5px rgba(0,0,0,0.2)";


    //btn.onclick = fileInput.click();

    btn_code_submission.addEventListener("click", function () {
        fileInput.click();

        //need to wait for file to be input
        //http://csrh51.cslab.seattleu.edu:8080
        //"http://127.0.0.1:8080/evaluate"
        //"http://127.0.0.1:8080/evaluate";
        const endpoint ="http://127.0.0.1:8080/evaluate"; //"http://csrh51.cslab.seattleu.edu:8080/evaluate";
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

                if(fileSubmit_response.output !== undefined){
                    let output = fileSubmit_response.output;

                    console.log("Sending message to content script from injection")
                    window.postMessage({ type: "assignment_evaluate", output });
                }
                else if (fileSubmit_response.code === 424){
                    console.log("Sending error message to content from injection")
                    window.postMessage({ type: "evaluate_error", fileSubmit_response });
                }

            });

            fileInput.removeEventListener("change", requestFunction);
        });

    })

    //making assumption all assignment pages have an "ag-list"

    //if url ends with assignments then insert at the bottom of "ag-list"
    //otherwise insert within the assignment page


    //create a div


    const el = document.getElementById("content-wrapper");
    console.log(el);

    el.appendChild(fileInput);
    console.log("appending to ag-list");
    el.appendChild(btn_code_submission);

})();