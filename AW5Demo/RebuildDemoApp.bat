call StopDemoApp.bat
mvn scala:run -DmainClass=org.agilewiki.tests.RebuildTestApp -DaddArgs="Master|testFiles|testFiles/organizer.aw5db"