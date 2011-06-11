call StopDemoApp.bat
mvn scala:run -DmainClass=org.agilewiki.tests.TestApp -DaddArgs="Master|localhost|4444|testFiles|testFiles/organizer.aw5db"