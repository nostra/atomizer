#!/bin/sh
# First build everything
make clean html
# Automatically build on changes in source directory on Linux
while inotifywait source/* ; do make html ; done
