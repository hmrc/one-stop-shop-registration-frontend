#!/bin/bash

echo ""
echo "Applying migration CannotRejoinQuarantinedCountry"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cannotRejoinQuarantinedCountry                       controllers.CannotRejoinQuarantinedCountryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotRejoinQuarantinedCountry.title = cannotRejoinQuarantinedCountry" >> ../conf/messages.en
echo "cannotRejoinQuarantinedCountry.heading = cannotRejoinQuarantinedCountry" >> ../conf/messages.en

echo "Migration CannotRejoinQuarantinedCountry completed"
