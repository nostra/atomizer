# Atomizer service

Framework for wrapping mongo queries:
http://mongojack.org/

Resources:

* http://tools.ietf.org/html/rfc5023
* https://cwiki.apache.org/confluence/display/ABDERA/Creating+and+Consuming+Atom+Documents
* http://ietfreport.isoc.org/idref/draft-nottingham-http-cache-channels/
* REST cheat sheet: http://blogs.plexibus.com/2009/01/15/rest-esting-with-curl/

# Commands

    GET http://localhost:9006/atomizer/event/current  | xmllint --pretty 1 -
    curl -i -H "Content-Type: application/json" -X POST -d '{"path":"/whee"}' http://localhost:9006/atomizer/stalegroup
    curl -i -H "Accept: application/json" -X GET http://localhost:9006/atomizer/stalegroup/51e3f480e4b05a72bc852db4

    GET http://localhost:9006/atomizer/counter/read/jawr
    curl -i -H "Content-Type: application/json" -X POST http://localhost:9006/atomizer/counter/increment/jawr