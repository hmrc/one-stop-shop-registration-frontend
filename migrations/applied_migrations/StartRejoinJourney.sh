#!/bin/bash

echo ""
echo "Applying migration StartRejoinJourney"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /startRejoinJourney                       controllers.StartRejoinJourneyController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "startRejoinJourney.title = startRejoinJourney" >> ../conf/messages.en
echo "startRejoinJourney.heading = startRejoinJourney" >> ../conf/messages.en

echo "Migration StartRejoinJourney completed"
