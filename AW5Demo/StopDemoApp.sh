#!/bin/bash
mvn scala:run -DmainClass=org.agilewiki.tests.ShutdownTestApp -DaddArgs="Master|localhost|4444"
