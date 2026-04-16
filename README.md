# TSN-CBS-Project2

This project includes Jackson-based parsers for test cases under `src/main/resources/examples/test_case_*`.

## What is parsed

- `streams.json` -> `StreamsFile`
- `routes.json` -> `RoutesFile`
- `topology.json` -> `TopologyWrapper`

Use `JsonParsers.parseTestCaseFromResources("examples/test_case_1")` to load a full test case.

## Quick verify

```powershell
mvn test
```
