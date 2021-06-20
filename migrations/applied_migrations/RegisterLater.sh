#!/bin/bash

echo ""
echo "Applying migration RegisterLater"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /registerLater                       controllers.RegisterLaterController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "registerLater.title = registerLater" >> ../conf/messages.en
echo "registerLater.heading = registerLater" >> ../conf/messages.en

echo "Migration RegisterLater completed"
