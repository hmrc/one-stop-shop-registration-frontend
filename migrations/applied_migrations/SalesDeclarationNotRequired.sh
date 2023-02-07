#!/bin/bash

echo ""
echo "Applying migration SalesDeclarationNotRequired"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /salesDeclarationNotRequired                       controllers.SalesDeclarationNotRequiredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "salesDeclarationNotRequired.title = salesDeclarationNotRequired" >> ../conf/messages.en
echo "salesDeclarationNotRequired.heading = salesDeclarationNotRequired" >> ../conf/messages.en

echo "Migration SalesDeclarationNotRequired completed"
