// Dependencies
var restful = require('node-restful');
var mongoose = restful.mongoose;

// Schema
var productSchema = new mongoose.Schema({
    name: String,
    productid : String,
    location : String,
    view1 :String,
    view2: String,
    view3 : String,
    price: Number
});

// Return model
module.exports = restful.model('Products', productSchema);
