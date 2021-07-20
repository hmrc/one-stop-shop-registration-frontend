#!/bin/bash

echo ""
echo "Applying migration IsPlanningFirstEligibleSale"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /isPlanningFirstEligibleSale                        controllers.IsPlanningFirstEligibleSaleController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /isPlanningFirstEligibleSale                        controllers.IsPlanningFirstEligibleSaleController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIsPlanningFirstEligibleSale                  controllers.IsPlanningFirstEligibleSaleController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIsPlanningFirstEligibleSale                  controllers.IsPlanningFirstEligibleSaleController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "isPlanningFirstEligibleSale.title = isPlanningFirstEligibleSale" >> ../conf/messages.en
echo "isPlanningFirstEligibleSale.heading = isPlanningFirstEligibleSale" >> ../conf/messages.en
echo "isPlanningFirstEligibleSale.checkYourAnswersLabel = isPlanningFirstEligibleSale" >> ../conf/messages.en
echo "isPlanningFirstEligibleSale.error.required = Select yes if isPlanningFirstEligibleSale" >> ../conf/messages.en
echo "isPlanningFirstEligibleSale.change.hidden = IsPlanningFirstEligibleSale" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsPlanningFirstEligibleSaleUserAnswersEntry: Arbitrary[(IsPlanningFirstEligibleSalePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IsPlanningFirstEligibleSalePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsPlanningFirstEligibleSalePage: Arbitrary[IsPlanningFirstEligibleSalePage.type] =";\
    print "    Arbitrary(IsPlanningFirstEligibleSalePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IsPlanningFirstEligibleSalePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration IsPlanningFirstEligibleSale completed"
