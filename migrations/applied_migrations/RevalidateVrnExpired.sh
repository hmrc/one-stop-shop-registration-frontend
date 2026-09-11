#!/bin/bash

echo ""
echo "Applying migration RevalidateVrnExpired"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /revalidateVrnExpired                       controllers.RevalidateVrnExpiredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "revalidateVrnExpired.title = revalidateVrnExpired" >> ../conf/messages.en
echo "revalidateVrnExpired.heading = revalidateVrnExpired" >> ../conf/messages.en

echo "Migration RevalidateVrnExpired completed"
