/**
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/14/2014
 */
// -----------------------------------------------------------
// REQUIRE
// -----------------------------------------------------------
var NodeServer  = require('../main/server.js'),
    config      = require('../main/config.js'),
    assert      = require('assert'),
    fs          = require('fs');
    net         = require('net');


// -----------------------------------------------------------
// TESTS
// -----------------------------------------------------------

suite('NodeServer', function() {

    suite("suite to test the start/stop functionality of the node server", function() {
        var server = null;

        setup(function(){
            server = new NodeServer();
            server.start(false);
        });

        test("should connect to the server on '127.0.0.1:5555'", function(done) {
            setTimeout(function(){
                var client = net.connect(config.port, function(){
                   done();
                });
            }, 20);
        });

        test("should find the logging file if the server was stoped correctly containing logging data", function(done) {
            setTimeout(function(){
                if(server != null) {
                    server.stop();
                    server = null;
                    fs.readFile('../src/main/log/logs.log', function(err, data) {
                        if(err) throw err;
                        assert.equal((0 !== data.length), true);
                        done();
                    });
                }
                else {
                    assert.fail(true, false, 'Server was never started, so you can not stop it!');
                }
            }, 20);
        });

        teardown(function() {
            if(server != null) {
                server.stop();
            }
        });
    });

    suite("suite to test the sequential protocol functionality of the node server", function() {
        var server = null;

        setup(function(){
            server = new NodeServer();
            server.start(false);
        });

        test("should not received data because the delimiter was never send'", function(done) {
            setTimeout(function(){
                var client = net.connect(config.port, function(){
                    client.on('data', function(data) {
                        assert.fail(false, true, 'The protocol should not send any data!');
                    });
                    client.write('any example data');
                    setTimeout(function() {
                        done();
                    }, 100);
                });
            }, 20);
        });

        test("should received data if the delimiter was send'", function(done) {
            var dataReceived = false;
            setTimeout(function(){
                var client = net.connect(config.port, function(){
                    client.on('data', function(data) {
                        if(!dataReceived) {
                            dataReceived = true;
                            done();
                        }
                    });
                    client.write('any example data');
                    client.write(config.delimiter);
                });
            }, 20);
        });

        teardown(function() {
            if(server != null) {
                server.stop();
            }
        });
    });

    suite("suite to test the parallel protocol functionality of the node server", function() {
        var server = null;

        setup(function(){
            server = new NodeServer();
            server.start(true);
        });

        test("should received data immediately after sending any data'", function(done) {
            var dataReceived = false;
            setTimeout(function(){
                var client = net.connect(config.port, function(){
                    client.write('any example data');
                    client.on('data', function(data) {
                        if(!dataReceived) {
                            dataReceived = true;
                            done();
                        }
                    });
                });
            }, 20);
        });

        teardown(function() {
            if(server != null) {
                server.stop();
            }
        });
    });
});