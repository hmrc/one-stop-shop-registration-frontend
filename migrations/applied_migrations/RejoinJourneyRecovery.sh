#!/bin/bash

echo ""
echo "Applying migration RejoinJourneyRecovery"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /rejoinJourneyRecovery                       controllers.RejoinJourneyRecoveryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "rejoinJourneyRecovery.title = rejoinJourneyRecovery" >> ../conf/messages.en
echo "rejoinJourneyRecovery.heading = rejoinJourneyRecovery" >> ../conf/messages.en

echo "Migration RejoinJourneyRecovery completed"
