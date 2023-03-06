#!/bin/bash

echo ""
echo "Applying migration DeletePreviousScheme"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deletePreviousScheme                        controllers.DeletePreviousSchemeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deletePreviousScheme                        controllers.DeletePreviousSchemeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeletePreviousScheme                  controllers.DeletePreviousSchemeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeletePreviousScheme                  controllers.DeletePreviousSchemeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deletePreviousScheme.title = deletePreviousScheme" >> ../conf/messages.en
echo "deletePreviousScheme.heading = deletePreviousScheme" >> ../conf/messages.en
echo "deletePreviousScheme.checkYourAnswersLabel = deletePreviousScheme" >> ../conf/messages.en
echo "deletePreviousScheme.error.required = Select yes if deletePreviousScheme" >> ../conf/messages.en
echo "deletePreviousScheme.change.hidden = DeletePreviousScheme" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeletePreviousSchemeUserAnswersEntry: Arbitrary[(DeletePreviousSchemePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeletePreviousSchemePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeletePreviousSchemePage: Arbitrary[DeletePreviousSchemePage.type] =";\
    print "    Arbitrary(DeletePreviousSchemePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeletePreviousSchemePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeletePreviousScheme completed"
