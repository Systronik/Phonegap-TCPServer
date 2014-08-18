cordova.define("org.systronik.tcpserver.TCPServer", function(require, exports, module) { var tcpserver = {
    startServer: function(dataToSend, port, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TCPServer", "startServer", [{
        	"dataToSend": dataToSend,
        	"port": port
        }]);
    }
}
module.exports = tcpserver;


});
