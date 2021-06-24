#!/bin/bash

echo ""
echo "Applying migration NotLiableForVat"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /notLiableForVat                       controllers.NotLiableForVatController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "notLiableForVat.title = notLiableForVat" >> ../conf/messages.en
echo "notLiableForVat.heading = notLiableForVat" >> ../conf/messages.en

echo "Migration NotLiableForVat completed"
