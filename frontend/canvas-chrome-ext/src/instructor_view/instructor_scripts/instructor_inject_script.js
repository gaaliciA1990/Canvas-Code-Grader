
(function () {


    var fileInput = document.createElement("input");
    fileInput.id = "fileInput";
    fileInput.type = "file";
    fileInput.hidden = false;
    fileInput.setAttribute("multiple", "");


    btn_code_submission.addEventListener("click", function () {
        fileInput.click();

        //need to wait for file to be input

        // TODO add wildcard for this speedgrader page
        const endpoint = "https://canvas.instructure.com/courses/5660191/gradebook/speed_grader?assignment_id=33719910&student_id=36497701";
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
        let bearerToken = "Bearer 7~cQ7XoNd23PrQhB5XBAp8v9osuQsPnyQVsDsZcHb7oTjgnoWYh2lU5qg5RMRMN8rr    ";

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

    //find where to add upload button
    //eesy eesy-tab2-container
    //const el = document.querySelector(".FPdoLc.lJ9FBc center");
    const el = document.body;
    console.log(el);
    el.appendChild(fileInput);
    el.appendChild(btn_code_submission);

    el.appendChild(btn_response);

})();