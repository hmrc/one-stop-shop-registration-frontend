#!/bin/bash

echo ""
echo "Applying migration HasWebsite"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /hasWebsite                        controllers.HasWebsiteController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /hasWebsite                        controllers.HasWebsiteController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHasWebsite                  controllers.HasWebsiteController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHasWebsite                  controllers.HasWebsiteController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "hasWebsite.title = hasWebsite" >> ../conf/messages.en
echo "hasWebsite.heading = hasWebsite" >> ../conf/messages.en
echo "hasWebsite.checkYourAnswersLabel = hasWebsite" >> ../conf/messages.en
echo "hasWebsite.error.required = Select yes if hasWebsite" >> ../conf/messages.en
echo "hasWebsite.change.hidden = HasWebsite" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasWebsiteUserAnswersEntry: Arbitrary[(HasWebsitePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HasWebsitePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasWebsitePage: Arbitrary[HasWebsitePage.type] =";\
    print "    Arbitrary(HasWebsitePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HasWebsitePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration HasWebsite completed"
