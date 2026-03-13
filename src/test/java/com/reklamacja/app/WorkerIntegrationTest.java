package com.reklamacja.app;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerUnitTest {

    private CentralnyWorker worker;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JobClient jobClient;

    @Mock
    private ActivatedJob job;

    @BeforeEach
    void setUp() {
        worker = new CentralnyWorker();
    }

    @Test
    void testScenariuszPoprawny_Zamowienie900() {
        when(job.getVariablesAsMap()).thenReturn(Map.of(
                "reklamacja", Map.of("NrZamowienia", 900)
        ));
        when(job.getKey()).thenReturn(12345L);

        worker.weryfikuj(jobClient, job);

        verify(jobClient).newCompleteCommand(12345L);

        verify(jobClient, never()).newThrowErrorCommand(anyLong());

        System.out.println(">>> TEST OK: Worker zaakceptował zamówienie 900.");
    }

    @Test
    void testScenariuszBledny_Zamowienie1500() {
        when(job.getVariablesAsMap()).thenReturn(Map.of(
                "reklamacja", Map.of("NrZamowienia", 1500)
        ));
        when(job.getKey()).thenReturn(67890L);

        worker.weryfikuj(jobClient, job);

        verify(jobClient).newThrowErrorCommand(67890L);

        verify(jobClient, never()).newCompleteCommand(anyLong());

        System.out.println(">>> TEST OK: Worker rzucił błąd dla zamówienia 1500.");
    }
}