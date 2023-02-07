import React from "react";
import {render} from "react-dom";


/**
 * The extension popup window. This enables us to display infomraiton
 * about the extension and do the oAuth Login
 * @returns {JSX.Element}
 * @constructor
 */
function Popup() {
    window.addEventListener('DOMContentLoaded', () => {
        let background = chrome.extension.getBackgroundPage();

        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            let currentTabId = tabs[0].id;
            let results = background.results[currentTabId];

            background.console.log(results);
        })


        document.getElementById("oAuth-signIn").addEventListener('click', canvasOauthRedirect);

        //oAuth redirect link should go here
        function canvasOauthRedirect() {
            chrome.tabs.create({ active: true, url: "https://www.google.com/" });
        }


    })


    chrome.runtime.onMessage.addListener(function (request, sender, sendResponse) {

        if (request.msg === "done") {
            console.log("got msg from background page change text")
            document.getElementsByClassName("update-text").innerHTML = "Got the message";

            sendResponse("background recv msg");
        }
    })

    return (
        <div className="popup">
            <div className="popup-header">
                <h2> Welcome to SU Canvas Code Grader</h2>
                <div className="popup-container">
                    <button id='oAuth-signIn'>Log in with Canvas</button>
                </div>

                <div className="popup-containerr">
                    <h3> Students!</h3>
                    <p>
                        You can validate your code assignment compiles
                        before submitting it for grading.
                    </p>
                    <h3> Graders!</h3>
                    <p>
                        You can run student's code in SpeedGrader
                        next to the rubric for easier grading!<br/>
                    </p>
                </div>
            </div>
        </div>
    );
}

render(<Popup />, document.getElementById("react-target"));