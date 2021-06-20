#!/bin/bash

echo ""
echo "Applying migration AlreadyMadeSales"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /alreadyMadeSales                        controllers.AlreadyMadeSalesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /alreadyMadeSales                        controllers.AlreadyMadeSalesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAlreadyMadeSales                  controllers.AlreadyMadeSalesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAlreadyMadeSales                  controllers.AlreadyMadeSalesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "alreadyMadeSales.title = alreadyMadeSales" >> ../conf/messages.en
echo "alreadyMadeSales.heading = alreadyMadeSales" >> ../conf/messages.en
echo "alreadyMadeSales.checkYourAnswersLabel = alreadyMadeSales" >> ../conf/messages.en
echo "alreadyMadeSales.error.required = Select yes if alreadyMadeSales" >> ../conf/messages.en
echo "alreadyMadeSales.change.hidden = AlreadyMadeSales" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAlreadyMadeSalesUserAnswersEntry: Arbitrary[(AlreadyMadeSalesPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AlreadyMadeSalesPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAlreadyMadeSalesPage: Arbitrary[AlreadyMadeSalesPage.type] =";\
    print "    Arbitrary(AlreadyMadeSalesPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AlreadyMadeSalesPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AlreadyMadeSales completed"
