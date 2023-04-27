#!/bin/bash

echo ""
echo "Applying migration DeleteAllWebsites"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deleteAllWebsites                        controllers.DeleteAllWebsitesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deleteAllWebsites                        controllers.DeleteAllWebsitesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeleteAllWebsites                  controllers.DeleteAllWebsitesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeleteAllWebsites                  controllers.DeleteAllWebsitesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteAllWebsites.title = deleteAllWebsites" >> ../conf/messages.en
echo "deleteAllWebsites.heading = deleteAllWebsites" >> ../conf/messages.en
echo "deleteAllWebsites.checkYourAnswersLabel = deleteAllWebsites" >> ../conf/messages.en
echo "deleteAllWebsites.error.required = Select yes if deleteAllWebsites" >> ../conf/messages.en
echo "deleteAllWebsites.change.hidden = DeleteAllWebsites" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllWebsitesUserAnswersEntry: Arbitrary[(DeleteAllWebsitesPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeleteAllWebsitesPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllWebsitesPage: Arbitrary[DeleteAllWebsitesPage.type] =";\
    print "    Arbitrary(DeleteAllWebsitesPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeleteAllWebsitesPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeleteAllWebsites completed"
