var express        =         require("express");
var bodyParser     =         require("body-parser");
var app            =         express();
var redis           =	  require("redis");
var mysql           =	  require("mysql");
var session         =	  require('express-session');
var redisStore      =	  require('connect-redis')(session);
var cookieParser    =	  require('cookie-parser');
var path            =	  require("path");
var async           =	  require("async");
var client          =   redis.createClient();
var router          =	  express.Router();
var mysql = require('mysql');
var MongoClient = require('mongodb').MongoClient;
var assert = require('assert');
var ObjectId = require('mongodb').ObjectID;
var url = 'mongodb://localhost:27017/rest_test';


//app.use(bodyParser.urlencoded({ extended: false }));
//app.use(bodyParser.json());
app.engine('html',require('ejs').renderFile);
app.set('views',path.join(__dirname));
app.use(express.static(__dirname + '/public'));


var mysql = require('mysql');
var connection = mysql.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : 'root',
  database : 'redis_demo'
});

connection.connect();


connection.end();


var pool	=	mysql.createPool({
    connectionLimit : 100,
    host     : 'localhost',
    user     : 'root',
    password : 'root',
    database : 'redis_demo',
    debug    :  false
});

app.use(session({
		secret: 'ssshhhhh',
		store: new redisStore({ host: 'localhost', port: 6379, client: client,ttl :  260}),
		saveUninitialized: false,
		resave: false
}));
app.use(cookieParser("secretSign#143_!223"));
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());


function handle_database(req,type,callback) {
	async.waterfall([
		function(callback) {
			pool.getConnection(function(err,connection){
				if(err) {
          // if there is error, stop right away.
          // This will stop the async code execution and goes to last function.
					callback(true);
				} else {
					callback(null,connection);
				}
			});
		},
		function(connection,callback) {
			var SQLquery;
			console.log("i am in call back");
			switch(type) {
				case "login" :
				SQLquery = "SELECT * from user_login WHERE user_email='"+req.body.email+"' AND `user_password`='"+req.body.password+"'";
				console.log("selecting");
				break;
        case "checkEmail" :
        SQLquery = "SELECT * from user_login WHERE user_email='"+req.body.email+"'";
        break;
				case "register" :
				SQLquery = "INSERT into user_login(user_email,user_password,user_name) VALUES ('"+req.body.email+"','"+req.body.password+"','"+req.body.name+"')";
				break;
				case "addStatus" :
				SQLquery = "INSERT into user_status(user_id,user_status) VALUES ("+req.session.key["user_id"]+",'"+req.body.status+"')";
				break;
				case "getStatus" :
				SQLquery = "SELECT * FROM user_status WHERE user_id="+req.session.key["user_id"];
				break;
				default :
				break;
			}
			callback(null,connection,SQLquery);
		},
		function(connection,SQLquery,callback) {
			connection.query(SQLquery,function(err,rows){
        connection.release();
				if(!err) {
					if(type === "login") {
						callback(rows.length === 0 ? false : rows[0]);
					} else if(type === "getStatus") {
            callback(rows.length === 0 ? false : rows);
          } else if(type === "checkEmail") {
            callback(rows.length === 0 ? false : true);
          }else {
						callback(false);
					}
				} else {
          // if there is error, stop right away.
          // This will stop the async code execution and goes to last function.
          callback(true);
        }
			});
		}
	],function(result){
    // This function gets call after every async task finished.
		if(typeof(result) === "boolean" && result === true) {
			callback(null);
		} else {
			callback(result);
		}
	});
}


app.get('/',function(req,res){
	res.sendfile("index.html");
});





app.post('/login.html',function(req,res){
	console.log("coming into post");
	handle_database(req,"login",function(response){
		if(response === null) {
			//res.json({"error" : "true","message" : "Database error occured"});
			res.redirect("/404.html");
		} else {
			if(!response) {
				//res.json({"error" : "true","message" : "Login failed ! Please register"});
				res.redirect("/404.html");
			} else {
				req.session.key = response;
				res.redirect("/shop.html");
				//res.json({"error" : false,"message" : "Login success."});
			}
		}
	});
});




app.get('/shop.html',function(req,res){
	res.render("shop.html",{email : req.session.key["user_name"]});
	console.log("email :", req.session.key["user_name"]);
	//res.sendfile("shop.html");
});



app.post('/shop.html',function(req,res){
	//console.log("post on shop.html rceived");
	console.log("chocolate name selected is ",req.body.varname);
    var image_location = '' ;
    var view1_location = '';
    var view2_location = '' ;
    var view3_location = ''
   	MongoClient.connect(url,function(err,db){ 
        if(err){
            console.log("Unable to connect to the mongoDb server",err);
        } else {
                console.log("Connection established to db \n");
                var collection = db.collection('products');
                collection.find({name :req.body.varname}).stream()
                .on('data',function(doc){
                    console.log("here");
                    image_location = doc.location;
                    view1_location = doc.view1;
                    view2_location = doc.view2;
                    view3_location = doc.view3;
                })
                .on('error',function(err){
                    console.log("error");
                })  
                .on('end', function(){
                console.log("views : ",view1_location,view2_location,view3_location);
                res.render("product-details.ejs",{image:image_location,imageview1: view1_location});
                });
                           
              }
         });
});	






app.get('/checkout.html',function(req,res){
	res.sendfile("checkout.html");
});

app.get('/login.html',function(req,res){
	res.sendfile("login.html");
	console.log(req.body);
	//res.json({"error":true, "message":"test msg"});
	
});


app.get('/contact-us.html',function(req,res){
	res.sendfile("contact-us.html");
});

app.get('/cart.html',function(req,res){
	res.sendfile("cart.html");
});

/*function handle_database(req,type){

	console.log("i have beeen called");
}

app.post('/login1.html', function(req,res){
	console.log(req.body.email);
	handle_database(req,"login",{
		
	});
	res.json({"email":req.body.email,"password":req.body.password});
	console.log("json format :"+ req.body.email);
	res.sendfile("dummy.html");
});*/

app.get('/dummy.html',function(req,res){
	//res.writeHead(404,{"Content-Type":"application/json"});
	console.log(req.body.name)
});

app.get('/product-details.html',function(req,res){
	res.render("product-details.ejs",{image:req.body.image_location});
});

app.get('/404.html',function(req,res){
	res.sendfile("404.html");
});


app.get('/logout',function(req,res){
	if(req.session.key) {
    	req.session.destroy(function(){
      	res.redirect('/');
    });
	} else {
		res.redirect('/');
	}
});

app.listen(8080,function(){
  console.log("Started on PORT 8080");
})

