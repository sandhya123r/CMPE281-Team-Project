var RedisSentinel = require('simple-sentinel');
var sentinels = [
  { host: '127.0.0.1', port: 26379 },
  { host: '127.0.0.1', port: 26380 },
  { host: '127.0.0.1', port: 26381 },
  { host: '127.0.0.1', port: 26382 },
  { host: '127.0.0.1', port: 26383 }
];

var options = {
//   Omit this to track all servers that the sentinel knows about:
  watchedNames: ["replica_a", "replica_b", "replica_d"]
};

// Create a sentinel object to track the given Replicas:
var sentinel = new RedisSentinel(sentinels, options);

// Keep track of the Masters here:
var masters = {
  replica_a: null,
  replica_b: null,
  replica_d: null
};



// Listen for connection info changes:
sentinel.on('change', function (name, replica) {
  console.log("Just got connection info for Replica:", name, replica.toString());
  masters[name] = replica.connectMaster();
});
