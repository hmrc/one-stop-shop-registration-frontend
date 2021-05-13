#!/bin/bash

echo ""
echo "Applying migration ApplicationComplete"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /applicationComplete                       controllers.ApplicationCompleteController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "applicationComplete.title = applicationComplete" >> ../conf/messages.en
echo "applicationComplete.heading = applicationComplete" >> ../conf/messages.en

echo "Migration ApplicationComplete completed"
