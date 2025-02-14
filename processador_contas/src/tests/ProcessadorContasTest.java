package tests;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import main.models.*;
import main.enumerations.*;
import main.service.ProcessadorContas;

public class ProcessadorContasTest {

    private List<Conta> contas;
    private Conta conta;
    private Fatura fatura;
    private Pagamento pagamento;
    private ProcessadorContas processadorContas;

    @BeforeEach
    public void setUp() {
        this.contas = new ArrayList<>();
        this.conta = new Conta(TipoPagamento.CARTAO_CREDITO, 001, (LocalDate.of(2024, 07, 24)), 1000);
        this.contas.add(conta);
        this.fatura = new Fatura((LocalDate.of(2024, 07, 24)), 1000, "Cliente");
        this.pagamento = new Pagamento(TipoPagamento.CARTAO_CREDITO, (LocalDate.of(2024, 07, 24)), 1000);
        this.processadorContas = new ProcessadorContas();
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal >= valorFatura, fatura.Status == paga")
    public void testProcessarContasFaturaPaga() {
        assertAll(
                () -> { this.processadorContas.processarContas(this.contas, this.fatura);
                        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
                },
                () -> { this.fatura.setValorTotalFatura(700);
                        this.processadorContas.processarContas(this.contas, this.fatura);
                        assertEquals(FaturaStatus.PAGA, this.fatura.getStatus());
                }
        );
    }

    @Test
    @DisplayName("Verifica se valorSomaTotal < valorFatura, fatura.Status ==  pendente")
    public void testProcessarContasFaturaPendente() {
        this.conta.setValorPagoConta(700);
        this.processadorContas.processarContas(this.contas, this.fatura);
        assertEquals(FaturaStatus.PENDENTE, this.fatura.getStatus());
    }

    @Test
    @DisplayName("fatura.getData()")
    public void testFaturaData() { assertEquals((LocalDate.of(2024, 07, 24)), this.fatura.getData()); }

    @Test
    @DisplayName("fatura.getValorTotalFatura()")
    public void testFaturaValorTotalFatura() { assertEquals(1000, this.fatura.getValorTotalFatura()); }

    @Test
    @DisplayName("fatura.getNomeCliente()")
    public void testFaturaNomeCliente() { assertEquals("Cliente", this.fatura.getNomeCliente()); }

    @Test
    @DisplayName("conta.getCodigoConta()")
    public void testContaCodigo() { assertEquals(001, this.conta.getCodigoConta()); }

    @Test
    @DisplayName("conta.getData()")
    public void testContaData() { assertEquals((LocalDate.of(2024, 07, 24)), this.conta.getData()); }

    @Test
    @DisplayName("conta.getValorPago()")
    public void testContaValorPago() { assertEquals(1000, this.conta.getValorPagoConta()); }

    @Test
    @DisplayName("O processador de contas deve, para cada conta, criar um \"pagamento\" associado a essa fatura")
    public void testPagamento() {
        this.processadorContas.criarPagamento(this.conta, this.fatura);
        Pagamento pagamentoEsperado = new Pagamento(this.conta.getTipoPagamento(), LocalDate.now(), this.conta.getValorPagoConta());
        assertAll(
                () -> assertEquals(pagamentoEsperado.getTipoPagamento(), fatura.getPagamentos().get(0).getTipoPagamento()),
                () -> assertEquals(pagamentoEsperado.getData(), fatura.getPagamentos().get(0).getData()),
                () -> assertEquals(pagamentoEsperado.getValorPago(), fatura.getPagamentos().get(0).getValorPago())
        );
    }

    @Test
    @DisplayName("pagamento.getTipo()")
    public void testPagamentoTipo() {
        assertAll(
                () -> assertEquals(TipoPagamento.CARTAO_CREDITO, this.conta.getTipoPagamento()),

                () -> { this.conta.setTipoPagamento(TipoPagamento.BOLETO);
                        assertEquals(TipoPagamento.BOLETO, this.conta.getTipoPagamento());
                },
                () -> { this.conta.setTipoPagamento(TipoPagamento.TRANSFERENCIA_BANCARIA);
                        assertEquals(TipoPagamento.TRANSFERENCIA_BANCARIA, this.conta.getTipoPagamento());
                }
        );
    }

    @Test
    @DisplayName("pagamento.getValor()")
    public void testPagamentoValor() { assertEquals(1000, this.pagamento.getValorPago()); }

    @Test
    @DisplayName("pagamento.getData()")
    public void testPagamentoData() { assertEquals((LocalDate.of(2024, 07, 24)), this.pagamento.getData()); }

    @Test
    @DisplayName("! R$ 5.000,00 < pagamento.getValor() < R$0,01")
    public void testPagamentoValorMinimo() {
        this.conta.setTipoPagamento(TipoPagamento.BOLETO);
        this.processadorContas.criarPagamento(this.conta, this.fatura);
        assertAll(
                () -> assertTrue(this.conta.getValorPagoConta() > 0.01),
                () -> assertTrue(this.conta.getValorPagoConta() < 5000),
                () -> { this.conta.setValorPagoConta(0);
                        assertFalse(this.conta.getValorPagoConta() > 0.01); },
                () -> { this.conta.setValorPagoConta(7000);
                        assertFalse(this.conta.getValorPagoConta() < 5000); }
        );
    }

    @Test
    @DisplayName("Se a data de pagamento de um boleto for posterior à data da conta respectiva, então o boleto deve ser acrescido 10%")
    public void testPagamentoComJuros() {
        double valorPagoConta = this.conta.getValorPagoConta();
        this.conta.setTipoPagamento(TipoPagamento.BOLETO);
        this.conta.setData(LocalDate.of(2025, 07, 24));
        this.processadorContas.criarPagamento(this.conta, this.fatura);
        assertEquals((valorPagoConta * 1.1), this.conta.getValorPagoConta());
    }

    @Test
    @DisplayName("Se a fatura for criada com valorTotal negativo.")
    public void testContaComFaturaInvalida() {
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.BOLETO, 001, LocalDate.of(2024, 06, 06), 100.00);
        contas1.add(conta1);
        Fatura fatura1 = new Fatura(LocalDate.now(), -100.00, "Usuário 1");
        this.processadorContas.processarContas(contas1, fatura1);
        assertEquals(FaturaStatus.PENDENTE, fatura1.getStatus());
    }

    @Test
    @DisplayName("Se a fatura no cartão é paga antes dos 15 dias.")
    public void testContaNoCartaoAntesDe15Dias() {
        List<Conta> contas1 = new ArrayList<>();
        contas1.add(new Conta(TipoPagamento.CARTAO_CREDITO, 001, LocalDate.of(2024, 06, 06), 1000.00));
        this.processadorContas.processarContas(contas1, fatura);
        assertEquals(FaturaStatus.PAGA, fatura.getStatus());
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com 3 contas no valor de 500,00, 400,00 e 600,00. " +
                "As três contas foram pagas por boleto no dia 20/02/2023 (todas em dia), " +
                "assim a fatura é marcada como PAGA.\n")
    public void testExemplo1(){
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.BOLETO, 002, LocalDate.of(2023, 02, 20), 500);
        Conta conta2 = new Conta(TipoPagamento.BOLETO, 003, LocalDate.of(2023, 02, 20), 400);
        Conta conta3 = new Conta(TipoPagamento.BOLETO, 004, LocalDate.of(2023, 02, 20), 600);
        contas1.add(conta1);
        contas1.add(conta2);
        contas1.add(conta3);
        Fatura fatura1 = new Fatura(LocalDate.of(2023, 02, 20), 1500, "Cliente Exemplo 1");
        (new ProcessadorContas()).processarContas(contas1, fatura1);

        assertEquals(FaturaStatus.PAGA, fatura1.getStatus());
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. " +
                "A primeira conta foi paga por cartão de crédito (05/02/2023), " +
                "enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PAGA.\n")
    public void testExemplo2(){
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, LocalDate.of(2023, 02, 05), 700);
        Conta conta2 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, LocalDate.of(2023, 02, 17), 800);
        contas1.add(conta1);
        contas1.add(conta2);
        Fatura fatura1 = new Fatura(LocalDate.of(2023, 02, 20), 1500, "Cliente Exemplo 2");
        (new ProcessadorContas()).processarContas(contas1, fatura1);

        assertEquals(FaturaStatus.PAGA, fatura1.getStatus());
    }

    @Test
    @DisplayName("Fatura de 1.500,00 (20/02/2023) com uma conta no valor 700,00 e outra conta de 800,00. " +
                "A primeira conta foi paga por cartão de crédito (06/02/2023), " +
                "enquanto que a segunda conta foi paga por transferência (17/02/2023). Assim, a fatura é marcada como PENDENTE.\n")
    public void testExemplo3(){
        List<Conta> contas1 = new ArrayList<>();
        Conta conta1 = new Conta(TipoPagamento.CARTAO_CREDITO, 002, LocalDate.of(2023, 02, 06), 700.0);
        Conta conta2 = new Conta(TipoPagamento.TRANSFERENCIA_BANCARIA, 003, LocalDate.of(2023, 02, 17), 800.0);
        contas1.add(conta1);
        contas1.add(conta2);
        Fatura fatura1 = new Fatura(LocalDate.of(2023, 02, 20), 1500.0, "Cliente Exemplo 2");
        (new ProcessadorContas()).processarContas(contas1, fatura1);

        assertEquals(FaturaStatus.PENDENTE, fatura1.getStatus());
    }
}
