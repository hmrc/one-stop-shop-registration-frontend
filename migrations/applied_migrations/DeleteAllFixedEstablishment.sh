#!/bin/bash

echo ""
echo "Applying migration DeleteAllFixedEstablishment"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /deleteAllFixedEstablishment                       controllers.DeleteAllFixedEstablishmentController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteAllFixedEstablishment.title = deleteAllFixedEstablishment" >> ../conf/messages.en
echo "deleteAllFixedEstablishment.heading = deleteAllFixedEstablishment" >> ../conf/messages.en

echo "Migration DeleteAllFixedEstablishment completed"
