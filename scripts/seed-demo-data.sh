#!/bin/sh
# Adds 10 demo loan applications through the REST API, then logs in as a reviewer and
# moves the first 5 to UNDER_REVIEW. Same as seed-demo-data.ps1, for macOS/Linux and Docker.
#
#   sh scripts/seed-demo-data.sh                      # backend on http://localhost:8080
#   sh scripts/seed-demo-data.sh http://backend:8080  # used by docker-compose (profile "demo")
set -e
API="${1:-http://localhost:8080}"

echo "Waiting for the loan service at $API ..."
i=0
until curl -s -o /dev/null -X POST "$API/api/auth/login" -H 'Content-Type: application/json' -d '{}'; do
  i=$((i + 1)); [ "$i" -gt 90 ] && echo "The loan service did not start." && exit 1
  sleep 2
done

# title|givenName|surname|gender|street|suburb|province|postalCode|purpose|amount|years|income
cat > /tmp/applicants.txt <<'ROWS'
MS|Lerato|Dlamini|FEMALE|5 Main Road|Rosebank|Gauteng|2196|PROPERTY|FROM_500K_TO_999K|20|FROM_50K_TO_99K
MR|Thabo|Nkosi|MALE|12 Jan Smuts Avenue|Braamfontein|Gauteng|2001|VEHICLE|FROM_250K_TO_499K|5|FROM_50K_TO_99K
MRS|Naledi|Mokoena|FEMALE|8 Long Street|Cape Town City Centre|Western Cape|8001|INVESTMENT|LESS_THAN_250K|10|LESS_THAN_50K
MR|Kagiso|Molefe|MALE|44 Nelson Mandela Drive|Mahikeng|North West|2745|VEHICLE|LESS_THAN_250K|5|LESS_THAN_50K
MISS|Ayanda|Zulu|FEMALE|17 Florida Road|Morningside|KwaZulu-Natal|4001|PROPERTY|ONE_MILLION_PLUS|30|FROM_50K_TO_99K
MR|Sipho|Khumalo|MALE|3 Church Street|Polokwane|Limpopo|0699|VEHICLE|FROM_250K_TO_499K|10|LESS_THAN_50K
MS|Zanele|Mahlangu|FEMALE|21 Samora Machel Street|Mbombela|Mpumalanga|1201|PROPERTY|FROM_500K_TO_999K|10|FROM_100K_TO_149K
MR|Pieter|van der Merwe|MALE|9 Voortrekker Road|Bellville|Western Cape|7530|INVESTMENT|ONE_MILLION_PLUS|10|FROM_100K_TO_149K
MRS|Nomvula|Sithole|FEMALE|30 Nelson Mandela Drive|Bloemfontein|Free State|9301|VEHICLE|LESS_THAN_250K|20|LESS_THAN_50K
MR|Tshepo|Mabaso|MALE|6 Beach Road|Summerstrand|Eastern Cape|6001|PROPERTY|ONE_MILLION_PLUS|5|FROM_150K_PLUS
ROWS

echo "Adding 10 applications ..."
REFS=""
while IFS='|' read -r t g s gen st sub prov pc pur amt yrs inc; do
  body=$(printf '{"title":"%s","givenName":"%s","surname":"%s","gender":"%s","address":{"street":"%s","suburb":"%s","province":"%s","postalCode":"%s"},"loanPurpose":"%s","loanAmountBand":"%s","termYears":%s,"incomeBand":"%s"}' \
    "$t" "$g" "$s" "$gen" "$st" "$sub" "$prov" "$pc" "$pur" "$amt" "$yrs" "$inc")
  ref=$(curl -sf -X POST "$API/api/applications" -H 'Content-Type: application/json' -d "$body" \
        | sed -n 's/.*"referenceNumber":"\([^"]*\)".*/\1/p')
  echo "  $ref  $g $s"
  REFS="$REFS $ref"
done < /tmp/applicants.txt

TOKEN=$(curl -sf -X POST "$API/api/auth/login" -H 'Content-Type: application/json' \
        -d '{"username":"thandi","password":"Reviewer#2026"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

echo "Moving the first 5 to UNDER_REVIEW ..."
n=0
for ref in $REFS; do
  n=$((n + 1)); [ "$n" -gt 5 ] && break
  curl -sf -o /dev/null -X PATCH "$API/api/applications/$ref/status" \
    -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
    -d '{"status":"UNDER_REVIEW","note":"Documents received"}'
  echo "  $ref -> UNDER_REVIEW"
done
echo "Done: 5 applications under review and 5 submitted."
