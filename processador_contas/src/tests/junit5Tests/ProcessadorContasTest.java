package tests.junit5Tests;

import main.enumerations.*;
import main.models.*;
import main.service.ProcessadorContas;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

class ProcessadorContasTest {

    private ProcessadorContas processadorContas;
    private List<Conta> contas;
    private Fatura fatura;

    @BeforeEach
    void setUp() {
        this.processadorContas = new ProcessadorContas();
        this.fatura = new Fatura(LocalDate.now(), 1000.0, "Cliente");
        this.contas = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        this.processadorContas = new ProcessadorContas();
        this.contas = new ArrayList<>();
    }

    @Test
    @DisplayName("Fatura com valor negativo não deve ser processada")
    @Tag("Fatura negativa")
    @Tag("Fatura pendente")
    void testFaturaNegativaNaoDeveSerProcessada() {
        fatura.setValorTotalFatura(-500.0);
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 1,  LocalDate.now(), 200.0);

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }

    @Test
    @DisplayName("Pagamento com cartão de crédito dentro do prazo é considerado")
    @Tag("Cartão de crédito")
    void testPagamentoCartaoCreditoDentroPrazo() {
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 2, LocalDate.now(), 300.0);
        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(300.0, fatura.getPagamentos().get(0).getValorPago(), 1);
    }

    @Test
    @DisplayName("Pagamento com boleto vencido aplica juros de 10%")
    @Tag("Boleto")
    void testPagamentoBoletoVencidoAplicaJuros() {
        Conta conta = new Conta(TipoPagamento.BOLETO, 3, LocalDate.now().minusDays(2), 400.0);
        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(440.0, fatura.getPagamentos().get(0).getValorPago(), 1.1);
    }

    @Test
    @DisplayName("Fatura é marcada como paga quando soma dos pagamentos atinge o total")
    @Tag("Fatura paga")
    void testFaturaMarcadaComoPagaSeTotalPagoAlcancado() {
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 4, LocalDate.now(), 500.0);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 5, LocalDate.now(), 500.0);

        processadorContas.processarContas(List.of(conta1, conta2), fatura);

        assertEquals(FaturaStatus.PAGA, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 110",   // Boleto vencido -> aplica 10% de juros
            "300, 300"    // Boleto sem vencimento → mantém valor
    })
    @Tag("Boleto")
    @DisplayName("Cálculo correto de juros em boleto vencido")
    void testCalculoJurosBoletoVencido(double valorPago, double esperado) {
        Conta conta = new Conta(TipoPagamento.BOLETO, 6, LocalDate.now().minusDays(2), valorPago);
        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(esperado, fatura.getPagamentos().get(0).getValorPago(), 1.1);
    }

    @ParameterizedTest(name = "Boleto pago com valor {0} tem status {1}")
    @CsvSource({
            "5001, PENDENTE",
            "5000, PAGA",
            "4999, PAGA",
            "100, PAGA",
            "0.02, PAGA",
            "0.01, PENDENTE",
            "0, PENDENTE"
    })
    @Tag("Boleto")
    @DisplayName("Limites do boleto")
    void testLimitesValorBoleto(double valorPago, String statusEsperado) {
        Conta conta = new Conta(TipoPagamento.BOLETO,1, LocalDate.of(2024, 7, 24), valorPago);
        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), 1000, "Cliente");

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.valueOf(statusEsperado), fatura.getStatus());
    }

    @Test
    @Tag("Boleto")
    @DisplayName("Aplicação de Multa de 10% para Boletos Pagos com Atraso")
    void testAplicacaoMultaBoletoAtrasado() {
        Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
        double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta());

        contas.add(conta1); contas.add(conta2);
        processadorContas.processarContas(contas, fatura);

        int valorPagamentoFatura = 0;
        for (Pagamento pagamento : fatura.getPagamentos()) { valorPagamentoFatura += pagamento.getValorPago(); }

        assertEquals(valorContas, valorPagamentoFatura, 1.1);
    }

    @Test
    @Disabled("Teste temporariamente desativado por conta de mudanças na regra de negócio")
    void testDesativado() {
        Fatura fatura = new Fatura(LocalDate.now(), 200, "Cliente Y");
        Conta conta = new Conta(TipoPagamento.BOLETO,2, LocalDate.now(), 100);

        processadorContas.processarContas(List.of(conta), fatura);
        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }
    @Test
    @DisplayName("Fatura com data mínima possível")
    @Timeout(1)
    void test_Fatura_Data_Minima() {
        Fatura fatura = new Fatura(LocalDate.of(1900, 1, 1), 100, "Cliente Z");
        assertNotNull(fatura);
    }

    @Test
    @DisplayName("Fatura com data máxima possível")
    @Timeout(1)
    void test_Fatura_Data_Maxima() {
        Fatura fatura = new Fatura(LocalDate.of(3000, 12, 31), 100, "Cliente Z");
        assertNotNull(fatura);
    }

    @Test
    @DisplayName("Processar 1.000 contas")
    @Timeout(3)
    void testProcessarMuitasContas() {
        Fatura fatura = new Fatura(LocalDate.now(), 1_000_000, "Cliente Grande");
        List<Conta> contas = Stream.generate(() -> new Conta(TipoPagamento.BOLETO, 0, LocalDate.now(), 1000))
                .limit(1000)
                .toList();

        processadorContas.processarContas(contas, fatura);

        assertEquals(FaturaStatus.PAGA, fatura.getStatus());
    }

    @RepeatedTest(5)
    @DisplayName("Repetindo processamento de contas")
    void testProcessamentoRepetido(RepetitionInfo info) {
        System.out.println("Execução #" + info.getCurrentRepetition());
        Fatura fatura = new Fatura(LocalDate.now(), 500, "Cliente");
        Conta conta = new Conta(TipoPagamento.BOLETO,9, LocalDate.now(), 500);

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.PAGA, fatura.getStatus());
    }

    @ParameterizedTest(name = "Teste valor {0}")
    @CsvSource({
            "0.01, PENDENTE",
            "5000, PAGA",
            "999999999, PAGA"
    })
    @Tag("LimiteSuperior")
    void testBoletosValoresExtremos(double valor, String esperado) {
        Fatura fatura = new Fatura(LocalDate.now(), valor, "Cliente X");
        Conta conta = new Conta(TipoPagamento.BOLETO,5, LocalDate.now(), valor);

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.valueOf(esperado), fatura.getStatus());
    }

    @Test
    @DisplayName("🔴 Deve lançar exceção para valor negativo")
    @Tag("Excecao")
    void test_Valor_Negativo() {
        assertThrows(IllegalArgumentException.class, (Executable) new Conta(TipoPagamento.BOLETO, 1, LocalDate.now(), -1));
    }

    @TestFactory
    @Tag("Fatura paga")
    @DisplayName("")
    Stream<DynamicTest> testDynamicProcessamentoListas() {
        return Stream.of(1, 10, 100, 1000)
                .map(size -> DynamicTest.dynamicTest(
                        "Testando com " + size + " contas",
                        () -> {
                            Fatura fatura = new Fatura(LocalDate.now(), size * 10, "Cliente X");
                            Double random = (new Random()).nextDouble();
                            List<Conta> contas = Stream.generate(() -> new Conta(TipoPagamento.BOLETO, 10, LocalDate.now(), size))
                                    .limit(size)
                                    .toList();

                            processadorContas.processarContas(contas, fatura);

                            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
                        }
                ));
    }
}
