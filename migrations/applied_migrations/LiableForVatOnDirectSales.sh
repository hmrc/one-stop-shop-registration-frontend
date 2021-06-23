#!/bin/bash

echo ""
echo "Applying migration LiableForVatOnDirectSales"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /liableForVatOnDirectSales                       controllers.LiableForVatOnDirectSalesController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "liableForVatOnDirectSales.title = liableForVatOnDirectSales" >> ../conf/messages.en
echo "liableForVatOnDirectSales.heading = liableForVatOnDirectSales" >> ../conf/messages.en

echo "Migration LiableForVatOnDirectSales completed"
