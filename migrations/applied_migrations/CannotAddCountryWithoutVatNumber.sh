#!/bin/bash

echo ""
echo "Applying migration CannotAddCountryWithoutVatNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /cannotAddCountryWithoutVatNumber                        controllers.CannotAddCountryWithoutVatNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /cannotAddCountryWithoutVatNumber                        controllers.CannotAddCountryWithoutVatNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCannotAddCountryWithoutVatNumber                  controllers.CannotAddCountryWithoutVatNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCannotAddCountryWithoutVatNumber                  controllers.CannotAddCountryWithoutVatNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotAddCountryWithoutVatNumber.title = cannotAddCountryWithoutVatNumber" >> ../conf/messages.en
echo "cannotAddCountryWithoutVatNumber.heading = cannotAddCountryWithoutVatNumber" >> ../conf/messages.en
echo "cannotAddCountryWithoutVatNumber.checkYourAnswersLabel = cannotAddCountryWithoutVatNumber" >> ../conf/messages.en
echo "cannotAddCountryWithoutVatNumber.error.required = Select yes if cannotAddCountryWithoutVatNumber" >> ../conf/messages.en
echo "cannotAddCountryWithoutVatNumber.change.hidden = CannotAddCountryWithoutVatNumber" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCannotAddCountryWithoutVatNumberUserAnswersEntry: Arbitrary[(CannotAddCountryWithoutVatNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CannotAddCountryWithoutVatNumberPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCannotAddCountryWithoutVatNumberPage: Arbitrary[CannotAddCountryWithoutVatNumberPage.type] =";\
    print "    Arbitrary(CannotAddCountryWithoutVatNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CannotAddCountryWithoutVatNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration CannotAddCountryWithoutVatNumber completed"
