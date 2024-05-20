# User API Spec

## Register User

Endpoint : POST /api/users

Request Body :

```json
{
  "username" : "khannedy",
  "password" : "rahasia",
  "name" : "Eko Kurniawan Khannedy" 
}
```

Response Body (Success) :

```json
{
  "data" : "OK"
}
```

Response Body (Failed) :

```json
{
  "errors" : "Username must not blank, ???"
}
```

## Login User

Endpoint : POST /api/auth/login

Request Body :

```json
{
  "username" : "khannedy",
  "password" : "rahasia" 
}
```

Response Body (Success) :

```json
{
  "data" : {
    "token" : "TOKEN",
    "expiredAt" : 2342342423423 // milliseconds
  }
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Username or password wrong"
}
```

## Get User

Endpoint : GET /api/users/current

Request Header :

- X-API-TOKEN : Token (Mandatory) 

Response Body (Success) :

```json
{
  "data" : {
    "username" : "khannedy",
    "name" : "Eko Kurniawan Khannedy"
  }
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Unauthorized"
}
```

## Update User

Endpoint : PATCH /api/users/current

Request Header :

- X-API-TOKEN : Token (Mandatory)

Request Body : 

```json
{
  "name" : "Eko Khannedy", // put if only want to update name
  "password" : "new password" // put if only want to update password
}
```

Response Body (Success) :

```json
{
  "data" : {
    "username" : "khannedy",
    "name" : "Eko Kurniawan Khannedy"
  }
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Unauthorized"
}
```

## Logout User

Endpoint : DELETE /api/auth/logout

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : "OK"
}
```


# Contact API Spec

## Create Contact

Endpoint : POST /api/contacts

Request Header :

- X-API-TOKEN : Token (Mandatory)

Request Body :

```json
{
  "firstName" : "Eko Kurniawan",
  "lastName" : "Khannedy",
  "email" : "eko@example.com",
  "phone" : "0899889998"
}
```

Response Body (Success) : 

```json
{
  "data": {
    "id" : "random-string",
    "firstName": "Eko Kurniawan",
    "lastName": "Khannedy",
    "email": "eko@example.com",
    "phone": "0899889998"
  }
}
```

Response Body (Failed) :

```json
{
  "errors" : "Email format invalid, phone formar invalid, ..."
}
```

## Update Contact

Endpoint : PUT /api/contacts/{idContact}

Request Header :

- X-API-TOKEN : Token (Mandatory)

Request Body :

```json
{
  "firstName" : "Eko Kurniawan",
  "lastName" : "Khannedy",
  "email" : "eko@example.com",
  "phone" : "0899889998"
}
```

Response Body (Success) :

```json
{
  "data": {
    "id" : "random-string",
    "firstName": "Eko Kurniawan",
    "lastName": "Khannedy",
    "email": "eko@example.com",
    "phone": "0899889998"
  }
}
```

Response Body (Failed) :

```json
{
  "errors" : "Email format invalid, phone formar invalid, ..."
}
```

## Get Contact

Endpoint : GET /api/contacts/{idContact}

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data": {
    "id" : "random-string",
    "firstName": "Eko Kurniawan",
    "lastName": "Khannedy",
    "email": "eko@example.com",
    "phone": "0899889998"
  }
}
```

Response Body (Failed, 404) :

```json
{
  "errors" : "Contact is not found"
}
```

## Search Contact

Endpoint : GET /api/contacts

Query Param :

- name : String, contact first name or last name, using like query, optional
- phone : String, contact phone, using like query, optional
- email : String, contact email, using like query, optional
- page : Integer, start from 0, default 0
- size : Integer, default 10

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data": [
    {
      "id": "random-string",
      "firstName": "Eko Kurniawan",
      "lastName": "Khannedy",
      "email": "eko@example.com",
      "phone": "0899889998"
    }
  ],
  "paging" : {
    "currentPage" : 0,
    "totalPage" : 10,
    "size" : 10
  }
}
```

Response Body (Failed) :

```json
{
  "errors" : "Unauthorized"
}
```

## Remove Contact

Endpoint : DELETE /api/contacts/{idContact}

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : "OK"
}
```

Response Body (Failed) :

```json
{
  "errors" : "Contact is not found"
}
```


# Address API Spec

## Create Address

Endpoint : POST /api/contacts/{idContact}/addresses

Request Header :

- X-API-TOKEN : Token (Mandatory)

Request Body :

```json
{
  "street" : "Jalan apa",
  "city" : "Kota",
  "province" : "provinsi",
  "country" : "Negara",
  "postalCode" : "12313"
}
```

Response Body (Success) :

```json
{
  "data" : {
    "id" : "randomstring",
    "street" : "Jalan apa",
    "city" : "Kota",
    "province" : "provinsi",
    "country" : "Negara",
    "postalCode" : "12313"
  }
}
```

Response Body (Failed) :

```json
{
  "errors" : "Contact is not found"
}
```

## Update Address

Endpoint : PUT /api/contacts/{idContact}/addresses/{idAddress}

Request Header :

- X-API-TOKEN : Token (Mandatory)

Request Body :

```json
{
  "street" : "Jalan apa",
  "city" : "Kota",
  "province" : "provinsi",
  "country" : "Negara",
  "postalCode" : "12313"
}
```

Response Body (Success) :

```json
{
  "data" : {
    "id" : "randomstring",
    "street" : "Jalan apa",
    "city" : "Kota",
    "province" : "provinsi",
    "country" : "Negara",
    "postalCode" : "12313"
  }
}
```

Response Body (Failed) :

```json
{
  "errors" : "Address is not found"
}
```

## Get Address

Endpoint : GET /api/contacts/{idContact}/addresses/{idAddress}

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : {
    "id" : "randomstring",
    "street" : "Jalan apa",
    "city" : "Kota",
    "province" : "provinsi",
    "country" : "Negara",
    "postalCode" : "12313"
  }
}
```

Response Body (Failed) :

```json
{
  "errors" : "Address is not found"
}
```

## Remove Address

Endpoint : DELETE /api/contacts/{idContact}/addresses/{idAddress}

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : "OK"
}
```

Response Body (Failed) :

```json
{
  "errors" : "Address is not found"
}
```

## List Address

Endpoint : GET /api/contacts/{idContact}/addresses

Request Header :

- X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data": [
    {
      "id": "randomstring",
      "street": "Jalan apa",
      "city": "Kota",
      "province": "provinsi",
      "country": "Negara",
      "postalCode": "12313"
    }
  ]
}
```

Response Body (Failed) :

```json
{
  "errors" : "Contact is not found"
}
```
