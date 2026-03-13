package com.reklamacja.app;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CentralnyWorker {

    @JobWorker(type = "weryfikacja-zamowienia", autoComplete = false)
    public void weryfikuj(final JobClient client, final ActivatedJob job) {
        System.out.println("\n##################################################");
        System.out.println(">>> Start weryfikacji zamówienia...");

        Map<String, Object> variables = job.getVariablesAsMap();
        Integer nrZamowienia = null;

        try {
            if (variables.containsKey("reklamacja") && variables.get("reklamacja") instanceof Map) {
                Map<?, ?> rek = (Map<?, ?>) variables.get("reklamacja");
                Object nrObj = rek.get("NrZamowienia");
                if (nrObj != null) nrZamowienia = Integer.parseInt(nrObj.toString());
            } else if (variables.containsKey("NrZamowienia")) {
                nrZamowienia = Integer.parseInt(variables.get("NrZamowienia").toString());
            }
        } catch (Exception e) {
            System.out.println(">>> [WORKER] Błąd parsowania numeru: " + e.getMessage());
        }

        if (nrZamowienia != null && nrZamowienia < 1000) {
            System.out.println(">>> [WORKER] Zamówienie " + nrZamowienia + " jest poprawne.");
            System.out.println(">>> [WORKER] Wynik: SUKCES. Przesuwam do DMN.");

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("czyZamowienieOK", true))
                    .send()
                    .join();

        } else {
            System.out.println(">>> [WORKER] BŁĄD: Zamówienie " + nrZamowienia + " nie istnieje!");
            System.out.println(">>> [WORKER] Rzucam BPMN Error: ORDER_NOT_FOUND");

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("ORDER_NOT_FOUND")
                    .errorMessage("Podano błędny numer zamówienia: " + nrZamowienia)
                    .send()
                    .join();
        }
        System.out.println("##################################################\n");
    }

    @JobWorker(type = "wyslij-powiadomienie", autoComplete = false)
    public void wyslijPowiadomienie(final JobClient client, final ActivatedJob job) {
        try {
            System.out.println(">>> [EMAIL WORKER] Wysyłanie powiadomienia do klienta...");

            Thread.sleep(100);

            client.newCompleteCommand(job.getKey())
                    .send()
                    .join();

            System.out.println(">>> [EMAIL WORKER] Powiadomienie wysłane.");

        } catch (Exception e) {
            e.printStackTrace();
            client.newFailCommand(job.getKey()).retries(0).errorMessage(e.getMessage()).send().join();
        }
    }
}