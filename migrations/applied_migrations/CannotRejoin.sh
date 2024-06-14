#!/bin/bash

echo ""
echo "Applying migration CannotRejoin"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cannotRejoin                       controllers.CannotRejoinController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotRejoin.title = cannotRejoin" >> ../conf/messages.en
echo "cannotRejoin.heading = cannotRejoin" >> ../conf/messages.en

echo "Migration CannotRejoin completed"
