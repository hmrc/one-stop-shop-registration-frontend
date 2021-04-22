#!/bin/bash

echo ""
echo "Applying migration PartOfVatGroup"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /partOfVatGroup                        controllers.PartOfVatGroupController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /partOfVatGroup                        controllers.PartOfVatGroupController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePartOfVatGroup                  controllers.PartOfVatGroupController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePartOfVatGroup                  controllers.PartOfVatGroupController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "partOfVatGroup.title = partOfVatGroup" >> ../conf/messages.en
echo "partOfVatGroup.heading = partOfVatGroup" >> ../conf/messages.en
echo "partOfVatGroup.checkYourAnswersLabel = partOfVatGroup" >> ../conf/messages.en
echo "partOfVatGroup.error.required = Select yes if partOfVatGroup" >> ../conf/messages.en
echo "partOfVatGroup.change.hidden = PartOfVatGroup" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPartOfVatGroupUserAnswersEntry: Arbitrary[(PartOfVatGroupPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PartOfVatGroupPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPartOfVatGroupPage: Arbitrary[PartOfVatGroupPage.type] =";\
    print "    Arbitrary(PartOfVatGroupPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PartOfVatGroupPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PartOfVatGroup completed"
