#!/bin/bash

echo ""
echo "Applying migration NiProtocolRejection"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /niProtocolRejection                       controllers.NiProtocolRejectionController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "niProtocolRejection.title = niProtocolRejection" >> ../conf/messages.en
echo "niProtocolRejection.heading = niProtocolRejection" >> ../conf/messages.en

echo "Migration NiProtocolRejection completed"
