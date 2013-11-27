backend default {
  .host = "127.0.0.1";
  .port = "80";
}

sub vcl_fetch {
    set beresp.do_esi = true; # Use ESI
    set beresp.ttl = 10s;     # Cache content for 10s
}

# Just to serve static content
backend nginx {
  .host = "localhost";
  .port = "81";
}

sub vcl_recv {
    set req.backend = nginx;
    #  return(pass); # Pass will return all content uncached
}

# Set a header to track a cache HIT/MISS.
sub vcl_deliver {
  if (obj.hits > 0) {
    set resp.http.X-Varnish-Cache = "HIT";
  }
  else {
    set resp.http.X-Varnish-Cache = "MISS";
  }
}
