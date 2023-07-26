#!/bin/bash

echo ""
echo "Applying migration AmendJourneyRecovery"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /amendJourneyRecovery                       controllers.AmendJourneyRecoveryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendJourneyRecovery.title = amendJourneyRecovery" >> ../conf/messages.en
echo "amendJourneyRecovery.heading = amendJourneyRecovery" >> ../conf/messages.en

echo "Migration AmendJourneyRecovery completed"
