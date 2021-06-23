#!/bin/bash

echo ""
echo "Applying migration DateOfFirstSale"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /dateOfFirstSale                  controllers.DateOfFirstSaleController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /dateOfFirstSale                  controllers.DateOfFirstSaleController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDateOfFirstSale                        controllers.DateOfFirstSaleController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDateOfFirstSale                        controllers.DateOfFirstSaleController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "dateOfFirstSale.title = DateOfFirstSale" >> ../conf/messages.en
echo "dateOfFirstSale.heading = DateOfFirstSale" >> ../conf/messages.en
echo "dateOfFirstSale.hint = For example, 12 11 2007" >> ../conf/messages.en
echo "dateOfFirstSale.checkYourAnswersLabel = DateOfFirstSale" >> ../conf/messages.en
echo "dateOfFirstSale.error.required.all = Enter the dateOfFirstSale" >> ../conf/messages.en
echo "dateOfFirstSale.error.required.two = The dateOfFirstSale" must include {0} and {1} >> ../conf/messages.en
echo "dateOfFirstSale.error.required = The dateOfFirstSale must include {0}" >> ../conf/messages.en
echo "dateOfFirstSale.error.invalid = Enter a real DateOfFirstSale" >> ../conf/messages.en
echo "dateOfFirstSale.change.hidden = DateOfFirstSale" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDateOfFirstSaleUserAnswersEntry: Arbitrary[(DateOfFirstSalePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DateOfFirstSalePage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDateOfFirstSalePage: Arbitrary[DateOfFirstSalePage.type] =";\
    print "    Arbitrary(DateOfFirstSalePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DateOfFirstSalePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DateOfFirstSale completed"
