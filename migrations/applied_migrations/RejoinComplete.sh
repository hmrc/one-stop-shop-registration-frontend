#!/bin/bash

echo ""
echo "Applying migration RejoinComplete"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /rejoinComplete                       controllers.RejoinCompleteController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "rejoinComplete.title = rejoinComplete" >> ../conf/messages.en
echo "rejoinComplete.heading = rejoinComplete" >> ../conf/messages.en

echo "Migration RejoinComplete completed"
