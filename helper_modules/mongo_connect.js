//Track details of a chocolate by name


var MongoClient = require('mongodb').MongoClient;
var assert = require('assert');
var ObjectId = require('mongodb').ObjectID;
var url = 'mongodb://localhost:27017/rest_test';

var findChocolate = function(db,chocname,callback) {
   var cursor =db.collection('products').find({"name":chocname} );
   cursor.each(function(err, doc) {
      assert.equal(err, null);
      if (doc != null) {
         console.dir(doc);
      } else {
         callback();
      }
   });
};


MongoClient.connect(url, function(err, db) {
  assert.equal(null, err);
  findChocolate(db,"kitkat", function() {
      db.close();
  });
});
