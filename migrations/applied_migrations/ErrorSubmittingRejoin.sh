#!/bin/bash

echo ""
echo "Applying migration ErrorSubmittingRejoin"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /errorSubmittingRejoin                       controllers.ErrorSubmittingRejoinController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "errorSubmittingRejoin.title = errorSubmittingRejoin" >> ../conf/messages.en
echo "errorSubmittingRejoin.heading = errorSubmittingRejoin" >> ../conf/messages.en

echo "Migration ErrorSubmittingRejoin completed"
