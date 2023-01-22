console.log("inside InstructorInjectScript.js")

let endpoint = "http://localhost:8080/submission/courses/5660191/assignments/33673192/?studentId=36496549&userType=GRADER"
let bearerToken = "Bearer 7~cQ7XoNd23PrQhB5XBAp8v9osuQsPnyQVsDsZcHb7oTjgnoWYh2lU5qg5RMRMN8rr";

fetch(
    endpoint, {
    method: "GET",
    headers: new Headers({
        'Authorization': bearerToken
    })
}
).catch(console.error).then(async response => {
    //console.log(response);
    const serv_response = await response.text();
    //console.log(serv_response)
    console.log(serv_response);

    chrome.runtime.sendMessage({ "message": serv_response });

    alert("sent message to background");

});

