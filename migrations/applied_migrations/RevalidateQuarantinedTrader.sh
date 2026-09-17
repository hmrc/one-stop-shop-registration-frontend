#!/bin/bash

echo ""
echo "Applying migration RevalidateQuarantinedTrader"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /revalidateQuarantinedTrader                       controllers.RevalidateQuarantinedTraderController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "revalidateQuarantinedTrader.title = revalidateQuarantinedTrader" >> ../conf/messages.en
echo "revalidateQuarantinedTrader.heading = revalidateQuarantinedTrader" >> ../conf/messages.en

echo "Migration RevalidateQuarantinedTrader completed"
