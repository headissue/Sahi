----
Sahi
----

Installing Sahi
---------------
1) Unzip sahi.zip to a desired location

Getting started
---------------
Sahi uses a proxy to inject javascript into existing web applications.
In order to use Sahi,
1) The sahi proxy server has to be running
2) The browser has to use Sahi as its proxy server

Starting the proxy server:
    Windows:
    - Go to <sahi_root>\bin and run sahi.bat
    Linux
    - Go to <sahi_root>/bin and run sahi.sh

Configuring the browser:
    Firefox:
    - Go to Tools > Options > General > Connection Settings >
    - Set to "Manual Proxy Configuration"
    - Set "HTTP Proxy" to "localhost"
    - Set "Port" to "9999". (This setting can be modified through <sahi_root>/config/sahi.properties)
    - Keep "Use the same proxy for all protocol" unchecked as Sahi does not understand protocols other than HTTP
    - NOTE: "No Proxy for" should NOT have localhost in it.

    Internet Explorer:
    - Go to Tools > Internet Options > Connections > LAN Settings >
    - In "Proxy server" section, Check "Use a proxy server for your LAN"
    - Set "Address" to "localhost"
    - Set "Port" to "9999"
    - Leave "Bypass proxy server for local addresses" unchecked
    - OK > OK :);


Recording through Sahi
-----------------------

- Press Alt and double click on the window which you want to record.
- On the popup that appears, give a name for the script you wish to generate, and click 'Record'
- Refresh the main page
- Most actions on the page will now get recorded. 
- Moving the mouse over any html element while pressing Ctrl key will show the javascript accessor info in the popup.
- With Ctrl key pressed, pointing an element and then pressing Q adds assertions for the element.


The script being generated can be viewed by opening the sah file in the scripts directory.
(Open it in textpad. Intellij automatically starts saving and it messes the file);

- Once done, click stop.


Known issues
------------
- Form submit by pressing "enter" does not get recorded


Playing back
-------------
- Close and reopen (alt-dblclick) the popup window.
- Select script file just created from the dropdown, and click 'Set'
- Navigate to the page where you started recording. Click 'Play'


Scripting
---------
- Statements can be clubbed into javascript functions.
- Variables can be declared like in javascript. But they need to prefixed with $


Running test from the console
------------------------------
If you wish to run a script called "MySahiScript" on a page like say, http://www.domain.com/dir/page1.jsp do
"C:\Program Files\Mozilla Firefox\firefox.exe" "http://www.domain.com/_s_/dyn/auto?file=MySahiScript&startUrl=http://www.domain.com/dir/page1.jsp" 
                                                       ^^^^^^^^^^^^^^	                                             ^^^^^^^^^^^^^^										
Note that the underlined domains should be the same. 
Yeah, this needs some clean up.


Including files
---------------
One sah file may include other sah files, thus allowing logical separation of code.
Syntax: _include(file2.sah)


Suites
------
Multiple tests can be run through ant. For each test in the suite, the ant target opens a browser, runs the test and closes the browser, 
The ant target specifies a suite file. 
The suite file syntax is:

test1.sah	/startPageForTest1.jsp
test2.sah	http://www.d2.com/startPageForTest2.htm
test3.sah	abc/startPageForTest3.htm

The ant target is like:

<taskdef name="sahi" classname="com.sahi.ant.RunSahiTask" classpathref="library.sahi.classpath">
</taskdef>
<target name="runsuite" description="invoke suite">
    <sahi suite="../scripts/my.suite" 
    	browser="C:\\Program Files\\Internet Explorer\\iexplore.exe" 
    	baseurl="http://localhost/myapp/" 
    	sahihost="localhost" 
    	sahiport="9999"/>
</target>


<target name="sahireport" description="show report">
	<exec command="C:\\Program Files\\Internet Explorer\\iexplore.exe http://localhost/_s_/spr/logs/"/>
</target>
    

Logging
-------
Logs are created in "logs/playback/" directory.
Log results can be accessed through the browser through http://mydomain/_s_/logs/


Limitations
------------
- All web pages need to be from the same domain

Not yet supported, but coming in
---------------------------------
- Window.open not yet supported
- Form submit by pressing "enter" does not get recorded
- Javascript alerts, confirms and prompts not yet supported
- Input type=image not curre(c/n)tly supported

