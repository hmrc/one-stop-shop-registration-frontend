#!/bin/bash

echo ""
echo "Applying migration PreviousIossNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /previousIossNumber                        controllers.PreviousIossNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /previousIossNumber                        controllers.PreviousIossNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePreviousIossNumber                  controllers.PreviousIossNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePreviousIossNumber                  controllers.PreviousIossNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "previousIossNumber.title = previousIossNumber" >> ../conf/messages.en
echo "previousIossNumber.heading = previousIossNumber" >> ../conf/messages.en
echo "previousIossNumber.checkYourAnswersLabel = previousIossNumber" >> ../conf/messages.en
echo "previousIossNumber.error.required = Enter previousIossNumber" >> ../conf/messages.en
echo "previousIossNumber.error.length = PreviousIossNumber must be 100 characters or less" >> ../conf/messages.en
echo "previousIossNumber.change.hidden = PreviousIossNumber" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousIossNumberUserAnswersEntry: Arbitrary[(PreviousIossNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PreviousIossNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousIossNumberPage: Arbitrary[PreviousIossNumberPage.type] =";\
    print "    Arbitrary(PreviousIossNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PreviousIossNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PreviousIossNumber completed"
