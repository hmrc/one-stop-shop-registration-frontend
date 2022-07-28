#!/bin/bash

echo ""
echo "Applying migration VatApiDown"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /vatApiDown                       controllers.VatApiDownController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vatApiDown.title = vatApiDown" >> ../conf/messages.en
echo "vatApiDown.heading = vatApiDown" >> ../conf/messages.en

echo "Migration VatApiDown completed"
