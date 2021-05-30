#!/bin/bash

echo ""
echo "Applying migration AlreadyRegistered"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /alreadyRegistered                       controllers.AlreadyRegisteredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "alreadyRegistered.title = alreadyRegistered" >> ../conf/messages.en
echo "alreadyRegistered.heading = alreadyRegistered" >> ../conf/messages.en

echo "Migration AlreadyRegistered completed"
