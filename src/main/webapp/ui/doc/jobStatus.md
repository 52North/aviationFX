The Status API provides a read-only access to all jobs that are currently running or have been finished.

## Base Endpoint

`HTTP GET` ${baseUrl}/status

### Output:

```json
[
    {
        "id": "7d3fd4f9-d488-4989-8d82-af63186e5e48",
        "href": "${baseUrl}/status/7d3fd4f9-d488-4989-8d82-af63186e5e48",
        "created": "2016-05-24T10:31:21.628Z",
        "type": {
            "name": "delete",
            "subType": "bulk"
        },
        "status": "FINISHED"
    },
    {
        "id": "f26c8ba0-f2b3-4d2f-8463-d60229e88407",
        "href": "${baseUrl}/status/f26c8ba0-f2b3-4d2f-8463-d60229e88407",
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

`HTTP GET` ${baseUrl}/status/:theJob

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
