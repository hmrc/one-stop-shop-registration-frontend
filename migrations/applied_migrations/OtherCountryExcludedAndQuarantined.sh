#!/bin/bash

echo ""
echo "Applying migration OtherCountryExcludedAndQuarantined"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /otherCountryExcludedAndQuarantined                       controllers.OtherCountryExcludedAndQuarantinedController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "otherCountryExcludedAndQuarantined.title = otherCountryExcludedAndQuarantined" >> ../conf/messages.en
echo "otherCountryExcludedAndQuarantined.heading = otherCountryExcludedAndQuarantined" >> ../conf/messages.en

echo "Migration OtherCountryExcludedAndQuarantined completed"
