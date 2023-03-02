/**
 * script that is being used in compilation-notification.html
 */

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {

    if (msg.action === 'compilation_results') {
        console.log('msg: ', msg);
        sendResponse("confirmed");
        const output = msg.data.output;

        if(msg.data.success) {
            document.getElementById("comp-status").textContent = "Success!";
            document.getElementById("res_image").src="img/tick.png";
            document.getElementById('update-text').textContent = output[0];
        }else{
            document.getElementById("comp-status").textContent = "Program Failed to Compile";
            document.getElementById("res_image").src="img/redx.png";
            let compResultsTextArea = document.createElement('textarea');
            compResultsTextArea.id = "comp-results";
            compResultsTextArea.className = "comp-results-textarea";
            for (let i = 0; i < output.length; i++) {
                compResultsTextArea.value += `${output[i]} \r\n`
            }
            compResultsTextArea.readOnly = true;
            compResultsTextArea.style.fontFamily = "Consolas";
            compResultsTextArea.style.boxSizing = "border-box";
            compResultsTextArea.style.border = "2px solid #1E2021";
            compResultsTextArea.style.width = "100%";
            compResultsTextArea.style.height = "300px";
            document.getElementById("comp-parent-container").appendChild(compResultsTextArea)
        }
    }
    else if(msg.action === '424error'){
        console.log('msg.data 424 error: ', msg.data);
        sendResponse("error received from backend");

        document.getElementById('update-text').textContent = msg.data.message;

        document.getElementById("comp-status").textContent = "No Evaluation Script Found";
        document.getElementById("res_image").src="img/redx.png";
    }
    //handle specific errors
});