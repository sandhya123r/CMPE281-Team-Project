var endpoint = "<insert endpoint here>";

var fs = require('fs');
var express = require('express');
var Client = require('node-rest-client').Client;
var bodyParser  = require('body-parser');
var http = require('http');
var app = express();
app.use(bodyParser.urlencoded({
  extended: true
}));

var page = function( req, res ) {

     http.get(endpoint, function (response) {
        response.setEncoding('utf8')
        response.on('data', console.log)
        response.on('error', console.error)
    })
}
var handle_get = function (req, res) {
    console.log( "Get: ..." ) ;
    page( req, res ) ;
}

app.get( "*", handle_get ) ;

app.listen('8080', function() {
  console.log('Node app is running on port', 8080);
});

