#!/bin/bash

echo ""
echo "Applying migration LiableForVatOnAllSales"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /liableForVatOnAllSales                       controllers.LiableForVatOnAllSalesController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "liableForVatOnAllSales.title = liableForVatOnAllSales" >> ../conf/messages.en
echo "liableForVatOnAllSales.heading = liableForVatOnAllSales" >> ../conf/messages.en

echo "Migration LiableForVatOnAllSales completed"
