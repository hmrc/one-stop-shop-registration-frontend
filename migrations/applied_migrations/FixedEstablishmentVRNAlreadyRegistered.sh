#!/bin/bash

echo ""
echo "Applying migration FixedEstablishmentVRNAlreadyRegistered"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /fixedEstablishmentVRNAlreadyRegistered                       controllers.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "fixedEstablishmentVRNAlreadyRegistered.title = fixedEstablishmentVRNAlreadyRegistered" >> ../conf/messages.en
echo "fixedEstablishmentVRNAlreadyRegistered.heading = fixedEstablishmentVRNAlreadyRegistered" >> ../conf/messages.en

echo "Migration FixedEstablishmentVRNAlreadyRegistered completed"
