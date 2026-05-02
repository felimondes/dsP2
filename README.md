
How to run and edit the program:

### Compiling:

To compile the project, open the dsP2 directory in PowerShell and run the command (Only tested on Windows):
```powershell
mvn clean compile
```

### Analysis tool:

The test case to run is set on line 16 in the `App.java` file:
```powershell
TestCaseData testCaseData = jsonParser.parseTestCase("src/main/resources/examples/test_case_3");
```
To run the analysis tool, run the command:
```powershell
mvn exec:java "-Dexec.mainClass=com.TransactionApp.App"
```
The output will be printed in the console.

### Simulation tool:
The test case to run is set on line 26-28 in the `SimulationApp.java` file:
```powershell
TestCaseData testCase = parser.parseTestCase(
                "src/main/resources/examples/test_case_2"
        );
```
to run the simulation tool, run the command:
```powershell
mvn exec:java "-Dexec.mainClass=com.TransactionApp.SimulationTool.Simulator"
```
The output will be printed in the console.