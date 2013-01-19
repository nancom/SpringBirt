-= Pre requisit software =-
1. apache maven 3.0.x download from http://maven.apache.org/
2. apache tomcat 7.x download from http://tomcat.apache.org/

-= Configuration =-
1. change tomcat 7.x http port 
   edit file conf/server.xml
   modify line <Connector connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"/>
          to <Connector connectionTimeout="20000" port="8014" protocol="HTTP/1.1" redirectPort="8443"/>
2. edit file conf/tomcat-users.xml
   add line
       <role rolename="manager-gui"/>
      <role rolename="manager-script"/>
       <role rolename="admin-gui"/>
       <role rolename="admin-script"/>
       <user password="s3cret" roles="manager-script,manager-gui,admin-gui,admin-script" username="tomcat"/>
   
-= Run SpringBirt =-
1. Start tomcat 7.x first
2. open terminal and go to SpringBirt directory
3. input command "mvn clean compile package cargo:redeploy"
4. after process finish open web browser and input "http://localhost:8014/SpringBirt"
5. !! Enjoy !!
