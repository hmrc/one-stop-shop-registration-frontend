#!/bin/bash

echo ""
echo "Applying migration NoRegistrationInProgress"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /noRegistrationInProgress                       controllers.NoRegistrationInProgressController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "noRegistrationInProgress.title = noRegistrationInProgress" >> ../conf/messages.en
echo "noRegistrationInProgress.heading = noRegistrationInProgress" >> ../conf/messages.en

echo "Migration NoRegistrationInProgress completed"
