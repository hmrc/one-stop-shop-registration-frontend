#!/bin/bash

echo ""
echo "Applying migration DoNotNeedToRegister"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /doNotNeedToRegister                       controllers.DoNotNeedToRegisterController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "notSellingGoodsToEu.title = doNotNeedToRegister" >> ../conf/messages.en
echo "notSellingGoodsToEu.heading = doNotNeedToRegister" >> ../conf/messages.en

echo "Migration DoNotNeedToRegister completed"
