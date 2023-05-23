#!/bin/bash

echo ""
echo "Applying migration CancelAmendRegistration"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cancelAmendRegistration                       controllers.CancelAmendRegistrationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cancelAmendRegistration.title = cancelAmendRegistration" >> ../conf/messages.en
echo "cancelAmendRegistration.heading = cancelAmendRegistration" >> ../conf/messages.en

echo "Migration CancelAmendRegistration completed"
