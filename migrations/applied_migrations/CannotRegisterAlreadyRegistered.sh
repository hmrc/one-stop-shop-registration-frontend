#!/bin/bash

echo ""
echo "Applying migration CannotRegisterAlreadyRegistered"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cannotRegisterAlreadyRegistered                       controllers.CannotRegisterAlreadyRegisteredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotRegisterAlreadyRegistered.title = cannotRegisterAlreadyRegistered" >> ../conf/messages.en
echo "cannotRegisterAlreadyRegistered.heading = cannotRegisterAlreadyRegistered" >> ../conf/messages.en

echo "Migration CannotRegisterAlreadyRegistered completed"
