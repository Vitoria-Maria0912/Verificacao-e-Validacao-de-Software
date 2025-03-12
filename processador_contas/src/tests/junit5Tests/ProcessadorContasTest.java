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
    @Order(1)
    void testFaturaNegativaNaoDeveSerProcessada() {
        fatura.setValorTotalFatura(-500.0);
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 1,  LocalDate.now(), 200.0);

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }

    @Test
    @DisplayName("Pagamento com cartão de crédito dentro do prazo é considerado")
    @Tag("Cartão de crédito")
    @Order(2)
    void testPagamentoCartaoCreditoDentroPrazo() {
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 2, LocalDate.now(), 300.0);
        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(300.0, fatura.getPagamentos().get(0).getValorPago(), 1);
    }

    @Test
    @DisplayName("Pagamento com boleto vencido aplica juros de 10%")
    @Tag("Boleto")
    @Order(3)
    void testPagamentoBoletoVencidoAplicaJuros() {
        Conta conta = new Conta(TipoPagamento.BOLETO, 3, LocalDate.now().minusDays(2), 400.0);
        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(440.0, fatura.getPagamentos().get(0).getValorPago(), 0.01);
    }

    @Test
    @DisplayName("Fatura é marcada como paga quando soma dos pagamentos atinge o total")
    @Tag("Fatura paga")
    @Order(4)
    void testFaturaMarcadaComoPagaSeTotalPagoAlcancado() {
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 4, LocalDate.now(), 500.0);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 5, LocalDate.now(), 500.0);

        processadorContas.processarContas(List.of(conta1, conta2), fatura);

        assertEquals(FaturaStatus.PAGA, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 110",
            "300, 300"
    })
    @Tag("Boleto")
    @DisplayName("Cálculo correto de juros em boleto vencido")
    @Order(5)
    void testCalculoJurosBoletoVencido(double valorPago, double esperado) {
        Conta conta = new Conta(TipoPagamento.BOLETO, 6, LocalDate.now().minusDays(2), valorPago);
        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(esperado, fatura.getPagamentos().get(0).getValorPago(), 0.01);
    }

    @ParameterizedTest(name = "Boleto pago com valor {0} tem status {1}")
    @CsvSource({
            "5001, PAGA",
            "5000, PAGA",
            "4999, PAGA",
            "100, PENDENTE",
            "0.02, PENDENTE",
            "0.01, PENDENTE",
            "0, PENDENTE"
    })
    @Tag("Boleto")
    @DisplayName("Limites do boleto")
    @Order(6)
    void testLimitesValorBoleto(double valorPago, String statusEsperado) {
        Conta conta = new Conta(TipoPagamento.BOLETO,1, LocalDate.of(2024, 7, 24), valorPago);
        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), 1000, "Cliente");

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.valueOf(statusEsperado), fatura.getStatus());
    }

    @Test
    @Tag("Boleto")
    @DisplayName("Aplicação de Multa de 10% para Boletos Pagos com Atraso")
    @Order(7)
    void testAplicacaoMultaBoletoAtrasado() {
        Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        Fatura fatura = new Fatura((LocalDate.of(2024, 07, 24)), 3000, "Cliente");
        double valorContas = (conta1.getValorPagoConta() + conta2.getValorPagoConta());

        contas.add(conta1); contas.add(conta2);
        processadorContas.processarContas(contas, fatura);

        int valorPagamentoFatura = 0;
        for (Pagamento pagamento : fatura.getPagamentos()) { valorPagamentoFatura += pagamento.getValorPago(); }

        assertEquals(valorContas, valorPagamentoFatura, 0.01);
    }

    @Test
    @Disabled("Teste temporariamente desativado por conta de mudanças na regra de negócio")
    @Order(8)
    void testDesativado() {
        Fatura fatura = new Fatura(LocalDate.now(), 200, "Cliente Y");
        Conta conta = new Conta(TipoPagamento.BOLETO,2, LocalDate.now(), 100);

        processadorContas.processarContas(List.of(conta), fatura);
        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }
    @Test
    @DisplayName("Fatura com data mínima possível")
    @Timeout(1)
    @Order(9)
    void testFaturaDataMinima() {
        Fatura fatura = new Fatura(LocalDate.of(1900, 1, 1), 100, "Cliente Z");
        assertNotNull(fatura);
    }

    @Test
    @DisplayName("Fatura com data máxima possível")
    @Timeout(1)
    @Order(10)
    void testFaturaDataMaxima() {
        Fatura fatura = new Fatura(LocalDate.of(3000, 12, 31), 100, "Cliente Z");
        assertNotNull(fatura);
    }

    @Test
    @DisplayName("Processar 1.000 contas")
    @Timeout(3)
    @Order(11)
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
    @Order(12)
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
    @Order(13)
    @DisplayName("Verifica valores extremos de um boleto")
    void testBoletosValoresExtremos(double valor, String esperado) {
        Fatura fatura = new Fatura(LocalDate.now(), valor, "Cliente X");
        Conta conta = new Conta(TipoPagamento.BOLETO,5, LocalDate.now(), valor);

        processadorContas.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.valueOf(esperado), fatura.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    @Tag("Excecao")
    @Tag("Conta")
    @Order(14)
    void testValorNegativoConta() {
        assertThrows(IllegalArgumentException.class, () -> new Conta(TipoPagamento.BOLETO, 1, LocalDate.now(), -1));
    }

    @Test
    @DisplayName("Não deve processar fatura com valor negativo")
    @Tag("Excecao")
    @Tag("Fatura")
    @Order(15)
    void testNaoProcessarFaturaNegativa() {
        fatura = new Fatura(LocalDate.now(), -500, "Cliente X");
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 1, LocalDate.now(), 100);
        assertDoesNotThrow(() -> processadorContas.processarContas(List.of(conta), fatura));
        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }

    @TestFactory
    @Tag("Fatura paga")
    @DisplayName("Gera novos testes com [5, 10, 100, 1000] para valorPagoConta")
    @Order(16)
    Stream<DynamicTest> testDynamicProcessamentoListas() {
        return Stream.of(0, 10, 100, 1000)
                .map(size -> DynamicTest.dynamicTest(
                        "Testando com " + size + " contas",
                        () -> {
                            Fatura fatura = new Fatura(LocalDate.now(), size * 10, "Cliente X");
                            List<Conta> contas = Stream.generate(() -> new Conta(TipoPagamento.BOLETO, 10, LocalDate.now(), size))
                                    .limit(size)
                                    .toList();

                            processadorContas.processarContas(contas, fatura);

                            assertEquals(FaturaStatus.PAGA, fatura.getStatus());
                        }
                ));
    }

    @ParameterizedTest
    @CsvSource({
            "CARTAO_CREDITO, 100, 0, PENDENTE",
            "CARTAO_CREDITO, 1500, 20, PENDENTE",
            "TRANSFERENCIA_BANCARIA, 1500, 20, PAGA",
            "BOLETO, 500, 5, PENDENTE",
            "BOLETO, 2000, -5, PAGA"
    })
    @DisplayName("Processamento de Contas")
    @Order(17)
    void testProcessarContas(TipoPagamento tipo, double valorPago, int diasDiferenca, FaturaStatus esperado) {
        fatura = new Fatura(LocalDate.now(), 1000, "Cliente Y");
        Conta conta = new Conta(tipo, 1, LocalDate.now().plusDays(diasDiferenca), valorPago);
        processadorContas.processarContas(List.of(conta), fatura);
        assertEquals(esperado, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "BOLETO, 50, 55",
            "BOLETO, 1000, 1100",
            "TRANSFERENCIA_BANCARIA, 5000, 5000",
            "CARTAO_CREDITO, 500, 500",
            "BOLETO, 100, 110",  // 10% de acréscimo para boletos pagos após o vencimento
            "BOLETO, 5000, 5000", // Sem alteração para valores altos
            "CARTAO_CREDITO, 200, 200" // Sem alteração para cartão de crédito
    })
    @DisplayName("Aplica regras de pagamento corretamente e calcula corretamente pagamentos com juros")
    @Tag("Pagamento")
    @Order(18)
    void testCriarPagamento(TipoPagamento tipoPagamento, double valorPago, double valorEsperado) {
        Conta conta = new Conta(tipoPagamento, 1, LocalDate.now().minusDays(5), valorPago);
        Fatura fatura = new Fatura(LocalDate.now().minusDays(1), 1000, "Cliente");

        processadorContas.criarPagamento(conta, fatura);
        assertEquals(valorEsperado, conta.getValorPagoConta(), 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "5001, PAGA",
            "5000, PAGA",
            "4999, PAGA",
            "100, PENDENTE",
            "0.02, PENDENTE",
            "0.01, PENDENTE",
            "0, PENDENTE"
    })
    @DisplayName("Testa diferentes limites de valor do boleto pago.")
    @Tag("LimitesBoleto")
    @Order(19)
    void testLimitesBoleto(double valorPago, FaturaStatus statusEsperado) {
        Conta conta = new Conta(TipoPagamento.BOLETO, 1, LocalDate.of(2024, 7, 24), valorPago);
        contas.add(conta);
        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), 1000, "Cliente");
        processadorContas.processarContas(contas, fatura);
        assertEquals(statusEsperado, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "2023-07-24, 3000, 3000",
            "2023-07-08, 3000, 3000",
            "2023-07-09, 3000, 3000",
            "2024-07-30, 3000, 0"
    })
    @Tag("DatasContas")
    @DisplayName("Testa inclusão de contas na fatura com diferentes datas.")
    void testInclusaoContasFatura(String dataConta, double valorFatura, double valorEsperado) {
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 1, LocalDate.parse(dataConta), 1000);
        Conta conta2 = new Conta(TipoPagamento.CARTAO_CREDITO, 2, LocalDate.parse(dataConta), 1000);
        Conta conta3 = new Conta(TipoPagamento.CARTAO_CREDITO, 3, LocalDate.parse(dataConta), 1000);
        contas.add(conta1);
        contas.add(conta2);
        contas.add(conta3);

        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), valorFatura, "Cliente");
        processadorContas.processarContas(contas, fatura);

        double valorTotalPago = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();
        assertEquals(valorEsperado, valorTotalPago, 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "CARTAO_CREDITO, 100, 0, PENDENTE",
            "CARTAO_CREDITO, 1500, 14, PENDENTE",
            "CARTAO_CREDITO, 1500, 20, PAGA",
            "TRANSFERENCIA_BANCARIA, 1500, 20, PAGA",
            "BOLETO, 500, 5, PENDENTE",
            "BOLETO, 2000, 5, PAGA"
    })
    @DisplayName("Processamento de Contas")
    @Order(17)
    void testProcessaContas(TipoPagamento tipo, double valorPago, int diasDiferenca, FaturaStatus esperado) {
        fatura = new Fatura(LocalDate.now(), 1000, "Cliente Y");
        Conta conta = new Conta(tipo, 1, LocalDate.now().minusDays(diasDiferenca), valorPago);
        processadorContas.processarContas(List.of(conta), fatura);
        assertEquals(esperado, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "1000, 1000, PAGA",
            "1200, 1000, PAGA",
            "800, 1000, PENDENTE"
    })
    @DisplayName("Verifica se a fatura muda para 'PAGA' quando o valor total das contas é suficiente")
    @Tag("FaturaStatus")
    void testStatusFaturaBaseadoNoTotalDePagamentos(double valorConta, double valorFatura, FaturaStatus statusEsperado) {
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 1, LocalDate.of(2024, 7, 24), valorConta);
        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), valorFatura, "Cliente");

        contas.add(conta);
        processadorContas.processarContas(contas, fatura);

        assertEquals(statusEsperado, fatura.getStatus());
    }

    @Test
    @DisplayName("Não deve processar fatura com valor negativo")
    @Tag("Validacao")
    void testFaturaComValorNegativo() {
        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), -500, "Cliente");

        assertDoesNotThrow(() -> processadorContas.processarContas(contas, fatura));
        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "2024-07-23, 2024-07-24, 1000",
            "2024-07-23, 2024-07-24, 2000",
            "2024-07-23, 2024-07-24, 3000"
    })
    @DisplayName("Processamento de pagamentos por transferência bancária")
    @Tag("Transferencia_Bancaria")
    void testProcessaPagamentosPorTransferenciaBancaria(String dataConta, String dataFatura, double valorConta) {
        List<Conta> contas = List.of(
                new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 1, LocalDate.parse(dataConta), valorConta),
                new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 2, LocalDate.parse(dataConta), valorConta),
                new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 3, LocalDate.parse(dataConta), valorConta)
        );

        Fatura fatura = new Fatura(LocalDate.parse(dataFatura), 3 * valorConta, "Cliente");

        processadorContas.processarContas(contas, fatura);

        double totalPago = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();
        double esperado = contas.stream().mapToDouble(Conta::getValorPagoConta).sum();

        assertEquals(esperado, totalPago, 0.01);
    }

    @Test
    @DisplayName("Pagamentos processados sem acréscimos para faturas não vencidas")
    @Tag("Transferencia_Bancaria")
    void testProcessaPagamentosParaFaturaEmDia() {
        List<Conta> contas = List.of(
                new Conta(TipoPagamento.BOLETO, 1, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.BOLETO, 2, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 3, LocalDate.of(2024, 7, 24), 1000)
        );

        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), 3000, "Cliente");

        processadorContas.processarContas(contas, fatura);

        double totalPago = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();
        double esperado = contas.stream().mapToDouble(Conta::getValorPagoConta).sum();

        assertEquals(esperado, totalPago, 0.01);
    }

    @Test
    @DisplayName("Pagamento de múltiplas contas com diferentes métodos")
    void testProcessaPagamentosParaDiferentesMetodosDePagamento() {
        List<Conta> contas = List.of(
                new Conta(TipoPagamento.BOLETO, 1, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.BOLETO, 2, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 3, LocalDate.of(2024, 7, 24), 1000)
        );

        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), 3000, "Cliente");

        processadorContas.processarContas(contas, fatura);

        double totalPago = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();
        double esperado = contas.stream().mapToDouble(Conta::getValorPagoConta).sum();

        assertEquals(esperado, totalPago, 0.01);
    }

    @DisplayName("Nenhum pagamento processado quando não há contas")
    @Test
    void testMantemFaturaSemPagamentosQuandoNaoHaContas() {
        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 24), 3000, "Cliente");

        processadorContas.processarContas(new ArrayList<>(), fatura);
        assertEquals(0, fatura.getPagamentos().size());
    }

    @Test
    @Tag("Boleto")
    @DisplayName("Aplicação de acréscimo sobre pagamentos com vencimento ultrapassado")
    void testAplicaAcrescimoParaPagamentosAtrasados() {
        List<Conta> contas = List.of(
                new Conta(TipoPagamento.BOLETO, 1, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.BOLETO, 2, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.BOLETO, 3, LocalDate.of(2024, 7, 24), 1000)
        );

        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 25), 3000, "Cliente");

        processadorContas.processarContas(contas, fatura);
        double totalPago = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

        assertEquals(3300, totalPago, 0.01);
    }

    @Test
    @Tag("Fatura")
    @DisplayName("Pagamentos não são processados se vencimento da fatura for anterior")
    void testProcessaPagamentosQuandoFaturaEstaVencida() {
        List<Conta> contas = List.of(
                new Conta(TipoPagamento.BOLETO, 1, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.CARTAO_CREDITO, 2, LocalDate.of(2024, 7, 24), 1000),
                new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 3, LocalDate.of(2024, 7, 24), 1000)
        );

        Fatura fatura = new Fatura(LocalDate.of(2024, 7, 23), 3000, "Cliente");

        processadorContas.processarContas(contas, fatura);

        double totalPago = fatura.getPagamentos().stream().mapToDouble(Pagamento::getValorPago).sum();

        assertEquals(3100, totalPago, 0.01);
    }
}
