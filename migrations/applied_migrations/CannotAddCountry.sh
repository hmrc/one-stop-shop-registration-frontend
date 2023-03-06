#!/bin/bash

echo ""
echo "Applying migration CannotAddCountry"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cannotAddCountry                       controllers.CannotAddCountryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotAddCountry.title = cannotAddCountry" >> ../conf/messages.en
echo "cannotAddCountry.heading = cannotAddCountry" >> ../conf/messages.en

echo "Migration CannotAddCountry completed"
