#!/bin/bash

echo ""
echo "Applying migration DeleteAllPreviousRegistrations"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deleteAllPreviousRegistrations                        controllers.DeleteAllPreviousRegistrationsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deleteAllPreviousRegistrations                        controllers.DeleteAllPreviousRegistrationsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeleteAllPreviousRegistrations                  controllers.DeleteAllPreviousRegistrationsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeleteAllPreviousRegistrations                  controllers.DeleteAllPreviousRegistrationsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteAllPreviousRegistrations.title = deleteAllPreviousRegistrations" >> ../conf/messages.en
echo "deleteAllPreviousRegistrations.heading = deleteAllPreviousRegistrations" >> ../conf/messages.en
echo "deleteAllPreviousRegistrations.checkYourAnswersLabel = deleteAllPreviousRegistrations" >> ../conf/messages.en
echo "deleteAllPreviousRegistrations.error.required = Select yes if deleteAllPreviousRegistrations" >> ../conf/messages.en
echo "deleteAllPreviousRegistrations.change.hidden = DeleteAllPreviousRegistrations" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllPreviousRegistrationsUserAnswersEntry: Arbitrary[(DeleteAllPreviousRegistrationsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeleteAllPreviousRegistrationsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllPreviousRegistrationsPage: Arbitrary[DeleteAllPreviousRegistrationsPage.type] =";\
    print "    Arbitrary(DeleteAllPreviousRegistrationsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeleteAllPreviousRegistrationsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeleteAllPreviousRegistrations completed"
