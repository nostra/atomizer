.. -*- coding: utf-8 -*-

========
Atomizer
========

Atomizer is a back end framework for `HTTP Cache Channels`_ consisting of 4 main parts:

* **Atomizer**: A server organizing the registration and exposure of cache channel events
* **Varnish-cc**: A polling server which posts cache channel events to varnish in an ordered manner
* **Client libraries** to help setting the correct cache channel headers for the various clients
* **Varnish**: Web cache server from `Varnish Software`_. We use version 3.x.

This setup is used for the Norwegian `Amedia media group`_, which cater for around 90
local Norwegian newspapers on the web, and which has roughly 1 million unique users
per week. The population of Norway is 5 million people.


Contents
========

.. toctree::
   :maxdepth: 2

   cachechannel
   esi
   atomizer
   inhouse


Resources
---------

* HTTP Cache Channels, (expired) draft: http://ietfreport.isoc.org/idref/draft-nottingham-http-cache-channels/

* Cache-Control itself is defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9


.. References:

.. _HTTP Cache Channels : http://ietfreport.isoc.org/idref/draft-nottingham-http-cache-channels/
.. _Varnish : https://www.varnish-cache.org/
.. _Varnish Software : https://www.varnish-cache.org/
.. _Amedia media group : http://www.amedia.no/

..
    http://www.mnot.net/cache_docs/
    http://www.mnot.net/cache_channels/

.. sectnum::
  :depth: 2
