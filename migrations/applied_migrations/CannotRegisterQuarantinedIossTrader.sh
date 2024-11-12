#!/bin/bash

echo ""
echo "Applying migration CannotRegisterQuarantinedIossTrader"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /cannotRegisterQuarantinedIossTrader                       controllers.CannotRegisterQuarantinedIossTraderController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotRegisterQuarantinedIossTrader.title = cannotRegisterQuarantinedIossTrader" >> ../conf/messages.en
echo "cannotRegisterQuarantinedIossTrader.heading = cannotRegisterQuarantinedIossTrader" >> ../conf/messages.en

echo "Migration CannotRegisterQuarantinedIossTrader completed"
