#!/bin/bash

echo ""
echo "Applying migration RevalidateAlreadyRegistered"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /revalidateAlreadyRegistered                       controllers.RevalidateAlreadyRegisteredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "revalidateAlreadyRegistered.title = revalidateAlreadyRegistered" >> ../conf/messages.en
echo "revalidateAlreadyRegistered.heading = revalidateAlreadyRegistered" >> ../conf/messages.en

echo "Migration RevalidateAlreadyRegistered completed"
