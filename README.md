## Welcome to the event-drive vs. thread-based architecture project page!
This project deals with the study of different server architectures in regard to a large number of incoming and outgoing connections, I/O operations, as well as the transfer of data over these connections. The goal of this project is to identify the strengths, weaknesses and an ideal use of the different architecture models under the terms of load tests.

In order to enable a quality assessment to the comparison of thread-based and event-driven architecture, load tests of the architecture models were carried out. The project consists of three modules:

*	loadtester module: A load testing framework to measure the throughput and the response time of clients connected to a thread-based or event-driven server implementation.
*	EDA module: An event-based server using asynchronous, non-blocking socket communication implemented in Node.js.
*	TBA module: A thread-based server using sockets of the net library (blocking I/O) implemented in Java.