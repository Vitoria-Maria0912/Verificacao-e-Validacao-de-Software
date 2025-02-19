package tests.functionalTests;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.*;

import main.models.*;
import main.enumerations.*;
import main.service.ProcessadorContas;

public class ProcessadorContasTest {

    private List<Conta> contas;
    private ProcessadorContas processadorContas;

    @BeforeEach
    public void setUp() {
        this.contas = new ArrayList<>();
        this.processadorContas = new ProcessadorContas();
    }

    @AfterEach
    public void tearDown() { this.contas.clear(); }

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
        public void testLimiteSuperiorBoleto() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 5000);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC03 - Testa próximo ao limite superior do valor boleto.")
        public void testProximoLimiteSuperiorBoleto() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 4999);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC04 - Testa, com um valor qualquer, se os limites superior e inferior do valor boleto são respeitados.")
        public void testValorQualquerBoleto() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 100);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC05 - Testa próximo ao limite inferior do valor boleto.")
        public void testProximoLimiteInferiorBoleto() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 0.02);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 0.1, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC06 - Testa se no limite inferior do valor boleto é respeitado.")
        public void testLimiteInferiorBoleto() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 0.01);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
        }

        @Test
        @DisplayName("TC07 - Testa abaixo do limite inferior do valor boleto pago, mantendo a fatura como pendente.")
        public void testAbaixoLimiteInferiorBoleto() { 
            Conta conta = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 0);
            contas.add(conta);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
        }

        @Test
        @DisplayName("TC08 - Testa se o valor da conta foi incluso na fatura, quando a data da conta é um ano antes da data da fatura.")
        public void testValorQualquer() { 
            Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2023, 07, 24)), 1000);
            Conta conta2 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, (LocalDate.of(2023, 07, 24)), 1000);
            Conta conta3 = new Conta(TipoPagamento.CARTAO_CREDITO, 003, (LocalDate.of(2023, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta());

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            double valorPagamentoFatura = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

            assertEquals(valorContas, valorPagamentoFatura, 0);
        }

        @Test
        @DisplayName("TC09 - Testa se o valor da conta foi incluso na fatura, quando a data da conta é 16 dias antes da data da fatura.")
        public void testProximoLimiteInferior() { 
            Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2023, 07, 8)), 1000);
            Conta conta2 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, (LocalDate.of(2023, 07, 8)), 1000);
            Conta conta3 = new Conta(TipoPagamento.CARTAO_CREDITO, 003, (LocalDate.of(2023, 07, 8)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta());

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            double valorPagamentoFatura = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

            assertEquals(valorContas, valorPagamentoFatura, 0);
        }
        
        @Test
        @DisplayName("TC10 - Testa se o valor da conta foi incluso na fatura, quando a data da conta é 15 dias antes da data da fatura.")
        public void testLimiteInferior() { 
            Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2023, 07, 9)), 1000);
            Conta conta2 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, (LocalDate.of(2023, 07, 9)), 1000);
            Conta conta3 = new Conta(TipoPagamento.CARTAO_CREDITO, 003, (LocalDate.of(2023, 07, 9)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta());

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            double valorPagamentoFatura = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

            assertEquals(valorContas, valorPagamentoFatura, 0);
        }

        @Test
        @DisplayName("TC11 - Testa se o valor da conta foi incluso na fatura, quando a data da conta é 14 dias antes da data da fatura.")
        public void testAbaixoLimiteInferior() { 
            Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2023, 07, 10)), 1000);
            Conta conta2 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, (LocalDate.of(2023, 07, 10)), 1000);
            Conta conta3 = new Conta(TipoPagamento.CARTAO_CREDITO, 003, (LocalDate.of(2023, 07, 10)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            int valorPagamentoFatura = ((int)fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum());

            assertEquals(0, valorPagamentoFatura);
        }
    }

    @Nested
    @DisplayName("Executando testes de 'Partições de Equivalência'")
    class ParticaoTest {

        @Test
        @DisplayName("TC12 - Muda o status da fatura para 'Paga' se a variável 'Valor total soma contas' for igual a 'Valor da fatura'.")
        public void testFaturaPagaValoresIguais() { 
            Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
            contas.add(conta);
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC13 - Muda o status da fatura para 'Paga' se a variável 'Valor total soma contas' for maior que a 'Valor da fatura'.")
        public void testFaturaPagaFaturaMenor() { 
            Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 800, "Cliente");
            contas.add(conta);
            processadorContas.processarContas(contas, fatura);
            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
        }

        @Test
        @DisplayName("TC14 - Mantém o status da fatura como 'Pendente' se a variável 'Valor total soma contas' for menor que a 'Valor da fatura'.")
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
        @DisplayName("RN01 - Condições: C01, C02, C04 ser falsa; Ações: A01, A03")
        public void testRN01() { 
            Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta2 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta3 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta());

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            int valorPagamentoFatura = 0;
            for (Pagamento pagamento : fatura.getPagamentos()) { valorPagamentoFatura += pagamento.getValorPago(); }

            assertEquals(valorContas, valorPagamentoFatura, 1.1);
        }

        @Test
        @DisplayName("RN02 - Condições: C01 ser falsa, C02; Ações: nenhuma")
        public void testRN02() { 
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            processadorContas.processarContas(contas, fatura);
            assertEquals(0, fatura.getPagamentos().size());
        }

        @Test
        @DisplayName("RN03 - Condições: C01, C02, C03, C06, C09; Ações: A01, A02, A03")
        public void testRN03() { 
            Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta2 = new Conta(TipoPagamento.BOLETO, 002, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta3 = new Conta(TipoPagamento.BOLETO, 003, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 25)), 3000, "Cliente");
            double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta()) * 1.1;

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            int valorPagamentoFatura = 0;
            for (Pagamento pagamento : fatura.getPagamentos()) { valorPagamentoFatura += pagamento.getValorPago(); }

            assertEquals(valorContas, valorPagamentoFatura, 1.1);
        }

        @Test
        @DisplayName("RN04 - Condições: C01, C02; Ações: A01")
        public void testRN04() { 
            Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta2 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta3 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 23)), 3000, "Cliente");

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            int valorPagamentoFatura = (int) fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

            assertEquals(0, valorPagamentoFatura);
        }

        @Test
        @DisplayName("RN05 - Condições: C01, C02, C03, C09; Ações: A01, A03")
        public void testRN05() { 
            Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta2 = new Conta(TipoPagamento.BOLETO, 002, (LocalDate.of(2024, 07, 24)), 1000);
            Conta conta3 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, (LocalDate.of(2024, 07, 24)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            int valorContas = (int) (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta());

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            int valorPagamentoFatura = (int) fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

            assertEquals(valorContas, valorPagamentoFatura);
        }

        // Os testes das regras de negócios 6 e 7 são os testes TC08 - TC11 de AVL

        @Test
        @DisplayName("RN08 - Condições: C01, C02, C04, C08; Ações: A01, A03")
        public void testRN08() { 
            Conta conta1 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 001, (LocalDate.of(2024, 07, 23)), 1000);
            Conta conta2 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 002, (LocalDate.of(2024, 07, 23)), 1000);
            Conta conta3 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, (LocalDate.of(2024, 07, 23)), 1000);
            Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
            int valorContas = (int) (conta1.getValorPagoConta() + conta2.getValorPagoConta() + conta3.getValorPagoConta());

            contas.add(conta1); contas.add(conta2); contas.add(conta3);
            processadorContas.processarContas(contas, fatura);

            int valorPagamentoFatura = (int) fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

            assertEquals(valorContas, valorPagamentoFatura);
        }
    }
}
