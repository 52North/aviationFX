# EUMETSAT Import/Export Component API

This document describes the HTTP API for importing, exporting and deleting
metadata entries within the EUMETSAT Product Navigator.

Version: *0.2*

Date: *2016-06-07*

Authors:

* Matthes Rieke (52°North)
* Uwe Voges (con terra)
* Udo Einspanier (con terra)

# Importing

Import jobs run asynchronously. After submitting an Import job via HTTP POST,
the job status can be requested via the Job Status API (see section Job Status).

## Base Endpoint

`HTTP GET` http://rs211980.rs.hosteurope.de/iec/import

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

`HTTP POST` http://rs211980.rs.hosteurope.de/iec/import


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
    "href": "http://rs211980.rs.hosteurope.de/iec/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```


# Exporting

Export jobs run asynchronously. After submitting an Export job via HTTP POST,
the job status can be requested via the Job Status API (see section Job Status).

The `dryRun` flag indicates that no actual export should take place. Instead the
count of affected metadata entries is returned in the result property of the job
response. Also, in contrast to normal execution, `dryRun` acts synchronously and
returns a JSON job response directly and no `email` parameter is required. An
example response for `dryRun` looks like the following:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "created": "2016-05-24T10:31:21.628Z",
    "type": {
        "name": "export",
        "subType": "bulk"
    },
    "parameters": {
        "dryRun": {
            "value": "true"
        }
    },
    "status": "FINISHED",
    "result":{  
        "date":"2016-05-24T10:31:21.728Z",
        "affectedCount": 607
    },
    "messages": [
        {
            "level": "INFO",
            "text": "Job scheduled for execution at 2016-05-24T12:31:21.628+02:00",
            "date": "2016-05-24T10:31:21.628Z"
        },
        {
            "level": "INFO",
            "text": "Finished execution at 2016-05-24T12:31:21.728+02:00",
            "date": "2016-05-24T10:31:21.728Z"
        }
    ]
}
```

## Base Endpoint

`HTTP GET` http://rs211980.rs.hosteurope.de/iec/export

### Output

```json
{  
    "bulk":{  
        "href":"http://rs211980.rs.hosteurope.de/iec/export/bulk"
    },
    "byQuery":{  
        "href":"http://rs211980.rs.hosteurope.de/iec/export/byQuery"
    }
}
```

## Bulk Execution

`HTTP POST` http://rs211980.rs.hosteurope.de/iec/export/bulk

### JSON Parameters:

* `email` (string): the status email (required)
* `stylesheet` (string): the stylesheet to be applied before export (optional)
* `dryRun` (boolean): do a dry-run - no actual export (optional)

### Input:

```json
{
    "parameters": {
        "email": "test@test.de",
        "stylesheet": "stylesheet1.xslt",
        "dryRun": true
    }
}
```

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "status": "ACCEPTED",
    "href": "http://rs211980.rs.hosteurope.de/iec/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```

## By Query Execution

`HTTP POST` http://rs211980.rs.hosteurope.de/iec/export/byQuery

### JSON Parameters:

* `email` (string): the status email (required)
* `query` (string): the Lucene query (required)
* `stylesheet` (string): the stylesheet to be applied before export (optional)
* `dryRun` (boolean): do a dry-run - no actual export (optional)

### Input:

```json
{
    "parameters": {
        "email": "test@test.de",
        "query": "title=\"theTitle\"",
        "stylesheet": "stylesheet1.xslt"
    }
}
```

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "status": "ACCEPTED",
    "href": "http://rs211980.rs.hosteurope.de/iec/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```


# Deleting

Delete jobs run asynchronously. After submitting a Delete job via HTTP POST,
the job status can be requested via the Job Status API (see section Job Status).
The `delete ById` request is an exception, it synchronously returns the result.

The `dryRun` flag indicates that no actual deletion should take place. Instead the
count of affected metadata entries is returned in the result property of the job
response. Also, in contrast to normal execution, `dryRun` acts synchronously and
returns a JSON job response directly and no `email` parameter is required. An
example response for `dryRun` looks like the following:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "created": "2016-05-24T10:31:21.628Z",
    "type": {
        "name": "delete",
        "subType": "bulk"
    },
    "parameters": {
        "dryRun": {
            "value": "true"
        }
    },
    "status": "FINISHED",
    "result":{  
        "date":"2016-05-24T10:31:21.728Z",
        "affectedCount": 607
    },
    "messages": [
        {
            "level": "INFO",
            "text": "Job scheduled for execution at 2016-05-24T12:31:21.628+02:00",
            "date": "2016-05-24T10:31:21.628Z"
        },
        {
            "level": "INFO",
            "text": "Finished execution at 2016-05-24T12:31:21.728+02:00",
            "date": "2016-05-24T10:31:21.728Z"
        }
    ]
}
```

## Base Endpoint

`HTTP GET` http://rs211980.rs.hosteurope.de/iec/delete

### Output

```json
{  
    "bulk":{  
        "href":"http://rs211980.rs.hosteurope.de/iec/delete/bulk"
    },
    "byId":{  
        "href":"http://rs211980.rs.hosteurope.de/iec/delete/byId"
    },
    "byQuery":{  
        "href":"http://rs211980.rs.hosteurope.de/iec/delete/byQuery"
    }
}
```

## Bulk Execution

`HTTP POST` http://rs211980.rs.hosteurope.de/iec/delete/bulk

### JSON Parameters:

* `email` (string): the status email (required)
* `dryRun` (boolean): do a dry-run - no actual delete (optional)

### Input:

```json
{
    "parameters": {
        "email": "test@test.de",
        "dryRun": true
    }
}
```

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "status": "ACCEPTED",
    "href": "http://rs211980.rs.hosteurope.de/iec/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```


## By ID Execution

`HTTP POST` http://rs211980.rs.hosteurope.de/iec/delete/byId

Delete By ID runs synchronously. The response is provided directly, thus
the status has not and cannot be requested after execution.

### JSON Parameters:

* `id` (string): the metadata entry id (required)

### Input: -

```json
{
    "parameters": {
        "id": "EUM:TEST:ID:1"
    }
}
```

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "created": "2016-05-24T10:31:21.628Z",
    "type": {
        "name": "delete",
        "subType": "byId"
    },
    "parameters": {
        "id": {
            "value": "EUM:TEST:ID:1"
        }
    },
    "status": "FINISHED",
    "messages": [
        {
            "level": "INFO",
            "text": "Job schedules for execution at 2016-05-24T12:31:21.628+02:00",
            "date": "2016-05-24T10:31:21.628Z"
        },
        {
            "level": "INFO",
            "text": "Deleted record with ID 'EUM:TEST:ID:1'",
            "date": "2016-05-24T10:31:21.728Z"
        },
        {
            "level": "INFO",
            "text": "Finished execution at 2016-05-24T12:31:21.828+02:00",
            "date": "2016-05-24T10:31:21.828Z"
        }
    ]
}
```

## By Query Execution

`HTTP POST` http://rs211980.rs.hosteurope.de/iec/delete/byQuery

### JSON Parameters:

* `email` (string): the status email (required)
* `query` (string): the Lucene query (required)
* `dryRun` (boolean): do a dry-run - no actual delete (optional)

### Input:

```json
{
    "parameters": {
        "email": "test@test.de",
        "query": "title=\"theTitle\"",
        "dryRun": true
    }
}
```

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "status": "ACCEPTED",
    "href": "http://rs211980.rs.hosteurope.de/iec/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```


# Stylesheets

The Stylesheets API provides a read-only access to the available stylesheets for import and export.

## Base Endpoint

`HTTP GET` http://rs211980.rs.hosteurope.de/iec/stylesheets

### Output:

```json
[
    {
        "id": "test-style.xslt",
        "label": "XSLT test-style.xslt",
        "content": "<?xml version=\"1.0\"?>\n<xsl:stylesheet />"
    }
]
```


# Job Status

The Status API provides a read-only access to all jobs that are currently running or have been finished.

## Base Endpoint

`HTTP GET` http://rs211980.rs.hosteurope.de/iec/status

### Output:

```json
[
    {
        "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
        "href": "http://rs211980.rs.hosteurope.de/iec/status/7d3fd4f9-d488-4989-8d82-af63186e5e48",
        "created": "2016-05-24T10:31:21.628Z",
        "type": {
            "name": "delete",
            "subType": "bulk"
        },
        "status": "FINISHED"
    },
    {
        "id": "f26c8ba0-f2b3-4d2f-8463-d60229e88407",
        "href": "http://rs211980.rs.hosteurope.de/iec/status/f26c8ba0-f2b3-4d2f-8463-d60229e88407",
        "created": "2016-05-24T10:16:33.902Z",
        "type": {
            "name": "import",
            "subType": "xml"
        },
        "status": "ERROR"
    }
]
```

## Single Job Status

`HTTP GET` http://rs211980.rs.hosteurope.de/iec/status/:theJob

Provides read-only access to details of a singular job execution.

### Output:

```json
{
    "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
    "created": "2016-05-24T10:31:21.628Z",
    "type": {
        "name": "delete",
        "subType": "bulk"
    },
    "parameters": {
        "dryRun": {
            "value": "true"
        },
        "email": {
            "value": "test@test.de"
        }
    },
    "status": "FINISHED",
    "messages": [
        {
            "level": "INFO",
            "text": "Job schedules for execution at 2016-05-24T12:31:21.628+02:00",
            "date": "2016-05-24T10:31:21.628Z"
        },
        {
            "level": "INFO",
            "text": "Finished execution at 2016-05-24T12:31:21.628+02:00",
            "date": "2016-05-24T10:31:21.628Z"
        }
    ]
}
```
