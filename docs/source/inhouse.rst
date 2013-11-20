==============================================
In house libraries: Details and considerations
==============================================

**TO BE REVISED**

Client side
^^^^^^^^^^^

* setup
* HTTP response header examination
* values inserted / strategy
* header size


Atomizer feed client
^^^^^^^^^^^^^^^^^^^^


Future directions
^^^^^^^^^^^^^^^^^
* Add support for indicating which channel groups that should be treated in
  a particular manner. (Now hard coded as */sec* and */art*)
* Phase in support for new atomizer version in production
* Implement varnish-cc

Calculating which value to retain for *max-age* and *channel-maxage*
====================================================================

When combining elements from several sources, of there may be **more than
one** *max-age* or *channel-maxage* element, there are some considerations
to be made.

Let's say you create an object based on 2 sources. The lifespan for that
object should be the same as the shortest lifespan of the sources. The
browser cache period should be the shortest of the 2 sources.

The rule for *max-age* is as follows: The lowest value for max-age wins.

The rule for *channel-maxage* is a bit more complex. Remember, this may be set
due to reading several already cached sources. Each of these may have an *Age*
element. *channel-maxage - Age* cannot be negative.

If you have *(channel-maxage, Age)* pairs, *(9999, 9000)* has a lower lifetime
than *(2000, 500)*.

* We want to keep the header element which has the lowest value
  for *channel-maxage - Age*.

* If we have not got *Age* (yet), we assume it is zero.

* Special behavior with two pairs of *(channel-maxage, Age)*. The permutations is
  as follows (c = *channel-maxage*, a = *Age*):

  - ccaa : Last *Age* element is discarded
  - caca : Normal case (i.e. we got a pair out of which to calculate a value)
  - caac : Normal
  - acca : Normal
  - acac : Normal
  - aacc : First age is discarded, last maxage is assumed to be associated
    with *Age=0*

* If we encounter one *channel-maxage*, but several *Age* elements, only the first
  *Age* element is taken into consideration. This is as we expect the normal case to be
  one *channel-maxage* per *Age*.

* Age must never be greater than *channel-maxage*. If it is, it should be set to the same as
  *channel-maxage*.
