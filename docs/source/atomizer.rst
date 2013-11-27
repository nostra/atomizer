===============
Atomizer server
===============

The atomizer server lets you submit which *Cache-Channel* groups
that shall be invalidated. The ``atomizer-cc`` server will read
from the Atom feed and invalidate the varnish servers associated
with it.

Atomizer is based on the `Dropwizard`_ framework.

Security
--------

**Note:** The atomizer application is not intended to be exposed to the
general public. If you do, you risk `DOS`_ attacks. Atomizer is expected
to live behind the firewall, being accessible from the server park inside.

Setup of atomizer
-----------------

To run atomizer as it is, you need Java and `MongoDB`_. You would
also need to configure the relevant properties in the
atomizer.yml configuration file.

In order to compile from source you need `Maven 3`_. The compilation
should be a matter of::

    mvn clean install

You would run the application jar like this from the source build
directory::

    java -jar *-service/target/a*-SNAPSHOT.jar \
              server \
             *-service/dropwizard.yml

You can also run it with maven from the -service directory::

    cd *-service
    mvn exec:java

Running inside an IDE
^^^^^^^^^^^^^^^^^^^^^

You can run the project, directly, inside an IDE. That will have
advantages such as easier debugging and reload of the code when
changed. The following instructions are for IntelliJ, but it is
to be expected that the instructions for Eclipse and other IDEs
will be analogous.

IntelliJ instructions:

* Go to the class ``AtomizerService``. Right click the class name, and
  choose *create run configuration*

* Tick off "run as single instance"

* Enter the following as program parameters: ``server atomizer-service/atomizer.yml``

* Choose to run the service from the ``Run`` menu.

You should see atomizer start in a manner which is similar to what happens on the
command line.

Functionality
-------------

The available resources will be reported at startup.


+------+---------------------------------------+----------------------------------------------------------------------+
|Method|  Path                                 | Explanation                                                          |
+======+=======================================+======================================================================+
| GET  |  /atomizer/                           | Web gui: Index page                                                  |
+------+---------------------------------------+----------------------------------------------------------------------+
| POST |  /atomizer/submit.html                | Web gui: Manual post                                                 |
+------+---------------------------------------+----------------------------------------------------------------------+
| POST |  /atomizer/search.html                | Web gui: Search for stalegroups                                      |
+------+---------------------------------------+----------------------------------------------------------------------+
| POST |  /atomizer/stalegroup                 | Post stalegroup item to be put into the database                     |
+------+---------------------------------------+----------------------------------------------------------------------+
| GET  |  /atomizer/stalegroup/{id}            | Get JSON stalegroup for spesific ID                                  |
+------+---------------------------------------+----------------------------------------------------------------------+
|DELETE|  /atomizer/stalegroup/{id}            | If you for some reason want or need to delete an entry               |
+------+---------------------------------------+----------------------------------------------------------------------+
| GET  |  /atomizer/event/archive/{timestamp}  | Find elements from the given timestamp, inclusive                    |
+------+---------------------------------------+----------------------------------------------------------------------+
| GET  |  /atomizer/event/{idOrCurrent}        | Find elements from current window, or element with given id          |
+------+---------------------------------------+----------------------------------------------------------------------+
| GET  |  /atomizer/counter/{name}             | Current value for counter with name "name". Include on path to js    |
|      |                                       | and css files in order to force them to be re-read                   |
+------+---------------------------------------+----------------------------------------------------------------------+
| PUT  | /atomizer/counter/{name}              | Increment the counter with name "name"                               |
+------+---------------------------------------+----------------------------------------------------------------------+
|DELETE| /atomizer/counter/{name}              | Delete counter with name "name"                                      |
+------+---------------------------------------+----------------------------------------------------------------------+

`Swagger`-documentation has been added and can be found at ``...:port/atomizer/api-docs/``

Examples
^^^^^^^^

Post the group */whee*::

    curl -i -H "Content-Type: application/json" -X POST -d '{"path":"/whee"}' http://localhost:9006/atomizer/stalegroup

Read groups in atom feed::

    GET http://localhost:9006/atomizer/event/current  | xmllint --pretty 1 -

Read a specific entry. (Replace XXX with some ID)::

    curl -i -H "Accept: application/json" -X GET http://localhost:9006/atomizer/stalegroup/XXX

Increment counter with name *NAME*::

    curl -i -X PUT http://localhost:9006/atomizer/counter/NAME

Read counter with name *NAME*::

    GET http://localhost:9006/atomizer/counter/NAME

Delete counter with name *NAME*::

    curl -i -X DELETE http://localhost:9006/atomizer/counter/NAME


.. References:

.. _DOS : http://en.wikipedia.org/wiki/Denial-of-service_attack
.. _Dropwizard : http://www.dropwizard.io/
.. _Maven 3 : http://maven.apache.org/
.. _MongoDB : http://www.mongodb.org/
.. _Swagger : http://swagger.wordnik.com/
