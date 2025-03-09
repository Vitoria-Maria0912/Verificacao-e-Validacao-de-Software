package tests.junit5Tests;

import main.enumerations.*;
import main.models.*;
import main.service.ProcessadorContas;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

class ProcessadorContasTest {
    private ProcessadorContas processador;
    private Fatura fatura;

    @BeforeEach
    void setUp() {
        processador = new ProcessadorContas();
        fatura = new Fatura(LocalDate.now(), 1000.0, "Cliente");
    }

    @Test
    @DisplayName("Fatura com valor negativo não deve ser processada")
    void faturaNegativaNaoDeveSerProcessada() {
        fatura.setValorTotalFatura(-500.0);
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 1,  LocalDate.now(), 200.0);

        processador.processarContas(List.of(conta), fatura);

        assertEquals(FaturaStatus.PENDENTE, fatura.getStatus());
    }

    @Test
    @DisplayName("Pagamento com cartão de crédito dentro do prazo é considerado")
    void pagamentoCartaoCreditoDentroPrazo() {
        Conta conta = new Conta(TipoPagamento.CARTAO_CREDITO, 2, LocalDate.now(), 300.0);
        processador.processarContas(List.of(conta), fatura);

        assertEquals(300.0, fatura.getPagamentos().get(0).getValorPago());
    }

    @Test
    @DisplayName("Pagamento com boleto vencido aplica juros de 10%")
    void pagamentoBoletoVencidoAplicaJuros() {
        Conta conta = new Conta(TipoPagamento.BOLETO, 3, LocalDate.now().minusDays(2), 400.0);
        processador.processarContas(List.of(conta), fatura);

        assertEquals(440.0, fatura.getPagamentos().get(0).getValorPago());
    }

    @Test
    @DisplayName("Fatura é marcada como paga quando soma dos pagamentos atinge o total")
    void faturaMarcadaComoPagaSeTotalPagoAlcancado() {
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 4, LocalDate.now(), 500.0);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 5, LocalDate.now(), 500.0);

        processador.processarContas(List.of(conta1, conta2), fatura);

        assertEquals(FaturaStatus.PAGA, fatura.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 110",   // Boleto vencido -> aplica 10% de juros
            "300, 300"    // Boleto sem vencimento → mantém valor
    })
    @DisplayName("Cálculo correto de juros em boleto vencido")
    void calculoJurosBoletoVencido(double valorPago, double esperado) {
        Conta conta = new Conta(TipoPagamento.BOLETO, 6, LocalDate.now().minusDays(2), valorPago);
        processador.processarContas(List.of(conta), fatura);

        assertEquals(esperado, fatura.getPagamentos().get(0).getValorPago());
    }
}
