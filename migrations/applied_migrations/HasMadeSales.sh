#!/bin/bash

echo ""
echo "Applying migration HasMadeSales"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /hasMadeSales                        controllers.HasMadeSalesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /hasMadeSales                        controllers.HasMadeSalesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHasMadeSales                  controllers.HasMadeSalesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHasMadeSales                  controllers.HasMadeSalesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "hasMadeSales.title = hasMadeSales" >> ../conf/messages.en
echo "hasMadeSales.heading = hasMadeSales" >> ../conf/messages.en
echo "hasMadeSales.checkYourAnswersLabel = hasMadeSales" >> ../conf/messages.en
echo "hasMadeSales.error.required = Select yes if hasMadeSales" >> ../conf/messages.en
echo "hasMadeSales.change.hidden = HasMadeSales" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasMadeSalesUserAnswersEntry: Arbitrary[(HasMadeSalesPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HasMadeSalesPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasMadeSalesPage: Arbitrary[HasMadeSalesPage.type] =";\
    print "    Arbitrary(HasMadeSalesPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HasMadeSalesPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration HasMadeSales completed"
