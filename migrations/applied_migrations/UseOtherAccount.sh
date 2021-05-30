#!/bin/bash

echo ""
echo "Applying migration UseOtherAccount"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /useOtherAccount                       controllers.UseOtherAccountController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "useOtherAccount.title = useOtherAccount" >> ../conf/messages.en
echo "useOtherAccount.heading = useOtherAccount" >> ../conf/messages.en

echo "Migration UseOtherAccount completed"
