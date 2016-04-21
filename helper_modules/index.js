/**
  Loading all dependcies.
**/
var express         =	  require("express");
var redis           =	  require("redis");
var mysql           =	  require("mysql");
var session         =	  require('express-session');
var redisStore      =	  require('connect-redis')(session);
var bodyParser      =	  require('body-parser');
var cookieParser    =	  require('cookie-parser');
var path            =	  require("path");
var async           =	  require("async");
var client          =   redis.createClient();
var app             =	  express();
var router          =	  express.Router();
var Client = require('node-rest-client').Client;
var http = require('http');
var fullString = '';


// Always use MySQL pooling.
// Helpful for multiple connections.

var pool	=	mysql.createPool({
    connectionLimit : 100,
    //host     : 'http://ec2-50-16-165-69.compute-1.amazonaws.com',
    host     : 'localhost',
    user     : 'root',
    password : 'root',
    database : 'redis_demo',
    debug    :  false
});

/*
var connection = mysql.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : 'root',
  database : 'redis_demo'
});

connection.connect();

connection.query('SELECT * FROM user_login', 
    function(err, rows, fields) {
        if (err) throw err;
        console.log(rows[0]);
    }
);
*/

app.set('views', path.join(__dirname,'../','views'));
app.engine('html', require('ejs').renderFile);
console.log("Hello World!!!");
/*
console.log("app  *********************************************************************************************");
console.log(app);
console.log("pool *********************************************************************************************");
console.log(pool);
*/
// IMPORTANT
// Here we tell Express to use Redis as session store.
// We pass Redis credentials and port information.
// And express does the rest !

app.use(session({
		secret: 'ssshhhhh',
		store: new redisStore({ host: 'localhost', port: 6379, client: client,ttl :  260}),
		saveUninitialized: false,
		resave: false
}));
app.use(cookieParser("secretSign#143_!223"));
app.use(bodyParser.urlencoded({extended: true}));
app.use(bodyParser.json());
var endpoint = "http://ec2-50-16-165-69.compute-1.amazonaws.com:8080/customers/";



// This is an important function.
// This function does the database handling task.
// We also use async here for control flow.

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
			console.log("I am in callback for connection");
			switch(type) {
				case "login" :
				SQLquery = "SELECT * from user_login WHERE user_email='"+req.body.user_email+"' AND `user_password`='"+req.body.user_password+"'";
				console.log("selecting ");
				break;
                case "checkEmail" :
                SQLquery = "SELECT * from user_login WHERE user_email='"+req.body.user_email+"'";
                break;
				case "register" :
				SQLquery = "INSERT into user_login(user_email,user_password,user_name) VALUES ('"+req.body.user_email+"','"+req.body.user_password+"','"+req.body.user_name+"')";
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

/**
    --- Router Code begins here.
**/

router.get('/',function(req,res){
	res.render('index.html');
});

router.post('/login',function(req,res){
	handle_database(req,"login",function(response){
		if(response === null) {
			res.json({"error" : "true","message" : "Database error occured"});
		} else {
			if(!response) {
				res.json({"error" : "true","message" : "Login failed ! Please register"});
			} else {
				console.log("Appending URL");	
				 var email ="sandhya@gmail"
  				 var  url = endpoint+email
  				 
				
				http.get(url, function callback(response) {

    				 response.on('data', function(data) {
       					 fullString += data.toString();
    					})
    				response.on('end', function() {
        			console.log("FULLSTRING : ",fullString);
        			newString = JSON.parse(fullString);
     				req.session.key = newString;  
    					})
 				})	
				
				res.json({"error" : false,"message" : "Login success."});
			}
		}
	console.log("handled request ");
	});
});

/*
var url = 'ec2-50-16-165-69.compute-1.amazonaws.com:8080/customers/sandhya@gmail.com'
var request = require("request")

request(url, function(error, response, body) {
  console.log(body);
});

*/
router.get('/home',function(req,res){
    //console.log(req.session)
	if(req.session.key) {
		console.log("Printing in home");
		console.log("KEY : ",req.session.key);
                console.log("EMAIL : ",req.session.key.email);	
		console.log("CART: ",req.session.key.cart);	
		res.render("home.html",{ email : req.session.key.email});
	} else {
		console.log("In redirect");
		res.redirect("/");
	}
});

router.get("/fetchStatus",function(req,res){
  if(req.session.key) {
    handle_database(req,"getStatus",function(response){
      if(!response) {
        res.json({"error" : false, "message" : "There is no status to show."});
      } else {
        res.json({"error" : false, "message" : response});
      }
    });
  } else {
    res.json({"error" : true, "message" : "Please login first."});
  }
});

router.post("/addStatus",function(req,res){
    if(req.session.key) {
      handle_database(req,"addStatus",function(response){
        if(!response) {
          res.json({"error" : false, "message" : "Status is added."});
        } else {
          res.json({"error" : false, "message" : "Error while adding Status"});
        }
      });
    } else {
      res.json({"error" : true, "message" : "Please login first."});
    }
});

router.post("/register",function(req,res){
    console.log("in register!!");
    handle_database(req,"checkEmail",function(response){
      if(response === null) {
        res.json({"error" : true, "message" : "This email is already present"});
      } else {
        handle_database(req,"register",function(response){
          if(response === null) {
            res.json({"error" : true , "message" : "Error while adding user."});
          } else {
            res.json({"error" : false, "message" : "Registered successfully."});
          }
        });
      }
    });
});

router.get('/logout',function(req,res){
	if(req.session.key) {
    req.session.destroy(function(){
      res.redirect('/');
    });
	} else {
		res.redirect('/');
	}
});

app.use('/',router);

app.listen(8080,function(){
	console.log("I am running at 8080");
});
