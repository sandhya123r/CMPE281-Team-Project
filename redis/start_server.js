var http = require('http');
var sockjs = require('sockjs');
var redis = require('redis');

// Setup Redis pub/sub.
// NOTE: You must create two Redis clients, as 
// the one that subscribes can't also publish.
var pub = redis.createClient();
var sub = redis.createClient();
sub.subscribe('global');

// Listen for messages being published to this server.
sub.on('message', function(channel, msg) {
  // Broadcast the message to all connected clients on this server.
  for (var i=0; i<clients.length; i++) {
    clients[i].write(msg);
  }
});

// Setup our SockJS server.
var clients = [];
var echo = sockjs.createServer();
echo.on('connection', function(conn) {
  // Add this client to the client list.
  clients.push(conn);

  // Listen for data coming from clients.
  conn.on('data', function(message) {
    // Publish this message to the Redis pub/sub.
    pub.publish('global', message);
  });

  // Remove the client from the list.
  conn.on('close', function() {
    clients.splice(clients.indexOf(conn), 1);
  });
});

// Begin listening.
var server = http.createServer();
echo.installHandlers(server, {prefix: '/sockjs'});
server.listen(80);


