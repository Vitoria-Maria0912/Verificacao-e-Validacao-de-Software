package tests.functionalTests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import main.models.*;
import main.enumerations.*;
import main.service.ProcessadorContas;

public class ProcessadorContasTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private List<Conta> contas;
    private ProcessadorContas processadorContas;

    @BeforeEach
    public void setUp() {
        this.contas = new ArrayList<>();
        // this.conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        // this.contas.add(conta);
        // this.fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
        // this.pagamento = new Pagamento(TipoPagamento.CARTAO_CREDITO, (LocalDate.of(2024, 07, 24)), 1000);
        this.processadorContas = new ProcessadorContas();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() { this.contas.clear(); System.setOut(originalOut); }

    @Nested
    @DisplayName("Executando testes de 'Análise do Valor Limite'")
    class AVLTest {

        @Test
        @DisplayName("TC01 - Testa acima do limite superior do valor boleto pago, mantendo a fatura como pendente.")
        public void testAcimaLimiteSuperior() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 5001);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
        }

        @Test
        @DisplayName("TC02 - Testa se no limite superior do valor boleto é respeitado.")
        public void testLimiteSuperior() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 5000);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC03 - Testa próximo ao limite superior do valor boleto.")
        public void testProximoLimiteSuperior() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 4999);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC04 - Testa, com um valor qualquer, se os limites superior e inferior do valor boleto são respeitados.")
        public void testValorQualquer() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 100);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC05 - Testa próximo ao limite inferior do valor boleto.")
        public void testProximoLimiteInferior() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 0.02);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 0.1, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC06 - Testa se no limite inferior do valor boleto é respeitado.")
        public void testLimiteInferior() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 0.01);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
        }

        @Test
        @DisplayName("TC07 - Testa abaixo do limite inferior do valor boleto pago, mantendo a fatura como pendente.")
        public void testAbaixoLimiteInferior() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 0);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
        }
    }

    @Nested
    @DisplayName("Executando testes de 'Partições de Equivalência'")
    class ParticaoTest {

        @Test
        @DisplayName("TC08 - Muda o status da fatura para 'Paga' se a variável 'Valor total soma contas' for igual a 'Valor da fatura'.")
        public void testFaturaPagaValoresIguais() { 
            Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            contas.add(conta);
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC09 - Muda o status da fatura para 'Paga' se a variável 'Valor total soma contas' for maior que a 'Valor da fatura'.")
        public void testFaturaPagaFaturaMenor() { 
            Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 800, "Cliente");
            contas.add(conta);
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC10 - Mantém o status da fatura como 'Pendente' se a variável 'Valor total soma contas' for menor que a 'Valor da fatura'.")
        public void testFaturaPagaFaturaMaior() { 
            Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 800);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            contas.add(conta);
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
        }
    }

    @Nested
    @DisplayName("Executando testes de 'Tabelas de Decisão'")
    class TabelaTest {

        @Test
        @DisplayName("")
        public void test() { assertEquals(1,1); }
    }
}
