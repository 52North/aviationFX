The Stylesheets API provides a read-only access to the available stylesheets for import and export.

## Base Endpoint

`HTTP GET` ${baseUrl}/stylesheets

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
