const express = require("express")
const app = express()
const cors = require('cors')
const formidable = require('express-formidable');


app.use(cors({
    origin: '*'
}))

app.use(formidable());

app.get('/' , (req,res)=>{
    const responseData = {
        message:"Hello",
        studentData:{
            studentId: "214",
            type: "submission"
        },
        endingMessage:"request complete"
    }
    // This will send the JSON data to the client.
    res.json(responseData);
})

app.get("/hello", cors(), function (req,res) {
    const responseData = {
        message:"Hello",
        studentData:{
            studentId: "214",
            type: "submission"
        },
        endingMessage:"request complete"
    }
    res.json(responseData);
})

app.post("/submit_file", cors(), async function(req,res){

    const files = await req.files;
    const fields = await req.fields;

    console.log(files);
    console.log(fields);

    const responseData = {
        message: "recv",
        studentData: {
            studentId: "214",
            type: "submission"
        },
        endingMessage: "request complete"
    }

    res.json(responseData);
})

app.listen(3002, () => {
    console.log("listening port 3002")
})