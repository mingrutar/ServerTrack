Per Christopher Selzo <Christopher.Selzo@ctl.io> from CenturyLink

Server Load

This specific challenge is called ServerTrack and allows you to demonstrate how you might 
implement a basic server monitoring system.

In this project, two API endpoints are necessary. They are:
1. Record load for a given server

This should take a:

• server name (string)
• CPU load (double)
• RAM load (double)

And apply the values to an in-memory model used to provide the data in endpoint #2.

2. Display loads for a given server:

This should return data (if it has any) for the given server:
• A list of the average load values for the last 60 minutes broken down by minute
• A list of the average load values for the last 24 hours broken down by hour

Both endpoints would be under a continuous load being called multiple times a second. 
There is no need to persist the results to storage.

to run:
     java -jar target/century-link-0.0.1-SNAPSHOT.jar
     
    java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -jar target/century-link-0.0.1-SNAPSHOT.jar    