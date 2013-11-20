===========
ESI
===========

Elements with a cache channel header are particularly interesting in a scenario where 
you use *esi:include* (`Edge Side Include`_) to create your web content. We use varnish, 
and thus rely on `how esi works in varnish`_. 

ESI example
-----------

..
   Firefox ESI plugin
   https://addons.mozilla.org/en-us/firefox/addon/esi-processor/?src=cb-dl-created

   .. raw:: html
         :file: static/esi/index.html


Example HTML file with ESI content to be found below.
When you have a working setup, this is what you will get 
served - with the contents for the different parts to be found at
the obvious places in the HTML code.

The index file:`index.html <_static/esi/index.html>`_ looks like this:

.. literalinclude:: _static/esi/index.html

Part 1 : `part1.html <_static/esi/part1.html>`_:

.. literalinclude:: _static/esi/part1.html

Part 2 : `part2.html <_static/esi/part2.html>`_:

.. literalinclude:: _static/esi/part2.html


Serving the test files
----------------------

You need something that servers your static files. This
could be apache, nginx, or something else. 

Let's assume that you, after configuration, can reach 
the example pages on:
http://localhost:81/esi/index.html


Apache configuration
^^^^^^^^^^^^^^^^^^^^

The apache configuration is similar to::

   Alias /esi "/somewhere.../build/html/_static/esi/"
   <Directory "/somewhere.../build/html/_static/esi/">
     Allow from all
   </Directory>

Nginx configuration
^^^^^^^^^^^^^^^^^^^

In the appropriate *server* block add something similar to::

    location /esi/ {
        alias somewhere.../build/html/_static/esi/;
    }

Varnish ESI setup
-----------------

`Example configuration <_static/varnish/esi.vcl>`_ in order to test ESI behaviour.
The page http://localhost/esi/esitest.html (when set up), will
tell you whether ESI is active or not.

.. literalinclude:: _static/varnish/esi.vcl

Take a look at the contents and the headers you get by
using ``GET``, ``curl`` or similar::

    GET -Sed http://localhost/esi/esitest.html
    GET http://localhost/esi/esitest.html
    curl -D- -sS -o/dev/null  http://localhost/esi/esitest.html
    curl http://localhost/esi/esitest.html

You should see an ``Age`` which makes sense, and you should see understandable
values for ``HIT`` and ``MISS``.

.. References:

.. _how esi works in varnish : https://www.varnish-cache.org/trac/wiki/ESIfeatures
.. _Edge Side Include : http://www.w3.org/TR/esi-lang
