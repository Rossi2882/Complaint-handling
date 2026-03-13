#  Complaint Handling System - Camunda 8 Orchestration

This project is a fully functional, distributed business process orchestration system (BPMN) based on the **Camunda 8 (Zeebe)** engine and the **Spring Boot** framework. 

The system automates the complaint handling process for a retail store (e.g., a shoe store) by making automated decisions using a business rule engine (DMN), delegating manual tasks (User Tasks), and integrating with external services via Java workers (Service Tasks).

## Key Features and BPMN Patterns Used

The project utilizes advanced BPMN 2.0 modeling mechanisms:
* **Business Decisions (DMN):** Automatic evaluation of the claim based on the purchase date and defect type (e.g., "factory defect").
* **Timer Events:** Escalation mechanism (the "Lazy Employee" scenario) – if an employee does not make a decision within a specific timeframe, the system automatically accepts the complaint using a context merge operation in the FEEL language (`decyzja + {...}`).
* **Inter-process Communication (Message Events):** Message correlation based on the `NrZamowienia` (Order Number) key between the main complaint process and an independent appeal process.
* **Race Condition Pattern (Signals):** A race mechanism implemented in the appeal process. A task is routed in parallel to two managers (Store Manager and Regional Manager). A decision made by one of them triggers an interrupt signal (`stop-ocena`), which immediately cancels the other manager's pending task.
* **Business Error Handling (Error Boundary Events):** Remediation loops for invalid data validation (e.g., an IBAN that is too short) caught from a subprocess (Call Activity).
* **Subprocesses (Call Activity):** Extracting the refund logic into a separate, reusable `Zwrot_pieniedzy.bpmn` process.

##  Project Structure

### BPMN / DMN Diagrams
*  `reklamacja.bpmn` - The main orchestrating process, spanning from the initial claim submission to the final decision.
*  `Odwolanie.bpmn` - The process handling customer escalations and appeals.
*  `Zwrot_pieniedzy.bpmn` - The invoked subprocess that validates the bank account and processes the refund.
*  `Ocena_reklamacji.dmn` - The decision table determining the initial status: *Przyjęta* (Accepted), *Odrzucona* (Rejected), or *Rozpatrzenie* (Under Review).

### Camunda Forms
Integrated user forms that pass data in JSON format to the process engine. These include:
* `ZlozenieReklamacji.form`
* `DecyzjaSprzedawcy.form`
* `ZmianaNrKarty.form`

### Java Module (Spring Boot Workers)
The `CentralnyWorker.java` class contains logic simulating integration with external systems using the External Task architecture:
* `weryfikacja-zamowienia`: Verifies the order number in a simulated database. Numbers `< 1000` are accepted. Numbers `>= 1000` throw a handled BPMN Error (`ORDER_NOT_FOUND`), which the process catches and routes to a fallback path.
* `wyslij-powiadomienie`: Simulates sending an email notification to the customer.

##  Technologies
* **Camunda 8 (Zeebe)** (v8.8.x) - Orchestration engine
* **Camunda Modeler** - Modeling tool
* **Java 21**
* **Spring Boot** (Camunda Spring Boot Starter)
* **FEEL Language** - Used for conditional expressions and JSON data transformations.
* **JUnit 5 & Mockito** - Unit testing framework.

##  Running the Project

1. **Camunda 8 Environment Setup:**
   The project can run using a local cluster (e.g., Docker) or Camunda SaaS. In the `application.properties` file, ensure your Zeebe client credentials are set up correctly.
2. **Run the Spring Boot application:**
   ```bash
   mvn spring-boot:run
