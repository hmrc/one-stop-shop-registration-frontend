#!/bin/bash

echo ""
echo "Applying migration BusinessBasedInNi"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /businessBasedInNi                        controllers.BusinessBasedInNiController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /businessBasedInNi                        controllers.BusinessBasedInNiController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeBusinessBasedInNi                  controllers.BusinessBasedInNiController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeBusinessBasedInNi                  controllers.BusinessBasedInNiController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "businessBasedInNi.title = businessBasedInNi" >> ../conf/messages.en
echo "businessBasedInNi.heading = businessBasedInNi" >> ../conf/messages.en
echo "businessBasedInNi.checkYourAnswersLabel = businessBasedInNi" >> ../conf/messages.en
echo "businessBasedInNi.error.required = Select yes if businessBasedInNi" >> ../conf/messages.en
echo "businessBasedInNi.change.hidden = BusinessBasedInNi" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessBasedInNiUserAnswersEntry: Arbitrary[(BusinessBasedInNiPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[BusinessBasedInNiPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessBasedInNiPage: Arbitrary[BusinessBasedInNiPage.type] =";\
    print "    Arbitrary(BusinessBasedInNiPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(BusinessBasedInNiPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration BusinessBasedInNi completed"
