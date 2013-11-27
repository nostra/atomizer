===================
Why Cache-channels?
===================

We want to add *cache channel-maxage* in a way that is non-intrusive to regular caching
mechanisms. This information will work in concert with the webcache server `Varnish`_,
and **expire objects**. We want something similar to the following in the response object:

::

    Cache-Control: max-age=30, channel="http://event.api.no/atomizer/event/current", 
        channel-maxage=86400, group="/pub41", group="/sec71", group="/art6596253" 

The line says the following:

**max-age**
  The page shall be cached 30 seconds in the browser. This will also be used in caches 
  that do not listen to events.

**channel**
  Where to listen for updates on the object.

**channel-max-age**
  How long shall the object be cached in varnish? In the example 86400 seconds, i.e. 24
  hours.

**group** 
  One or more groups. This is an identifier of the element in question. If it is desired that
  this element shall be purged, this is what the channel will publish.


We want to support all regular usages of `Cache-Control`_, with the addition of cache channel
elements. However, focus will initially be on the *Cache-Control* fields we use ourselves.

Depending on your setup, Varnish rewrites the *Cache-Control* statement, and the output 
sent to the browser becomes::

    Cache-Control: max-age=30


In order to achieve the control we want for the *Cache-Control* 
elements, we rely on in-house libraries.

Calculating which value to retain for *max-age* and *channel-maxage*
====================================================================

The strategy is as follows:

* Use proprietary functionality to cache header contents. The body
  contents is **not** cached, so you need to
  have an architecture that is MVC based - you cannot set any headers
  after the body has been supplied.

* When reading external resources, (relevant) header elements are propagated
  to the response. Relevant headers would be X-Trace-App, and cache-related
  headers.

* The header elements are collected in a manner which makes the system aware
  of which HTTP call they belong to. In other words: You are not setting
  headers based on two different resources at the same time, you are
  setting them sequentially.

* If neither max-age nor channel-maxage is set, use defaults. If max-age is
  set, use that value for channel-maxage. If channel-maxage is set, but not
  max-age, use default for max-age.

* It would not be natural to have a max-age which is larger than channel-maxage.
  If this happens, the max-age is adjusted down to the same level as channel-maxage

cache *response* directives
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Different browsers may interpret *max-age* in different ways. This is a 
not a concern in this setting.

Note that if you get a *Cache-Control* header without *channel-maxage*, but
with *max-age*, then *channel-maxage=max-age* is set.

The other *Cache-Control* related response headers are just passed as is. 

..
    headers passed as is

    public           
    private          
    no-cache         
    no-store
    no-transform
    must-revalidate
    proxy-revalidate
    s-maxage
    cache-extension        


Long cache control directive line
---------------------------------

A web page, particularly front pages, may easily contain in excess of 100 *Cache-Control*
groups. This is, of course, a problem for the webapp server, which may have limitations
on how large a response header can be. For jetty, it is 4KB unless it has been 
reconfigured. There are advantages of being able to use a stock setup, so we want
to be able to **limit** the length of the *Cache-Control* header. 2KB may be a sensible 
limitation, so there is room for other headers.

We limit the length of the *Cache-Control* directive in the following ways:

* Duplicate elements are merged. 

* If the *Cache-Control* directive is longer then the set limitation:

  - the last arriving elements are dropped, and 
  - the statement *group="/too_long"* is added to the directive. If the length is still too
    long, drop more of the other statements.
  - The channel-maxage gets adjusted to default (for us 300 seconds, i.e. 5 minutes), unless
    not already at a lower level.


Note that a rewrite of the *Cache-Channel* header is done by re-submitting the complete
and possibly rewritten header to the webapp server, which replaces it in it's
entirety.

**TODO** What is the varnish header size limit?

**TODO** Remember poison pill

Expires as header field
-----------------------

In our setting, the header field `Expires`_ is troublesome. None of our in-house
applications are supposed to set this header. We support it like this:
*Expires* is translated into a *Cache-Channel* statement. *channel-maxage* 
is calculated from the *Expires* value. The same value is used for
*max-age*. The *Expires* field is then removed.



.. References:

.. _Cache-Control : http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9
.. _Expires : http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21
.. _Varnish : https://www.varnish-cache.org/
