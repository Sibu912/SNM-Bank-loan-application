# API – SNM Bank Loan Service

**Base URL (local):** `http://localhost:8080`

The service has two interfaces that call the same business logic:

- **REST / JSON** for the loan form and the Angular admin
- **SOAP / XML** for partner systems

The codes used in requests (for example `FROM_250K_TO_499K`) are listed in [DESIGN.md §5.1](DESIGN.md#51-codes-and-how-they-map-to-the-form).

---

## REST endpoints

| # | Method | Path | Success |
|---|--------|------|---------|
| 1 | `POST` | `/api/applications` | `201 Created` |
| 2 | `GET` | `/api/applications` | `200 OK` |
| 3 | `GET` | `/api/applications/{referenceNumber}` | `200 OK` |
| 4 | `PATCH` | `/api/applications/{referenceNumber}/status` | `200 OK` |
| 5 | `GET` | `/api/applications/{referenceNumber}/history` | `200 OK` |

### 1. Submit an application

`POST /api/applications`

**Request**

```json
{
  "title": "MR",
  "givenName": "Thabo",
  "surname": "Nkosi",
  "gender": "MALE",
  "address": {
    "street": "12 Jan Smuts Avenue",
    "suburb": "Braamfontein",
    "province": "Gauteng",
    "postalCode": "2001"
  },
  "loanPurpose": "VEHICLE",
  "loanAmountBand": "FROM_250K_TO_499K",
  "termYears": 5,
  "incomeBand": "FROM_50K_TO_99K"
}
```

**Response** `201 Created`, with the header `Location: /api/applications/SNM-2026-000001`

```json
{
  "referenceNumber": "SNM-2026-000001",
  "status": "SUBMITTED",
  "affordabilityResult": "PASS",
  "estimatedMonthlyRepayment": 10996.30,
  "createdAt": "2026-11-25T18:30:00"
}
```

**Errors:** `400` if validation fails (see [Error format](#error-format)).

### 2. List applications

`GET /api/applications?status=SUBMITTED&page=0&size=20`

| Query parameter | Required | Default | Notes |
|-----------------|----------|---------|-------|
| `status` | No | all | `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `DECLINED` |
| `page` | No | `0` | Starts at 0 |
| `size` | No | `20` | Maximum 100 |

Newest applications come first.

**Response** `200 OK`

```json
{
  "content": [
    {
      "referenceNumber": "SNM-2026-000001",
      "applicantName": "Mr Thabo Nkosi",
      "loanPurpose": "VEHICLE",
      "loanAmountBand": "FROM_250K_TO_499K",
      "status": "SUBMITTED",
      "affordabilityResult": "PASS",
      "createdAt": "2026-11-25T18:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

> This uses a small `PageResponse<T>` record instead of returning Spring's `Page` object directly, so the JSON shape stays stable.

### 3. Get one application

`GET /api/applications/SNM-2026-000001`

**Response** `200 OK`

```json
{
  "referenceNumber": "SNM-2026-000001",
  "applicant": {
    "title": "MR",
    "givenName": "Thabo",
    "surname": "Nkosi",
    "gender": "MALE",
    "address": {
      "street": "12 Jan Smuts Avenue",
      "suburb": "Braamfontein",
      "province": "Gauteng",
      "postalCode": "2001"
    }
  },
  "loanPurpose": "VEHICLE",
  "loanAmountBand": "FROM_250K_TO_499K",
  "termYears": 5,
  "incomeBand": "FROM_50K_TO_99K",
  "estimatedMonthlyRepayment": 10996.30,
  "affordabilityResult": "PASS",
  "status": "SUBMITTED",
  "channel": "WEB",
  "createdAt": "2026-11-25T18:30:00",
  "updatedAt": "2026-11-25T18:30:00"
}
```

**Errors:** `404` if the reference number doesn't exist.

### 4. Change status

`PATCH /api/applications/SNM-2026-000001/status`

**Request**

```json
{
  "status": "UNDER_REVIEW",
  "note": "Payslips received",
  "changedBy": "reviewer.thandi"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `status` | Yes | Must be an allowed next status ([DESIGN.md §6.3](DESIGN.md#63-status-flow)) |
| `note` | No | Up to 255 characters |
| `changedBy` | Yes | 1–50 characters |

**Response** `200 OK`: the updated application, in the same shape as endpoint 3.

**Errors:** `400` validation · `404` not found · `409` status change not allowed

### 5. Status history

`GET /api/applications/SNM-2026-000001/history`

**Response** `200 OK`, oldest change first

```json
[
  {
    "fromStatus": null,
    "toStatus": "SUBMITTED",
    "note": null,
    "changedBy": "system",
    "changedAt": "2026-11-25T18:30:00"
  },
  {
    "fromStatus": "SUBMITTED",
    "toStatus": "UNDER_REVIEW",
    "note": "Payslips received",
    "changedBy": "reviewer.thandi",
    "changedAt": "2026-11-26T09:15:00"
  }
]
```

### Error format

All REST errors use Spring's `ProblemDetail` (RFC 9457).

**400: validation failed.** Each key in `errors` is the field's path in the request body. An unknown code (for example `"title": "DR"`) or broken JSON also returns 400, without the `errors` map.

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/applications",
  "errors": {
    "address.postalCode": "must be exactly 4 digits",
    "givenName": "must not be blank"
  }
}
```

**404: not found**

```json
{
  "type": "about:blank",
  "title": "Application not found",
  "status": 404,
  "detail": "Application SNM-2026-999999 not found",
  "instance": "/api/applications/SNM-2026-999999"
}
```

**409: status change not allowed**

```json
{
  "type": "about:blank",
  "title": "Invalid status change",
  "status": 409,
  "detail": "Cannot change status from SUBMITTED to APPROVED",
  "instance": "/api/applications/SNM-2026-000001/status"
}
```

### CORS

Browsers only let the form and the Angular app call the API if the backend allows their origin. Allowed origins are a setting:

```yaml
snm:
  cors:
    allowed-origins: http://localhost:4200,http://localhost:5500,http://127.0.0.1:5500
```

`4200` is the Angular dev server. `5500` is the VS Code Live Server extension, for the form.

---

## SOAP

| Item | Value |
|------|-------|
| Endpoint | `http://localhost:8080/ws` |
| WSDL | `http://localhost:8080/ws/loans.wsdl` |
| Namespace | `https://sibu912.github.io/snm-bank/loans/v1` |
| Style | Document/literal, contract-first |

| Operation | Request element | Response element |
|-----------|-----------------|------------------|
| SubmitLoanApplication | `SubmitLoanApplicationRequest` | `SubmitLoanApplicationResponse` |
| GetApplicationStatus | `GetApplicationStatusRequest` | `GetApplicationStatusResponse` |

How it's wired in Spring-WS:

- `MessageDispatcherServlet` is mapped to `/ws/*`
- `DefaultWsdl11Definition` builds the WSDL from the XSD. Elements ending in `Request` and `Response` become operations.
- A `PayloadValidatingInterceptor` checks every request **and** response against the XSD
- `@Endpoint` + `@PayloadRoot(namespace = ..., localPart = "SubmitLoanApplicationRequest")` sends each request to the right method
- The `jaxb2-maven-plugin` generates the Java classes from the XSD into `io.github.sibu912.snmbank.loans.soap.generated` during the build. They're never edited by hand.

### Example: submit

**Request**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
    <soapenv:Header/>
    <soapenv:Body>
        <loan:SubmitLoanApplicationRequest xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
            <loan:applicant>
                <loan:title>MR</loan:title>
                <loan:givenName>Thabo</loan:givenName>
                <loan:surname>Nkosi</loan:surname>
                <loan:gender>MALE</loan:gender>
                <loan:address>
                    <loan:street>12 Jan Smuts Avenue</loan:street>
                    <loan:suburb>Braamfontein</loan:suburb>
                    <loan:province>Gauteng</loan:province>
                    <loan:postalCode>2001</loan:postalCode>
                </loan:address>
            </loan:applicant>
            <loan:loan>
                <loan:purpose>VEHICLE</loan:purpose>
                <loan:amountBand>FROM_250K_TO_499K</loan:amountBand>
                <loan:termYears>5</loan:termYears>
                <loan:incomeBand>FROM_50K_TO_99K</loan:incomeBand>
            </loan:loan>
        </loan:SubmitLoanApplicationRequest>
    </soapenv:Body>
</soapenv:Envelope>
```

**Response**

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
        <loan:SubmitLoanApplicationResponse xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
            <loan:referenceNumber>SNM-2026-000001</loan:referenceNumber>
            <loan:status>SUBMITTED</loan:status>
            <loan:affordabilityResult>PASS</loan:affordabilityResult>
            <loan:estimatedMonthlyRepayment>10996.30</loan:estimatedMonthlyRepayment>
        </loan:SubmitLoanApplicationResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### Example: check status

**Request**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
    <soapenv:Header/>
    <soapenv:Body>
        <loan:GetApplicationStatusRequest>
            <loan:referenceNumber>SNM-2026-000001</loan:referenceNumber>
        </loan:GetApplicationStatusRequest>
    </soapenv:Body>
</soapenv:Envelope>
```

**Response**

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
        <loan:GetApplicationStatusResponse xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
            <loan:referenceNumber>SNM-2026-000001</loan:referenceNumber>
            <loan:status>UNDER_REVIEW</loan:status>
            <loan:lastUpdated>2026-11-26T09:15:00+02:00</loan:lastUpdated>
        </loan:GetApplicationStatusResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### Example: fault (reference number not found)

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
        <SOAP-ENV:Fault>
            <faultcode>SOAP-ENV:Client</faultcode>
            <faultstring xml:lang="en">Application SNM-2026-999999 not found</faultstring>
        </SOAP-ENV:Fault>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

This comes from an `ApplicationNotFoundException` annotated with `@SoapFault(faultCode = FaultCode.CLIENT)`.

### SOAP contract (XSD)

Saved as `backend/src/main/resources/xsd/loan-application.xsd`. The examples above have been validated against it.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  SNM Bank Loan Service - SOAP contract (v1)
  Contract-first: this file is written first, and the Java classes are generated from it.
-->
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:tns="https://sibu912.github.io/snm-bank/loans/v1"
           targetNamespace="https://sibu912.github.io/snm-bank/loans/v1"
           elementFormDefault="qualified">

    <!-- ===== Operation 1: submit a loan application ===== -->
    <xs:element name="SubmitLoanApplicationRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="applicant" type="tns:Applicant"/>
                <xs:element name="loan" type="tns:LoanDetails"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="SubmitLoanApplicationResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="referenceNumber" type="tns:ReferenceNumber"/>
                <xs:element name="status" type="tns:ApplicationStatus"/>
                <xs:element name="affordabilityResult" type="tns:AffordabilityResult"/>
                <xs:element name="estimatedMonthlyRepayment" type="tns:Money"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- ===== Operation 2: check the status of an application ===== -->
    <xs:element name="GetApplicationStatusRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="referenceNumber" type="tns:ReferenceNumber"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="GetApplicationStatusResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="referenceNumber" type="tns:ReferenceNumber"/>
                <xs:element name="status" type="tns:ApplicationStatus"/>
                <xs:element name="lastUpdated" type="xs:dateTime"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- ===== Complex types ===== -->
    <xs:complexType name="Applicant">
        <xs:sequence>
            <xs:element name="title" type="tns:Title"/>
            <xs:element name="givenName" type="tns:Name"/>
            <xs:element name="surname" type="tns:Name"/>
            <xs:element name="gender" type="tns:Gender"/>
            <xs:element name="address" type="tns:Address"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="Address">
        <xs:sequence>
            <xs:element name="street" type="tns:Text150"/>
            <xs:element name="suburb" type="tns:Name"/>
            <xs:element name="province" type="tns:Province"/>
            <xs:element name="postalCode" type="tns:PostalCode"/>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="LoanDetails">
        <xs:sequence>
            <xs:element name="purpose" type="tns:LoanPurpose"/>
            <xs:element name="amountBand" type="tns:LoanAmountBand"/>
            <xs:element name="termYears" type="tns:TermYears"/>
            <xs:element name="incomeBand" type="tns:IncomeBand"/>
        </xs:sequence>
    </xs:complexType>

    <!-- ===== Simple types (validation rules live here) ===== -->
    <xs:simpleType name="Name">
        <xs:restriction base="xs:string">
            <xs:minLength value="1"/>
            <xs:maxLength value="100"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="Text150">
        <xs:restriction base="xs:string">
            <xs:minLength value="1"/>
            <xs:maxLength value="150"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="Province">
        <xs:restriction base="xs:string">
            <xs:minLength value="1"/>
            <xs:maxLength value="50"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="PostalCode">
        <xs:restriction base="xs:string">
            <xs:pattern value="[0-9]{4}"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="ReferenceNumber">
        <xs:restriction base="xs:string">
            <xs:pattern value="SNM-[0-9]{4}-[0-9]{6}"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="Money">
        <xs:restriction base="xs:decimal">
            <xs:fractionDigits value="2"/>
            <xs:minInclusive value="0"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="TermYears">
        <xs:restriction base="xs:int">
            <xs:enumeration value="5"/>
            <xs:enumeration value="10"/>
            <xs:enumeration value="15"/>
            <xs:enumeration value="20"/>
            <xs:enumeration value="25"/>
            <xs:enumeration value="30"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="Title">
        <xs:restriction base="xs:string">
            <xs:enumeration value="MR"/>
            <xs:enumeration value="MRS"/>
            <xs:enumeration value="MISS"/>
            <xs:enumeration value="MS"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="Gender">
        <xs:restriction base="xs:string">
            <xs:enumeration value="MALE"/>
            <xs:enumeration value="FEMALE"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="LoanPurpose">
        <xs:restriction base="xs:string">
            <xs:enumeration value="VEHICLE"/>
            <xs:enumeration value="PROPERTY"/>
            <xs:enumeration value="INVESTMENT"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="LoanAmountBand">
        <xs:restriction base="xs:string">
            <xs:enumeration value="LESS_THAN_250K"/>
            <xs:enumeration value="FROM_250K_TO_499K"/>
            <xs:enumeration value="FROM_500K_TO_999K"/>
            <xs:enumeration value="ONE_MILLION_PLUS"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="IncomeBand">
        <xs:restriction base="xs:string">
            <xs:enumeration value="LESS_THAN_50K"/>
            <xs:enumeration value="FROM_50K_TO_99K"/>
            <xs:enumeration value="FROM_100K_TO_149K"/>
            <xs:enumeration value="FROM_150K_PLUS"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="ApplicationStatus">
        <xs:restriction base="xs:string">
            <xs:enumeration value="SUBMITTED"/>
            <xs:enumeration value="UNDER_REVIEW"/>
            <xs:enumeration value="APPROVED"/>
            <xs:enumeration value="DECLINED"/>
        </xs:restriction>
    </xs:simpleType>

    <xs:simpleType name="AffordabilityResult">
        <xs:restriction base="xs:string">
            <xs:enumeration value="PASS"/>
            <xs:enumeration value="FAIL"/>
        </xs:restriction>
    </xs:simpleType>

</xs:schema>
```
