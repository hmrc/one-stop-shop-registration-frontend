#!/bin/bash

echo ""
echo "Applying migration UpdateVatDetails"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /updateVatDetails                       controllers.UpdateVatDetailsController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "updateVatDetails.title = updateVatDetails" >> ../conf/messages.en
echo "updateVatDetails.heading = updateVatDetails" >> ../conf/messages.en

echo "Migration UpdateVatDetails completed"
