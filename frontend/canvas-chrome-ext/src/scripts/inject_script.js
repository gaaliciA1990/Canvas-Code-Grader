
(function () {


    var fileInput = document.createElement("input");
    fileInput.id = "fileInput";
    fileInput.type = "file";
    fileInput.hidden = false;

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

            chrome.runtime.sendMessage({"message": serv_response});

            alert("sent message to background");

        });
    })



    btn_code_submission.addEventListener("click", function () {
        fileInput.click();

        //need to wait for file to be input

        const endpoint ="http://127.0.0.1:3002/submit_file";
        const formData = new FormData();

        fileInput.addEventListener("change", function (){
            formData.append("fileInput", fileInput.files[0]);

            //default multipart form data
            fetch(endpoint, {
                method: "POST",
                body: formData
            }).catch(console.error).then(async response => {
                let fileSubmit_response = await response;

                console.log(fileSubmit_response)
                alert(fileSubmit_response);
            });
        });

    })


    //find where to add upload button
    const el = document.querySelector(".FPdoLc.lJ9FBc center");
    console.log(el);
    el.appendChild(fileInput);
    el.appendChild(btn_code_submission);

    el.appendChild(btn_response);

})();
