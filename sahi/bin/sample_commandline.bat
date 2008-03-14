..\tools\toggle_IE_proxy.exe enable
java -cp ..\lib\ant-sahi.jar net.sf.sahi.test.TestRunner D:/kamlesh/sahi/scripts/demo/demo.suite "C:\Program Files\Internet Explorer\IEXPLORE.EXE" http://rashmi:10000/demo/ default localhost 9999 3
..\tools\toggle_IE_proxy.exe disable
