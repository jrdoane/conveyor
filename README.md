# Conveyor
![Conveyor belt image](http://www.wpclipart.com/working/factory/conveyor_belt.png)

## What is it?
A Clojure library to enable different entities (or routers) to communicate with
eachother by passing messages over an n-sized graph while letting two endpoints
connected to different servers to communicate with eachother between any number
of intermediate routers.

## What will it use?
Streams! Manifold will be at the very heart of this library. Nothing about
individual messages can be handled in tandem efficently, but entire messages can
be. As a result, the entire process can broken down into a pipeline which can be
processed in parallel at the expensive of some potentnial latency. The goal of
this library is to learn more about Manifold and if pipelining using streams at
the software level can have any tangible effect on scalibility and performance.

This is a proof-of-concept/brain-dump kind of library.

## Usage

As soon as it has been implemented. I'll let you know.

## License

Copyright © 2015 Jon Doane

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
