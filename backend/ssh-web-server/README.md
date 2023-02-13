# SSH Web Server

Node web server for creating an SSH tunnel to the remote Linux server. For each new socket conneciton, a unique Express server instance is created to help with closing/managing the socket during browser events.

## Known Bugs

- Currently unable to open a speedgrader for two different assignments at the same time. This is because Express will attempt to start a new instance on the same port number. It's not a problem when switching to another student in the same speedgrader because the Express instance is shutdown when the `/logout` API is invoked. Another Express can then be opened on the same port without issue.