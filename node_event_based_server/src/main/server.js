/**
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/08/2014
 */
// -----------------------------------------------------------
// REQUIRE
// -----------------------------------------------------------
var net     = require('net'),
    fs      = require('fs'),
    config  = require('./config');

// -----------------------------------------------------------
// SERVER
// -----------------------------------------------------------

var NodeServer = function() {

    // -----------------------------------------------------------
    // PROPERTIES
    // -----------------------------------------------------------

    this._server                = null;
    this._buffer_size           = Math.pow(2, 16);          // max. buffer size 65536
    this._parallelProgress      = false;                    // defines the processing strategy of the server (sequential | parallel)

    // file
    this._filePath              = __dirname;
    this._logFile               = this._filePath + '/log/logs.log';

    // server stats
    this._start_time            = null;                     // the start time of the server
    this._end_time              = null;                     // the end time of the server
    this._accepted_connections  = 0;                        // total count of accepted connections
    this._connections           = 0;                        // number of current connections on the server
};

// -----------------------------------------------------------
// METHODS
// -----------------------------------------------------------

/**
 * Starts a new server instance using the configuration options of the config.js file.
 *
 * @param parallelProgress defines the processing strategy of the server (sequential | parallel)
 * TRUE = SEQUENTIAL:   The client sends some data to the server until a delimiter will be detected to signal the
 *                      client is ended. After this happened the server sends data to the client.
 * FALSE = PARALLEL:    The server sends data to the client from the time when client data will be received on server side.
 */
NodeServer.prototype.start = function(parallelProgress){
    this._server = net.createServer();
    this._start_time = new Date();
    this._parallelProgress = parallelProgress !== 'undefined' ? parallelProgress : true;

    var ns = this;
    var buffer = new Buffer(this._buffer_size);

    this._server.on('connection', function(socket) {

        var sendFileToClient = false;

        ns._accepted_connections++;             // increase total count of accepted connections
        ns._connections++;                      // increase current connection count


        socket.on('data', function(data) {

            /* check if the processing strategy is sequential or parallel
             * SEQUENTIAL:  The client sends some data to the server until a delimiter will be detected to signal the client is ended.
             *              After this the server sends data to the client.
             * PARALLEL:    The server sends data to the client from the time when client data will be received on server side.
             */
            if(!ns._parallelProgress) {
                ns.processSequential(socket, buffer,data);
            }
            else {
                ns.processParallel(socket, buffer, data, sendFileToClient, function(dataSend) {
                    sendFileToClient = dataSend;
                });
            }
        });

        socket.on('end', function() {
            ns._connections--;                  // decrease current connection count
        });

        socket.on('error', function() {
            ns.stop();                          // stop the server if an error occurred and save the stats
        });

    }).listen(config.port, config.host);
    console.log("Server started on:" + config.host + ":" + config.port);
};

/**
 * Stops the running server from accepting new connections and keeps existing
 * connections until the last connection ended.
 */
NodeServer.prototype.stop = function() {

    var ns = this;

    if(this._server != null) {
        this._server.close(function() {
            ns._end_time = new Date();
            console.log('Deny new connections and shut down server!');
            fs.appendFile(ns._logFile, JSON.stringify(ns.collectLogData()) + "\n", function(err) {
                if(err) throw err;
            })
        });
    }
};

/**
 * The client sends some data to the server until a delimiter will be detected. This delimiter tells the server side
 * that the client has ended the transmission of the data. After this the server sends data to the client.
 *
 * The binary file for sending data to the client was downloaded from:
 * @see "http://www.galileocomputing.de/download/dateien/3023/galileocomputing_node.js.pdf"
 *
 * @param socket The socket object to send data back to the client.
 * @param buffer The buffer object to buffer parts of a message.
 * @param data The data send by the client.
 */
NodeServer.prototype.processSequential = function(socket, buffer, data) {
    // until the 2nd gen of the node.js stream api a delimiter is used
    // see http://nodejs.org/api/stream.html node.js > v0.10.26
    data = data.toString('utf-8');
    if(data.toString().indexOf(config.delimiter) == -1) {
        buffer.write(data);             // write the first part of the message into a buffer until message ends
    } else {
        // if delimiter occurred the state of transmitting data is ended, so discard the buffer content and
        // reset the buffer and start sending data to the client
        buffer = new Buffer(this._buffer_size);
        fs.readFile(this._filePath + '/assets/galileocomputing_node.js.pdf', function(err, data) {
            if(err) throw err;
            socket.write('Content-Length: ' + data.length + '\r\n');
            socket.write('\r\n');
            socket.write(data);
        });
    }
};

/**
 * The server sends data to the client from the time when client data will be received on server side.
 *
 * @param socket The socket object to send data back to the client.
 * @param buffer The buffer object to buffer parts of a message
 * @param data The data send by the client.
 * @param sendingCallback The callback function which should be called after sending data to the client.
 */
NodeServer.prototype.processParallel = function(socket, buffer, data, sendFileToClient, sendingCallback) {
    // until the 2nd gen of the node.js stream api a delimiter is used
    // see http://nodejs.org/api/stream.html node.js > v0.10.26
    data = data.toString('utf-8');
    if(data.toString().indexOf(config.delimiter) == -1) {
        buffer.write(data);             // write the first part of the message into a buffer until message ends
    } else {
        // if delimiter occurred the state of transmitting data is ended, so discard the buffer content and
        // reset the buffer and the flag of sending data to the client
        buffer = new Buffer(this._buffer_size);
    }

    if(!sendFileToClient) {
        fs.readFile(this._filePath + '/assets/galileocomputing_node.js.pdf', function(err, data) {
            if(err) throw err;
            socket.write('Content-Length: ' + data.length + '\r\n');
            socket.write('\r\n');
            socket.write(data);
            sendingCallback(true);
        });
    }
};

/**
 * This method returns the log information of the server for persisting the data into a logfile. The method returns the
 * information as JSON string.
 *
 * @returns JSON string containing the server stats.
 */
NodeServer.prototype.collectLogData = function() {

    var startDate   = this._start_time.getDate() + "-" + (this._start_time.getMonth() + 1) + "-"
                        + this._start_time.getFullYear() + "/" + this._start_time.toTimeString().match(/\d{2}:\d{2}:\d{2}/)[0];
    var endDate     = this._end_time.getDate() + "-" + (this._end_time.getMonth() + 1) + "-"
                        + this._end_time.getFullYear() + "/" + this._end_time.toTimeString().match(/\d{2}:\d{2}:\d{2}/)[0];

    return {
        "start"                 : startDate,
        "end"                   : endDate,
        "totalConns"            : this._accepted_connections,
        "connsUntilShutdown"    : this._connections,
        "parallelProgress"      : this._parallelProgress
    };
};

new NodeServer().start(process.argv[2]);

module.exports = NodeServer;