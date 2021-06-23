#!/bin/bash

echo ""
echo "Applying migration AllSalesViaMarketplace"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /allSalesViaMarketplace                        controllers.AllSalesViaMarketplaceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /allSalesViaMarketplace                        controllers.AllSalesViaMarketplaceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAllSalesViaMarketplace                  controllers.AllSalesViaMarketplaceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAllSalesViaMarketplace                  controllers.AllSalesViaMarketplaceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "allSalesViaMarketplace.title = allSalesViaMarketplace" >> ../conf/messages.en
echo "allSalesViaMarketplace.heading = allSalesViaMarketplace" >> ../conf/messages.en
echo "allSalesViaMarketplace.checkYourAnswersLabel = allSalesViaMarketplace" >> ../conf/messages.en
echo "allSalesViaMarketplace.error.required = Select yes if allSalesViaMarketplace" >> ../conf/messages.en
echo "allSalesViaMarketplace.change.hidden = AllSalesViaMarketplace" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAllSalesViaMarketplaceUserAnswersEntry: Arbitrary[(AllSalesViaMarketplacePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AllSalesViaMarketplacePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAllSalesViaMarketplacePage: Arbitrary[AllSalesViaMarketplacePage.type] =";\
    print "    Arbitrary(AllSalesViaMarketplacePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AllSalesViaMarketplacePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AllSalesViaMarketplace completed"
