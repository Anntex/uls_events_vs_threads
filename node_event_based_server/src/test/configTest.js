/**
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/14/2014
 */
// -----------------------------------------------------------
// REQUIRE
// -----------------------------------------------------------
var config = require('../main/config.js');
var assert = require('assert');

// -----------------------------------------------------------
// TESTS
// -----------------------------------------------------------

suite("Suite to test the constants of the node config file", function() {

    test("should return the configured port 5555", function() {
        var port = 5555;
        assert.equal(port, config.port);
    });

    test("should return the host address", function() {
        var host = '127.0.0.1';
        assert.equal(host, config.host);
    });

    test("should return the correct delimiter char sequence", function() {
        var delimiter = "$::_$";
        assert.equal(delimiter, config.delimiter);
    });
});