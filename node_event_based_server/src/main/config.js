/**
 * This object is used to configure the behavior of the server and defines some constants like ip address,
 * port number and so on.
 *
 * Created by Dennis Grewe [dg060@hdm-stuttgart.de] 03/08/2014
 */
var Config = {
    host                : '127.0.0.1',      // the host ip address of the server
    port                : 5555,             // the port to listen for incoming connections
    delimiter           : "$::_$"           // delimiter chars to show up the end of transmitted data
};

module.exports = Config;