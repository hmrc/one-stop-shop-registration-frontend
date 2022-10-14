#!/bin/bash

echo ""
echo "Applying migration AlreadyRegisteredOtherCountry"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /alreadyRegisteredOtherCountry                       controllers.AlreadyRegisteredOtherCountryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "alreadyRegisteredOtherCountry.title = alreadyRegisteredOtherCountry" >> ../conf/messages.en
echo "alreadyRegisteredOtherCountry.heading = alreadyRegisteredOtherCountry" >> ../conf/messages.en

echo "Migration AlreadyRegisteredOtherCountry completed"
