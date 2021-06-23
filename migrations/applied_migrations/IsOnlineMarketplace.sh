#!/bin/bash

echo ""
echo "Applying migration IsOnlineMarketplace"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /isOnlineMarketplace                        controllers.IsOnlineMarketplaceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /isOnlineMarketplace                        controllers.IsOnlineMarketplaceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIsOnlineMarketplace                  controllers.IsOnlineMarketplaceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIsOnlineMarketplace                  controllers.IsOnlineMarketplaceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "isOnlineMarketplace.title = isOnlineMarketplace" >> ../conf/messages.en
echo "isOnlineMarketplace.heading = isOnlineMarketplace" >> ../conf/messages.en
echo "isOnlineMarketplace.checkYourAnswersLabel = isOnlineMarketplace" >> ../conf/messages.en
echo "isOnlineMarketplace.error.required = Select yes if isOnlineMarketplace" >> ../conf/messages.en
echo "isOnlineMarketplace.change.hidden = IsOnlineMarketplace" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsOnlineMarketplaceUserAnswersEntry: Arbitrary[(IsOnlineMarketplacePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IsOnlineMarketplacePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsOnlineMarketplacePage: Arbitrary[IsOnlineMarketplacePage.type] =";\
    print "    Arbitrary(IsOnlineMarketplacePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IsOnlineMarketplacePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration IsOnlineMarketplace completed"
