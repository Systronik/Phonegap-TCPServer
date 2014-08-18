var tcpserver = {
    startServer: function(title, location, notes, startDate, endDate, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TCPServer", "startServer", []);
    }
}
module.exports = tcpserver;

