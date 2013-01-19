<b>-= Pre requisit software =- </b>
<br/>
1. apache maven 3.0.x download from http://maven.apache.org/
<br/>
2. apache tomcat 7.x download from http://tomcat.apache.org/
<br/>
3. Git client 
<br/>
<br/>
<b>-= Get Sources code=-</b>
<br/>
1. use git client get source code from command "git clone git://github.com/nancom/SpringBirt.git" to disire derectory
<br/>
or
<br>
Download from Zip button
<br/>
<br/>
<b>-= Configuration =-</b>
<br/>
1. change tomcat 7.x http port 
   <br/>
   edit file conf/server.xml
    <br/>
    modify line <br/>``` <Connector connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"/> ```
    <br/> to         
            ```  <Connector connectionTimeout="20000" port="8014" protocol="HTTP/1.1" redirectPort="8443"/> ```
<br/>
2. edit file conf/tomcat-users.xml
   <br/>
   add line
   ```xml
       <role rolename="manager-gui"/>
       <role rolename="manager-script"/>
       <role rolename="admin-gui"/> 
       <role rolename="admin-script"/> 
       <user password="s3cret" roles="manager-script,manager-gui,admin-gui,admin-script" username="tomcat"/>
   ```
<br/> 
<br/>
<b>-= Run SpringBirt =-</b>
<br/>
1. Start tomcat 7.x first
<br/>
2. open terminal and go to SpringBirt directory
<br/>
3. input command "mvn clean compile package cargo:redeploy"
<br/>
4. after process finish open web browser and input "http://localhost:8014/SpringBirt"
<br/>
5. !! Enjoy !!
