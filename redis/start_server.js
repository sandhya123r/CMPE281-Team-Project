var sentinel = require('redis-sentinel');
var endpoints = [
    {host: '127.0.0.1', port: 26379},
    {host: '127.0.0.1', port: 26380},
    {host: '127.0.0.1', port: 26381}
];
var opts = {}; // Standard node_redis client options
var masterName = 'mymaster';
var redisClient = sentinel.createClient(endpoints, masterName, opts);


//Connecting to master
var masterClient = sentinel.createClient(endpoints, masterName, {role: 'master'}); 

//Connecting to slaves
var slaveClient = sentinel.createClient(endpoints, masterName, {role: 'slave'});

//Connecting to sentinel
var sentinelClient = sentinel.createClient(endpoints, {role: 'sentinel'});


