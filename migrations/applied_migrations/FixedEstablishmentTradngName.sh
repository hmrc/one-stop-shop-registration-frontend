#!/bin/bash

echo ""
echo "Applying migration FixedEstablishmentTradingName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /fixedEstablishmentTradingName                        controllers.FixedEstablishmentTradingNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /fixedEstablishmentTradingName                        controllers.FixedEstablishmentTradingNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeFixedEstablishmentTradingName                  controllers.FixedEstablishmentTradingNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeFixedEstablishmentTradingName                  controllers.FixedEstablishmentTradingNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "fixedEstablishmentTradingName.title = fixedEstablishmentTradingName" >> ../conf/messages.en
echo "fixedEstablishmentTradingName.heading = fixedEstablishmentTradingName" >> ../conf/messages.en
echo "fixedEstablishmentTradingName.checkYourAnswersLabel = fixedEstablishmentTradingName" >> ../conf/messages.en
echo "fixedEstablishmentTradingName.error.required = Enter fixedEstablishmentTradingName" >> ../conf/messages.en
echo "fixedEstablishmentTradingName.error.length = FixedEstablishmentTradingName must be 100 characters or less" >> ../conf/messages.en
echo "fixedEstablishmentTradingName.change.hidden = FixedEstablishmentTradingName" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryFixedEstablishmentTradingNameUserAnswersEntry: Arbitrary[(FixedEstablishmentTradingNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[FixedEstablishmentTradingNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryFixedEstablishmentTradingNamePage: Arbitrary[FixedEstablishmentTradingNamePage.type] =";\
    print "    Arbitrary(FixedEstablishmentTradingNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(FixedEstablishmentTradingNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration FixedEstablishmentTradingName completed"
