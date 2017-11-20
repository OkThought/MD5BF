# MD5 Brute-Force

## Description

Task 5 of the course "Networking Technologies" at NSU. Task is to distributed brute-force md5 hash.

## Build

`$ ant build.xml` - builds `server.jar` and `client.jar` in `out` dir.

## Usage

### Server

A server to send brute-force tasks to client programs that will brute-force in parallel.

`java -jar server.jar hash port`

* `hash` - md5 hash of the string to hack
* `port` - port on which to listen to tcp connections from clients.

### Client

A client MD5BF program to brute-force in parallel tasks received from server.

`java -jar client.jar address port`

* `address` - ipv4 or ipv6 address or domain name of the server.
* `port` - port of the server.
