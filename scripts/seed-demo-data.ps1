<#
  Adds 10 demo loan applications through the REST API, then logs in as a reviewer
  and moves the first 5 to UNDER_REVIEW. Result: 5 applications waiting to be picked up
  ("Start review") and 5 waiting for a decision ("Approve" / "Decline").
  All 10 pass the affordability check, so each one can be approved.

  The backend must be running (http://localhost:8080). Run with:
    powershell -ExecutionPolicy Bypass -File scripts\seed-demo-data.ps1
#>
param(
    [string]$ApiUrl = 'http://localhost:8080',
    [string]$Reviewer = 'thandi',
    [string]$Password = 'Reviewer#2026'
)

$ErrorActionPreference = 'Stop'

# title, givenName, surname, gender, street, suburb, province, postalCode, purpose, amount band, years, income band
$applicants = @(
    @('MS',   'Lerato',  'Dlamini',        'FEMALE', '5 Main Road',            'Rosebank',              'Gauteng',       '2196', 'PROPERTY',   'FROM_500K_TO_999K', 20, 'FROM_50K_TO_99K'),
    @('MR',   'Thabo',   'Nkosi',          'MALE',   '12 Jan Smuts Avenue',    'Braamfontein',          'Gauteng',       '2001', 'VEHICLE',    'FROM_250K_TO_499K', 5,  'FROM_50K_TO_99K'),
    @('MRS',  'Naledi',  'Mokoena',        'FEMALE', '8 Long Street',          'Cape Town City Centre', 'Western Cape',  '8001', 'INVESTMENT', 'LESS_THAN_250K',    10, 'LESS_THAN_50K'),
    @('MR',   'Kagiso',  'Molefe',         'MALE',   '44 Nelson Mandela Drive','Mahikeng',              'North West',    '2745', 'VEHICLE',    'LESS_THAN_250K',    5,  'LESS_THAN_50K'),
    @('MISS', 'Ayanda',  'Zulu',           'FEMALE', '17 Florida Road',        'Morningside',           'KwaZulu-Natal', '4001', 'PROPERTY',   'ONE_MILLION_PLUS',  30, 'FROM_50K_TO_99K'),
    @('MR',   'Sipho',   'Khumalo',        'MALE',   '3 Church Street',        'Polokwane',             'Limpopo',       '0699', 'VEHICLE',    'FROM_250K_TO_499K', 10, 'LESS_THAN_50K'),
    @('MS',   'Zanele',  'Mahlangu',       'FEMALE', '21 Samora Machel Street','Mbombela',              'Mpumalanga',    '1201', 'PROPERTY',   'FROM_500K_TO_999K', 10, 'FROM_100K_TO_149K'),
    @('MR',   'Pieter',  'van der Merwe',  'MALE',   '9 Voortrekker Road',     'Bellville',             'Western Cape',  '7530', 'INVESTMENT', 'ONE_MILLION_PLUS',  10, 'FROM_100K_TO_149K'),
    @('MRS',  'Nomvula', 'Sithole',        'FEMALE', '30 Nelson Mandela Drive','Bloemfontein',          'Free State',    '9301', 'VEHICLE',    'LESS_THAN_250K',    20, 'LESS_THAN_50K'),
    @('MR',   'Tshepo',  'Mabaso',         'MALE',   '6 Beach Road',           'Summerstrand',          'Eastern Cape',  '6001', 'PROPERTY',   'ONE_MILLION_PLUS',  5,  'FROM_150K_PLUS')
)

$reviewNotes = @('Payslips received', 'ID and bank statements verified', 'Employer confirmed income',
                 'Valuation report received', 'Credit check clear')

try {
    Write-Host "Adding 10 applications to $ApiUrl ..." -ForegroundColor Cyan
    $references = @()
    foreach ($a in $applicants) {
        $body = @{
            title = $a[0]; givenName = $a[1]; surname = $a[2]; gender = $a[3]
            address = @{ street = $a[4]; suburb = $a[5]; province = $a[6]; postalCode = $a[7] }
            loanPurpose = $a[8]; loanAmountBand = $a[9]; termYears = $a[10]; incomeBand = $a[11]
        } | ConvertTo-Json -Depth 3

        $result = Invoke-RestMethod -Method Post -Uri "$ApiUrl/api/applications" -ContentType 'application/json' -Body $body
        $references += $result.referenceNumber
        Write-Host ("  {0}  {1} {2,-14}  R{3,10:N2}/month  {4}" -f $result.referenceNumber, $a[1], $a[2],
                    $result.estimatedMonthlyRepayment, $result.affordabilityResult)
    }

    Write-Host "Logging in as $Reviewer ..." -ForegroundColor Cyan
    $login = Invoke-RestMethod -Method Post -Uri "$ApiUrl/api/auth/login" -ContentType 'application/json' `
        -Body (@{ username = $Reviewer; password = $Password } | ConvertTo-Json)
    $headers = @{ Authorization = "Bearer $($login.token)" }

    Write-Host 'Moving the first 5 to UNDER_REVIEW ...' -ForegroundColor Cyan
    for ($i = 0; $i -lt 5; $i++) {
        $change = @{ status = 'UNDER_REVIEW'; note = $reviewNotes[$i] } | ConvertTo-Json
        Invoke-RestMethod -Method Patch -Uri "$ApiUrl/api/applications/$($references[$i])/status" `
            -Headers $headers -ContentType 'application/json' -Body $change | Out-Null
        Write-Host "  $($references[$i])  -> UNDER_REVIEW ($($reviewNotes[$i]))"
    }

    Write-Host ''
    Write-Host 'Done: 5 applications are UNDER REVIEW (ready to Approve or Decline)' -ForegroundColor Green
    Write-Host '      and 5 are SUBMITTED (ready to Start review).' -ForegroundColor Green
    Write-Host 'Open http://localhost:4200 and click REFRESH.'
}
catch {
    Write-Host ''
    Write-Host "Something went wrong: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host 'Is the backend running? The "SNM backend" window should show "Started LoanServiceApplication".'
}
