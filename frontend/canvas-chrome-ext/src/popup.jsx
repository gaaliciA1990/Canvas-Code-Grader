import React from "react";
import {render} from "react-dom";


/**
 * The extension popup window. This enables us to display infomraiton
 * about the extension and do the oAuth Login
 * @returns {JSX.Element}
 * @constructor
 */
function Popup() {
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