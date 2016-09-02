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

`HTTP GET` ${baseUrl}/delete

### Output

```json
{  
    "bulk":{  
        "href":"${baseUrl}/delete/bulk"
    },
    "byId":{  
        "href":"${baseUrl}/delete/byId"
    },
    "byQuery":{  
        "href":"${baseUrl}/delete/byQuery"
    }
}
```

## Bulk Execution

`HTTP POST` ${baseUrl}/delete/bulk

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
    "href": "${baseUrl}/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```


## By ID Execution

`HTTP POST` ${baseUrl}/delete/byId

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

`HTTP POST` ${baseUrl}/delete/byQuery

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
    "href": "${baseUrl}/status/7d3fd4f9-d488-4989-8d82-af63186e5e48"
}
```
