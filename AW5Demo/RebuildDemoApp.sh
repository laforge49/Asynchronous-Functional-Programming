#!/bin/bash
mvn scala:run -DmainClass=org.agilewiki.tests.ShutdownTestApp -DaddArgs="Master|localhost|4444"
mvn scala:run -DmainClass=org.agilewiki.tests.RebuildTestApp -DaddArgs="Master|localhost|testFiles|testFiles\organizer.aw5db"
