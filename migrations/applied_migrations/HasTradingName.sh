#!/bin/bash

echo ""
echo "Applying migration HasTradingName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /hasTradingName                        controllers.HasTradingNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /hasTradingName                        controllers.HasTradingNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHasTradingName                  controllers.HasTradingNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHasTradingName                  controllers.HasTradingNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "hasTradingName.title = hasTradingName" >> ../conf/messages.en
echo "hasTradingName.heading = hasTradingName" >> ../conf/messages.en
echo "hasTradingName.checkYourAnswersLabel = hasTradingName" >> ../conf/messages.en
echo "hasTradingName.error.required = Select yes if hasTradingName" >> ../conf/messages.en
echo "hasTradingName.change.hidden = HasTradingName" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasTradingNameUserAnswersEntry: Arbitrary[(HasTradingNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HasTradingNamePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasTradingNamePage: Arbitrary[HasTradingNamePage.type] =";\
    print "    Arbitrary(HasTradingNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HasTradingNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration HasTradingName completed"
