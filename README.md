# java11++ HttpClient processing chunked results

## Setup

Create an `.env` file:

```env
USERNAME=JohnDoe
PASSWORD=password
BASEURL=https://your.keep.server
QUERY_URL="/api/v1/lists/chunked?dataSource=demo&documents=true"
```

## Samples provided

- `SampleStream` processes lines using `BodyHandlers.ofLines()`
- `SampleStream2` processes lines using a custom handler `DocumentSubscriber`

## Caveats

- no TLS security at all
- no error handling
