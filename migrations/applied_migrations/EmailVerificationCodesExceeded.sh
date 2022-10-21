#!/bin/bash

echo ""
echo "Applying migration EmailVerificationCodesExceeded"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /emailVerificationCodesExceeded                       controllers.EmailVerificationCodesExceededController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "emailVerificationCodesExceeded.title = emailVerificationCodesExceeded" >> ../conf/messages.en
echo "emailVerificationCodesExceeded.heading = emailVerificationCodesExceeded" >> ../conf/messages.en

echo "Migration EmailVerificationCodesExceeded completed"
