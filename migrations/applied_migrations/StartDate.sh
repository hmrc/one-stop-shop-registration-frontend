#!/bin/bash

echo ""
echo "Applying migration StartDate"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /startDate                        controllers.StartDateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /startDate                        controllers.StartDateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeStartDate                  controllers.StartDateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeStartDate                  controllers.StartDateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "startDate.title = startDate" >> ../conf/messages.en
echo "startDate.heading = startDate" >> ../conf/messages.en
echo "startDate.option1 = Option 1" >> ../conf/messages.en
echo "startDate.option2 = Option 2" >> ../conf/messages.en
echo "startDate.checkYourAnswersLabel = startDate" >> ../conf/messages.en
echo "startDate.error.required = Select startDate" >> ../conf/messages.en
echo "startDate.change.hidden = StartDate" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryStartDateUserAnswersEntry: Arbitrary[(StartDatePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[StartDatePage.type]";\
    print "        value <- arbitrary[StartDate].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryStartDatePage: Arbitrary[StartDatePage.type] =";\
    print "    Arbitrary(StartDatePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryStartDate: Arbitrary[StartDate] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(StartDate.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(StartDatePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration StartDate completed"
