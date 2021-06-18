#!/bin/bash

echo ""
echo "Applying migration CommencementDate"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /commencementDate                       controllers.CommencementDateController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "commencementDate.title = commencementDate" >> ../conf/messages.en
echo "commencementDate.heading = commencementDate" >> ../conf/messages.en

echo "Migration CommencementDate completed"
