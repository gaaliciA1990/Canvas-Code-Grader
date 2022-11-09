# API Endpoints
## POST: /evaluate
Compiles the supplied code files
- **Body**: multipart/form-data
- **Parameters**:
  - files: code files to compile
  - userId: should correspond to some ID to identify the student who is requesting to have their code evaluated (grab from oauth?)
- **Response**: application/json
  - success (boolean): indicates if compilation was successful
  - output: compiler output (or success message if compilation was successful)

### Setup
This endpoint calls an external Canvas API to retrieve a makefile. Right now, some manual setup is required to make sure the API call is successful:
1. [Generate a Canvas authorization token](https://canvas.instructure.com/doc/api/file.oauth.html#manual-token-generation)
    - We should automate token generation if we can
2. Copy authorization token
3. Create a system environment variable `CANVAS_API_TOKEN`
4. Paste authorization token value as the value for the environment variable
5. Upload a [makefile](https://github.com/Canvas-Code-Capstone/mock-api/blob/master/responses/hello-world/makefile) to your Canvas account
    - This is for a simple hello world program - the supplied file parameter must be named `hello.cpp`
6. Hover over file in Canvas and copy/paste the file ID from the URL at the bottom of the screen as the parameter value for `getFileFromCanvas` function in `ChromeAPIController.java` 
    - We should automate retrieving the fileId over the makefile in the future too