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

`HTTP GET` ${baseUrl}/export

### Output

```json
{  
    "bulk":{  
        "href":"${baseUrl}/export/bulk"
    },
    "byQuery":{  
        "href":"${baseUrl}/export/byQuery"
    }
}
```

## Bulk Execution

`HTTP POST` ${baseUrl}/export/bulk

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
    "href": "${baseUrl}/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```

## By Query Execution

`HTTP POST` ${baseUrl}/export/byQuery

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
    "href": "${baseUrl}/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```
