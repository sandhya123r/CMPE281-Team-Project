Backend Server Section

Create customer table

```
create table customer 
(
id int(10) unsigned NOT NULL AUTO_INCREMENT,
email varchar (100) not null unique,
details varchar(1000) not null,
primary key(id)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
```

References
1. Spring JDBC: http://www.mkyong.com/spring/maven-spring-jdbc-example/


