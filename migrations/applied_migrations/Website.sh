#!/bin/bash

echo ""
echo "Applying migration Website"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /website                        controllers.WebsiteController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /website                        controllers.WebsiteController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeWebsite                  controllers.WebsiteController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeWebsite                  controllers.WebsiteController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "website.title = website" >> ../conf/messages.en
echo "website.heading = website" >> ../conf/messages.en
echo "website.checkYourAnswersLabel = website" >> ../conf/messages.en
echo "website.error.required = Enter website" >> ../conf/messages.en
echo "website.error.length = Website must be 100 characters or less" >> ../conf/messages.en
echo "website.change.hidden = Website" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWebsiteUserAnswersEntry: Arbitrary[(WebsitePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[WebsitePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryWebsitePage: Arbitrary[WebsitePage.type] =";\
    print "    Arbitrary(WebsitePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(WebsitePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration Website completed"
