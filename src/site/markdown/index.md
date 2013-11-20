Atomizer
========

Atomizer is an implementation of cache channel functionality.

**NB outdated link **

Interesting links:

 * http://www.mnot.net/cache_docs/

 * http://www.mnot.net/cache_channels/

 * http://ietfreport.isoc.org/idref/draft-nottingham-http-cache-channels/

 * HTTP header fields
 http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html


# Database setup

Atomizer would like a separate database, with the username
atomizer and the expected password.

You need to have this configuration under the \[mysqld\] section of
___/etc/mysql/my.cnf___ :

    binlog_format           = 'MIXED'
    lower_case_table_names=1


## Creating the mysql database

    mysql --user=root --password=pingu123
    create database atomizer;
    # Add user
    grant all privileges on atomizer.* to atomizer identified by 'pingu123';
    grant all privileges on atomizer.* to atomizer@'localhost' identified by 'pingu123' ;
    FLUSH PRIVILEGES;

# JNDI source

The jndi source is configured in web.xml (and referred to in jdbc.properties,
read by AtomizerJdbc), and is named:

    jdbc/DSAtomizer

