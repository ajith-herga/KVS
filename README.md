Distributed Key Value store

======

This system maintains a distributed key value store. The KVServer is responsible for running the grep server for supporting distributed logs and gossip server for supporting distributed membership.
It also introduces a KVClientRequestServer which responds to KVClientAPI methods via TCP and performs the required key value store operations.

It uses an MD5 hash function to map all machines as well as keys to circle and the keys are stored at the first machine after the key on the circle.


Steps to run the program:

1. cd to the folder in terminal and run "make" (this will compile the program)

2. run "source .settings_java" (this will load the environment settings and needs to be done always before the next step)

3. If this is the first node, execute "kvs"
You will see a machine_id in the format (hostname___port___timestamp) in the first 5 lines on the terminal after the server is started. This machine id needs to be used when you run the next machine.
If this is not the first node, run "kvs machine_id" using machine_id for some machine that has already joined.

4. To run the client run "kvc hostname port" - This will run the client program which will wait for your input. Supported commands are
    - insert key value(inserts key value pair to the appropriate machine)
    - show(returns all the key value pairs at the client's contact machine)
    - modify key value(updates the value for a key if the key exists)
    - delete key(deletes the key value pair for the key if it exists)
    - lookup key(returns the value for the key if it exists)

5. To make a machine leave the system voluntarily, use CTRL+C

6. To query the distributed logs run "GrepClient key:value"
Use ".*" (including the double quotes) in key or value if you wish to query for only one of them.

7. The logs are of the form: machine_id:(Joined/Left/Failed) at Date and Time. 
