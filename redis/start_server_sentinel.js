var sentinel = require('redis-sentinel');
 
// List the sentinel endpoints 
var endpoints = [
    {host: '127.0.0.1', port: 26379},
    {host: '127.0.0.1', port: 26380}
];
 
var opts = {}; // Standard node_redis client options 
var masterName = 'mymaster';

var redisClient = sentinel.createClient(endpoints, masterName, opts);

var Sentinel = sentinel.Sentinel(endpoints);
var masterClient = Sentinel.createClient(masterName, opts);

var masterClient1 = sentinel.createClient(endpoints, masterName, {role: 'master'}); 
var slaveClient = sentinel.createClient(endpoints, masterName, {role: 'slave'});
var sentinelClient = sentinel.createClient(endpoints, {role: 'sentinel'});
