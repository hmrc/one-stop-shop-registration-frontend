#!/bin/bash

echo ""
echo "Applying migration EmailVerificationCodesAndEmailsExceeded"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /emailVerificationCodesAndEmailsExceeded                       controllers.EmailVerificationCodesAndEmailsExceededController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "emailVerificationCodesAndEmailsExceeded.title = emailVerificationCodesAndEmailsExceeded" >> ../conf/messages.en
echo "emailVerificationCodesAndEmailsExceeded.heading = emailVerificationCodesAndEmailsExceeded" >> ../conf/messages.en

echo "Migration EmailVerificationCodesAndEmailsExceeded completed"
