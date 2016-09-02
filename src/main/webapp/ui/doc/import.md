Import jobs run asynchronously. After submitting an Import job via HTTP POST,
the job status can be requested via the Job Status API (see section Job Status).

## Base Endpoint

`HTTP GET` ${baseUrl}/import

### Output

```json
{  
    "supportedPayloads":[  
        "application/xml",
        "application/zip"
    ],
    "parameters":{  
        "email":{  
            "type":"email",
            "label":"Status report email",
            "order":0,
            "required":true
        },
        "stylesheet":{  
            "type":"text",
            "label":"Stylesheet ID for applying prior to export",
            "order":1,
            "required":false
        }
    }
}
```

## Execution

`HTTP POST` ${baseUrl}/import


### JSON Parameters:

* `email` (string): the status email (required)
* `stylesheet` (string): the stylesheet to be applied before import (optional)


### Input:

The input POST content shall be provided as `multipart/form-data`, containing
two separate dispositions, one for the `File` and one for the JSON parameters:

```
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryePkpFF7tjBAqx29L

------WebKitFormBoundaryePkpFF7tjBAqx29L
Content-Disposition: form-data; name="file"; filename="metadata.zip"
Content-Type: application/zip


100000
------WebKitFormBoundaryePkpFF7tjBAqx29L
Content-Disposition: form-data; name="parameters"


{"email":"test@test.de", "stylesheet":"stylesheet1.xslt"}
------WebKitFormBoundaryePkpFF7tjBAqx29L--
```

The API supports importing of single XML or ZIP files (containing multiple XML files).
An XML file would be represented with the following disposition:

```
------WebKitFormBoundaryePkpFF7tjBAqx29L
Content-Disposition: form-data; name="file"; filename="metadata-entry.xml"
Content-Type: application/xml
```

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "status": "ACCEPTED",
    "href": "${baseUrl}/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```
