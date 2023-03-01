/**
 * script that is being used in compilation-notification.html
 */

chrome.runtime.onMessage.addListener(function (msg, sender, sendResponse) {

    if (msg.action === 'compilation_results') {
        console.log(msg.data);
        sendResponse("confirmed");

        document.getElementById('update-text').textContent  =
            JSON.stringify(msg.data, null, '  ');

        if(msg.data !== "Your program compiled successfully!") {
            document.getElementById("comp-status").textContent = "Program Failed to Compile";
            document.getElementById("res_image").src="img/redx.png";
        }else{
            document.getElementById("comp-status").textContent = "Success!";
            document.getElementById("res_image").src="img/tick.png";
        }
    }
    else if(msg.action === '424error'){
        console.log(msg);
        sendResponse("error received from backend");

        document.getElementById('update-text').textContent  =
            JSON.stringify(msg, null, '  ');

        document.getElementById("comp-status").textContent = "ERROR 424";
        document.getElementById("res_image").src="img/redx.png";
    }
});