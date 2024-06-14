#!/bin/bash

echo ""
echo "Applying migration RejoinRegistration"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /rejoinRegistration                       controllers.RejoinRegistrationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "rejoinRegistration.title = rejoinRegistration" >> ../conf/messages.en
echo "rejoinRegistration.heading = rejoinRegistration" >> ../conf/messages.en

echo "Migration RejoinRegistration completed"
