#!/bin/bash

echo ""
echo "Applying migration CannotRemoveExistingPreviousRegistrations"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cannotRemoveExistingPreviousRegistrations                       controllers.CannotRemoveExistingPreviousRegistrationsController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotRemoveExistingPreviousRegistrations.title = cannotRemoveExistingPreviousRegistrations" >> ../conf/messages.en
echo "cannotRemoveExistingPreviousRegistrations.heading = cannotRemoveExistingPreviousRegistrations" >> ../conf/messages.en

echo "Migration CannotRemoveExistingPreviousRegistrations completed"
