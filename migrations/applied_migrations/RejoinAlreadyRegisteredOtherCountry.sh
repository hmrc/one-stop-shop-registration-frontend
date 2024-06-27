#!/bin/bash

echo ""
echo "Applying migration RejoinAlreadyRegisteredOtherCountry"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /rejoinAlreadyRegisteredOtherCountry                       controllers.RejoinAlreadyRegisteredOtherCountryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "rejoinAlreadyRegisteredOtherCountry.title = rejoinAlreadyRegisteredOtherCountry" >> ../conf/messages.en
echo "rejoinAlreadyRegisteredOtherCountry.heading = rejoinAlreadyRegisteredOtherCountry" >> ../conf/messages.en

echo "Migration RejoinAlreadyRegisteredOtherCountry completed"
