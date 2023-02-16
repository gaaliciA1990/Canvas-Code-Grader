require('dotenv').config()

const NODE_PORT = parseInt(process.env.NODE_PORT);
const EXPRESS_PORT = parseInt(process.env.EXPRESS_PORT);
const SSH_PORT = parseInt(process.env.SSH_PORT)

// Node and socket depedencies
const fs = require('fs');
const path = require('path');
const webServer = require('http').createServer(onRequest);
const io = require('socket.io')(webServer);
const SSHClient = require('ssh2').Client;

// Express dependencies
const express = require('express');
const cors = require('cors');

// Load xterm dependencies
let staticFiles = {};
let basePath = path.join(require.resolve('xterm'), '..');
staticFiles['/xterm.css'] = fs.readFileSync(path.join(basePath, '../css/xterm.css'));
staticFiles['/xterm.js'] = fs.readFileSync(path.join(basePath, 'xterm.js'));
basePath = path.join(require.resolve('xterm-addon-fit'), '..');
staticFiles['/xterm-addon-fit.js'] = fs.readFileSync(path.join(basePath, 'xterm-addon-fit.js'));
staticFiles['/'] = fs.readFileSync('index.html');

// Handle serving of static dependencies
function onRequest(req, res) {
    let file;
    if (req.method === 'GET' && (file = staticFiles[req.url])) {
        res.writeHead(200, {
            'Content-Type': 'text/' +
                (/css$/.test(req.url) ? 'css' :
                    (/js$/.test(req.url) ? 'javascript' : 'html'))
        });
        return res.end(file);
    }
    res.writeHead(404);
    res.end();
}

// Listen for connections
io.on('connection', function (socket) {
    let clientConnection = new SSHClient();

    // Create unique express instance for this connection
    const expressApp = express();
    const bodyParser = require('body-parser');
    expressApp.use(bodyParser.json());
    expressApp.use(bodyParser.urlencoded({ extended: false }));
    expressApp.use(cors({
        origin: '*'
    }));
    let expressServer = expressApp.listen(EXPRESS_PORT, () => {
        console.log(`Express Server running at: http://localhost:${EXPRESS_PORT}/`);
    });

    clientConnection.on('ready', function () {
        socket.emit('data', '\r\n*** SSH Connection Established to SU Server***\r\n');

        clientConnection.shell(function (err, stream) {
            if (err) {
                return socket.emit('data', '\r\n*** SSH SHELL ERROR: ' + err.message + ' ***\r\n');
            }

            // Get data from client
            socket.on('data', function (data) {
                console.log('socket.on data: ', data)
                stream.write(data);
                console.log('did stream.write\n')
            });

            stream.on('data', function (d) {
                console.log('stream.on data: ', d.toString('binary'))
                // Send data back to client
                socket.emit('data', d.toString('binary'));
                console.log('did socket.emit\n')
            }).on('close', function () {
                clientConnection.end();
            });

            // Route to change directory
            expressApp.post('/dir', (req, res) => {
                console.log(req.body)
                let cmd = `cd spring-boot-server/${req.body.dir}\n`;
                stream.write(cmd)
                res.send({ changedDirectorySuccess: true })
            });

            // Route to logout of SSH session
            expressApp.post('/logout', (req, res) => {
                console.log('logout API was invoked for socket: ', socket.id)
                clientConnection.end();
                res.send({ logoutSucces: true })
                expressServer.close((err) => {
                    if (err) {
                        console.log(err);
                    }
                    console.log('closed express server instance')
                })
            });

        });
    }).on('close', function () {
        socket.emit('data', '\r\n*** SSH Connection to SU Server is Closed ***\r\n');
    }).on('error', function (err) {
        socket.emit('data', '\r\n*** SSH CONNECTION ERROR: ' + err.message + ' ***\r\n');
    }).connect({
        // FIXME: better way to do this?
        host: process.env.SU_HOST,
        port: SSH_PORT,
        username: process.env.SU_USER,
        password: process.env.SU_PW
    });
});

webServer.listen(NODE_PORT, () => {
    console.log('SSH Server Listening on port', NODE_PORT)
});