# api-tester

A small command-line tool that runs HTTP API tests described in a JSON file
and reports pass/fail results. Exits non-zero on failure, so it works in CI.

## Requirements
- Java 17+
- Maven 3.8+ (to build)

## Build
```bash
mvn clean package
```
Produces `target/api-tester.jar` (a single runnable jar).

## Usage
```bash
java -jar target/api-tester.jar --no-color my-api-tests.json  //write the api request that u wanna test in my-api-tests.json
java -jar target/api-tester.jar --help
```

### Exit codes
| Code | Meaning |
|---|---|
| 0 | All tests passed |
| 1 | At least one test failed or could not get a response |
| 2 | Bad usage or invalid test file (no requests were sent) |

## Test file format
```json
{
  "baseUrl": "https://jsonplaceholder.typicode.com",
  "tests": [
    {
      "name": "Get user 1",
      "request": { "method": "GET", "path": "/users/1" },
      "assertions": [
        { "type": "STATUS", "expected": 200 },
        { "type": "JSON_PATH", "path": "$.name", "expected": "Leanne Graham" },
        { "type": "MAX_TIME_MS", "expected": 1000 }
      ]
    }
  ]
}
```

| Field | Required | Notes |
|---|---|---|
| `baseUrl` | no | Prepended to `path` (not applied if `path` starts with `http`) |
| `request.path` | yes | |
| `request.method` | no | Default `GET` |
| `request.headers` | no | Key/value map |
| `request.body` | no | Object/array is sent as JSON |
| `assertions[].type` | yes | `STATUS`, `MAX_TIME_MS`, `JSON_PATH` |
| `assertions[].expected` | yes | Compared as text for `JSON_PATH` |
| `assertions[].path` | JSON_PATH only | JsonPath expression, e.g. `$.items[0].id` |

## Architecture
```
main -> CliHandler -> TestCaseRepository -> TestRunnerService -> ConsoleReporter
                                              |-> HttpExecutor
                                              |-> AssertionEvaluator(s)
```
- `repository/`: loads and validates test definitions (interface + JSON implementation)
- `service/`: orchestrates a run; turns failures into results
- `client/`: sends HTTP requests
- `assertion/`: one evaluator class per assertion type
- `report/`: formats output; `cli/`: parses arguments

### Adding a new assertion type
1. Add a value to `AssertionType`
2. Add validation for it in `JsonFileTestCaseRepository.parseAssertion`
3. Create an `AssertionEvaluator` implementation
4. Register it in `ApiTesterApplication.run`

## Run the tests
```bash
mvn test
```

## Known limitations
- No variables or request chaining
- Tests run sequentially; 10 second timeout per request
- `JSON_PATH` compares scalar values as text only
