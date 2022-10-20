#!/bin/bash

echo ""
echo "Applying migration ExcludedVRN"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /excludedVRN                       controllers.ExcludedVRNController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "excludedVRN.title = excludedVRN" >> ../conf/messages.en
echo "excludedVRN.heading = excludedVRN" >> ../conf/messages.en

echo "Migration ExcludedVRN completed"
